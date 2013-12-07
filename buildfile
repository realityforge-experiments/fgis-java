require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'
require 'buildr/single_intermediate_layout'

JEE_GWT_JARS = [:javax_inject, :javax_annotation, :javax_validation, :javax_validation_sources, :findbugs_annotations]
GWT_JARS = JEE_GWT_JARS +
  [:gwt_openlayers, :gwt_user, :gwt_dev, :gwt_gin, :google_guice, :aopalliance, :google_guice_assistedinject]
JEE_JARS = [:javax_persistence,
            :javax_transaction,
            :javax_inject,
            :javax_json,
            :ejb_api,
            :javax_rs,
            :javax_servlet,
            :javax_annotation,
            :javax_validation]
GEO_DEPS = [:jeo, :geolatte_geom_eclipselink, :geolatte_geom, :jts, :slf4j_api, :slf4j_jdk14]
JACKSON_DEPS = [:jackson_core, :jackson_mapper]
PROVIDED_DEPS = JACKSON_DEPS + JEE_JARS
INCLUDED_DEPENDENCIES = [:rest_field_filter, :javax_json, :gwt_appcache_server, :gwt_openlayers_server] + GEO_DEPS

desc 'FGIS: Fire Ground Information System'
define 'fgis' do
  project.group = 'org.realityforge.fgis'

  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  Domgen::GenerateTask.new(:FGIS, "server", [:ee], _(:target, :generated, "domgen"))

  define_process_sass_dir(project)
  define_coffee_script_dir(project)

  compile.with GWT_JARS, INCLUDED_DEPENDENCIES, PROVIDED_DEPS

  gwt(["fgis.Fgis"],
      :java_args => ["-Xms512M", "-Xmx1024M", "-XX:PermSize=128M", "-XX:MaxPermSize=256M"],
      :draft_compile => (ENV["FAST_GWT"] == 'true'))

  test.using :testng

  package(:war).tap do |war|
    project.assets.paths.each do |asset|
      war.tap do |w|
        w.enhance([asset])
      end
      war.include asset, :as => '.'
    end
    war.with :libs => artifacts(INCLUDED_DEPENDENCIES)
  end

  project.clean { rm_rf _(:artifacts) }

  desc 'Generate assets and move them to idea artifact'
  task 'assets:artifact' => %w(client:assets) do
    target = _(:artifacts, 'fgis')
    mkdir_p target
    project('client').assets.paths.each do |asset|
      cp_r Dir["#{asset}/*"], "#{target}/"
    end
  end

  ipr.version = '13'

  iml.add_web_facet
  iml.add_gwt_facet({'fgis.FgisDev' => true,
                     'fgis.Fgis' => false},
                    :settings => {:compilerMaxHeapSize => "1024",
                                  :additionalCompilerParameters => '-Dgwt.usearchives=false -Dgwt.persistentunitcache=false'})

  iml.add_ejb_facet
  iml.add_jpa_facet

  ipr.add_exploded_war_artifact(project,
                                :name => 'fgis',
                                :build_on_make => true,
                                :enable_ejb => true,
                                :enable_gwt => true,
                                :enable_jpa => true,
                                :enable_web => true,
                                :dependencies => [project] + INCLUDED_DEPENDENCIES)
end

task('domgen:all').enhance(%w(fgis:assets))

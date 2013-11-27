require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'
require 'buildr/single_intermediate_layout'


desc 'FGIS: Fire Ground Information System'
define 'fgis' do
  project.group = 'org.realityforge.fgis'

  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  desc 'FGIS Client Code'
  define 'client' do
    add_bootstrap_media(project)
    add_leaflet_media(project)
    define_process_sass_dir(project)
    define_coffee_script_dir(project)
    define_jquery_dir(project)

    compile.with :gwt_openlayers,
                 :gwt_user,
                 :gwt_dev,
                 :gwt_gin,
                 :mgwt,
                 :javax_inject,
                 :javax_validation,
                 :javax_validation_sources,
                 :javax_annotation,
                 :findbugs_annotations,
                 :google_guice,
                 :aopalliance,
                 :google_guice_assistedinject


    package :jar
    package :sources

    iml.add_web_facet
    iml.add_gwt_facet({'fgis.FgisDev' => true,
                       'fgis.Fgis' => false},
                      :settings => {:compilerMaxHeapSize => "1024",
                                    :additionalCompilerParameters => '-Dgwt.usearchives=false -Dgwt.persistentunitcache=false'})

  end

  desc 'FGIS Server Code'
  define 'server' do
    Domgen::GenerateTask.new(:FGIS,
                             "server",
                             [:ee],
                             _(:target, :generated, "domgen"),
                             project)

    compile.with :javax_persistence,
                 :javax_transaction,
                 :javax_inject,
                 :rest_field_filter,
                 :geolatte_geom_eclipselink,
                 :javax_json,
                 :slf4j_api,
                 :slf4j_jdk14,
                 :mgwt,
                 :jeo,
                 :jts,
                 :geolatte_geom,
                 :ejb_api,
                 :javaee_api,
                 :javax_validation,
                 :javax_annotation,
                 :jackson_core,
                 :jackson_mapper,
                 :javax_validation

    test.using :testng

    package :jar

    iml.add_ejb_facet
    iml.add_jpa_facet
  end

  desc 'FGIS Web Archive'
  define 'web' do

    gwt(["fgis.Fgis"],
        :dependencies => [project('client').compile.dependencies,
                          # The following picks up both the jar and sources
                          # packages deliberately. It is needed for the
                          # generators to access classes in annotations.
                          project('client'),
                          :javax_annotation,
                          # Validation needed to quieten warnings from gwt compiler
                          :javax_validation,
                          :javax_validation_sources],
        :java_args => ["-Xms512M", "-Xmx1024M", "-XX:PermSize=128M", "-XX:MaxPermSize=256M"],
        :draft_compile => (ENV["FAST_GWT"] == 'true'),
        #:log_level => 'ALL',
        # Closure compiler seems to result in an error in GWT/GIN code. Unknown reason
        :enable_closure_compiler => false)

    package(:war).tap do |war|
      project('client').assets.paths.each do |asset|
        war.tap do |w| w.enhance([asset]) end
        war.include asset, :as => '.'
      end
      war.with :libs => artifacts(:rest_field_filter, :geolatte_geom_eclipselink, :javax_json, :jts, :mgwt, :geolatte_geom, project('server'), :gwt_openlayers_server)
    end

    iml.add_web_facet
  end

  project.clean { rm_rf _("database/generated") }
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

  ipr.add_exploded_war_artifact(project,
                                :name => 'fgis',
                                :build_on_make => true,
                                :enable_ejb => true,
                                :enable_gwt => true,
                                :enable_jpa => true,
                                :gwt_module_names => [project('client').iml.id],
                                :war_module_names => [project('client').iml.id, project('web').iml.id],
                                :ejb_module_names => [project('server').iml.id],
                                :jpa_module_names => [project('server').iml.id],
                                :dependencies => [project,
                                                  project('server'),
                                                  :rest_field_filter,
                                                  :geolatte_geom_eclipselink,
                                                  :javax_json,
                                                  :jts,
                                                  :jeo,
                                                  :geolatte_geom])
end

task('domgen:all').enhance(%w(fgis:client:assets))

require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'
require 'buildr/single_intermediate_layout'

JEE_GWT_JARS = [:javax_inject, :javax_jsr305, :javax_validation, :javax_validation_sources]
GIN_JARS = [:gwt_gin, :google_guice, :aopalliance, :google_guice_assistedinject]
APPCACHE_GWT_JARS = [:gwt_appcache_client, :gwt_appcache_linker]
GWT_JARS = JEE_GWT_JARS + GIN_JARS + [:gwt_user, :gwt_dev] + APPCACHE_GWT_JARS + [:g_leaflet_draw, :g_leaflet]
JEE_JARS = [:javax_javaee, :javax_javaee_endorsed, :javax_jsr305]
GEO_DEPS = [:jeo, :geolatte_geom_eclipselink, :geolatte_geom, :jts, :slf4j_api, :slf4j_jdk14]
JACKSON_DEPS = [:jackson_core, :jackson_mapper]
PROVIDED_DEPS = JACKSON_DEPS + JEE_JARS
INCLUDED_DEPENDENCIES = [:rest_field_filter, :javax_json, :gwt_appcache_server, :gwt_cache_filter] + GEO_DEPS

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

  gwt_dir = gwt(["fgis.Fgis"],
      :java_args => ["-Xms512M", "-Xmx1024M", "-XX:PermSize=128M", "-XX:MaxPermSize=256M"],
      :draft_compile => (ENV["FAST_GWT"] == 'true'))

  test.using :testng

  package(:war).tap do |war|
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

  # Hacke to remove GWT from path
  webroots = {}
  webroots[_(:source, :main, :webapp)] = "/" if File.exist?(_(:source, :main, :webapp))
  assets.paths.each { |path| webroots[path.to_s] = "/" if path.to_s != gwt_dir.to_s }
  iml.add_web_facet(:webroots => webroots)
  iml.add_gwt_facet({'fgis.FgisDev' => true, 'fgis.Fgis' => false}, :settings => {:compilerMaxHeapSize => "1024"})
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

require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'
require 'buildr/single_intermediate_layout'

download(artifact(:postgis_jdbc) => 'https://github.com/realityforge/repository/raw/master/org/postgis/postgis-jdbc/2.0.2SVN/postgis-jdbc-2.0.2SVN.jar')

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

    desc 'Generate assets and move them to idea artifact'
    task 'assets:artifact' => %w(assets) do
      target = _(:artifacts, project.name)
      mkdir_p target
      ([_(:source, :main, :webapp)] + self.assets.paths).each do |asset|
        cp_r Dir["#{asset}/*"], "#{target}/"
      end
    end
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
                 :eclipselink,
                 :javax_json,
                 :postgresql,
                 :postgis_jdbc,
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
    package(:war).tap do |war|
      project('client').assets.paths.each do |asset|
        war.tap do |war| war.enhance([asset]) end
        war.include asset, :as => '.'
      end
      war.with :libs => artifacts(:javax_json, :jts, :geolatte_geom, project('server'))
    end
    iml.add_web_facet(:webroots => [_(:source, :main, :webapp)] + project('client').assets.paths)
  end

  project.clean { rm_rf _("databases/generated") }
  project.clean { rm_rf _(:artifacts) }

  ipr.add_exploded_war_artifact(project,
                                :name => 'fgis',
                                :build_on_make => true,
                                :enable_ejb => true,
                                :enable_jpa => true,
                                :war_module_names => [project('web').iml.id],
                                :ejb_module_names => [project('server').iml.id],
                                :jpa_module_names => [project('server').iml.id],
                                :dependencies => [project,
                                                  project('server'),
                                                  :javax_json,
                                                  :jts,
                                                  :geolatte_geom])
end

task('domgen:all').enhance(%w(fgis:client:assets))

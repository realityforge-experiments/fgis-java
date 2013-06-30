def workspace_dir
  File.expand_path(File.dirname(__FILE__) + '/..')
end

$LOAD_PATH.insert(0, "#{workspace_dir}/vendor/plugins/dbt/lib")

require 'dbt'

Dbt::Config.environment = ENV['DB_ENV'] if ENV['DB_ENV']

Dbt::Config.driver = 'Pg'
Dbt::Config.config_filename = File.expand_path("#{workspace_dir}/config/database.yml")

def define_dbt_tasks(project)
  Dbt.database_for_key(:default).version = project.version
end

Dbt.add_database(:default,
                 :migrations => true,
                 :backup => true,
                 :restore => true) do |database|
  database.search_dirs = ["#{workspace_dir}/databases/generated", "#{workspace_dir}/databases"]
  database.enable_domgen(:FGIS, 'domgen:load', 'domgen:sql')
  database.import_assert_filters = true
  database.import_task_as_part_of_create = false
  database.separate_import_task = true
end

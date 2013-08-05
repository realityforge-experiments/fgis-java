workspace_dir = File.expand_path(File.dirname(__FILE__) + "/..")

$LOAD_PATH.unshift(File.expand_path("#{workspace_dir}/vendor/plugins/domgen/lib"))

require 'domgen'

Domgen::LoadSchema.new("#{workspace_dir}/architecture.rb")
#Domgen::Sql.dialect = Domgen::Sql::PgDialect
#sql_type = :pgsql
sql_type = :mssql

Domgen::GenerateTask.new(:FGIS, "sql", [sql_type], "#{workspace_dir}/databases/generated") do |t|
  t.verbose = !!ENV['DEBUG_DOMGEN']
end

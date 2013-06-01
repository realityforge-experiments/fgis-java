workspace_dir = File.expand_path(File.dirname(__FILE__) + "/..")

$LOAD_PATH.unshift(File.expand_path("#{workspace_dir}/../../domgen/lib"))

require 'domgen'

Domgen::LoadSchema.new("#{workspace_dir}/architecture.rb")
Domgen::Sql.dialect = Domgen::Sql::PgDialect

Domgen::GenerateTask.new(:FGIS, "sql", [:pgsql], "#{workspace_dir}/databases/generated") do |t|
  t.verbose = !!ENV['DEBUG_DOMGEN']
end
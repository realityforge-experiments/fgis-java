def define_coffee_script_dir(project)
  target_dir = project._(:target, :generated, "coffee/main/webapp")
  source_dir = project._(:source, :main, :assets)
  task 'process_coffee_script' do
    sh "coffee --bare --compile --output #{target_dir} #{source_dir}"
  end
  project.file(target_dir => %w(process_coffee_script))
  project.resources do
    task('process_coffee_script').invoke
  end

  target_dir
end

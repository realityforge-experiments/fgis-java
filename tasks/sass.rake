def define_process_sass_dir(project)
  target_dir = project._(:target, :generated, "sass/main/webapp")
  source_dir = project._(:source, :main, :assets)
  task 'process_sass' do
    sh "scss -q --update #{source_dir}:#{target_dir}"
  end
  project.resources do
    task('process_sass').invoke
  end

  target_dir
end

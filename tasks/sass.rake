def define_process_sass_dir(project)
  target_dir = project._(:target, :generated, "sass/main/webapp")
  source_dir = project._(:source, :main, :assets)
  project.file(target_dir => [FileList["#{source_dir}/**/*.scss"]]) do
    sh "scss -q --update #{source_dir}:#{target_dir}"
  end
  target_dir
end

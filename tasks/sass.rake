def define_process_sass_dir(project)
  target_dir = project._(:target, :generated, "sass/main/webapp")
  source_dir = project._(:source, :main, :sass)
  project.assets.paths << project.file(target_dir => [FileList["#{source_dir}/**/*.scss"]]) do
    sh "scss -q --update #{source_dir}:#{target_dir}"
    touch target_dir
  end

  p = project
  while p.parent
    p = p.parent
  end
  p.clean { rm_rf p._('.sass-cache') }
  if p.iml?
    p.iml.excluded_directories << p._('.sass-cache')
  end

  target_dir
end

def define_coffee_script_dir(project)
  target_dir = project._(:target, :generated, "coffee/main/webapp")
  source_dir = project._(:source, :main, :assets)

  project.assets.paths << project.file(target_dir => [FileList["#{source_dir}/**/*.coffee"]]) do
    sh "coffee --bare --compile --output #{target_dir} #{source_dir}"
  end
  target_dir
end

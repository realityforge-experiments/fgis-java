def define_coffee_script_dir(project)
  target_dir = project._(:target, :generated, "coffee/main/webapp")
  source_dir = project._(:source, :main, :coffee)

  project.assets.paths << project.file(target_dir => [FileList["#{source_dir}/**/*.coffee"]]) do
    puts "Compiling coffeescript"
    sh "coffee --bare --compile --output #{target_dir} #{source_dir}"
    touch target_dir
  end
  target_dir
end

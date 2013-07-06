def add_bootstrap_media(project)
  target_dir = project._(:target, :generated, "bootstrap/main/webapp")
  project.assets.paths << project.file(target_dir) do
    download_task = download("downloads/bootstrap.zip" => "http://twitter.github.com/bootstrap/assets/bootstrap.zip")
    download_task.invoke
    unzip(target_dir => download_task.name).include('**/*.min.*').include('bootstrap/img/*').extract unless File.exist?(target_dir)
    touch target_dir
  end
  target_dir
end

def add_bootstrap_media(project)
  target_dir = project._(:target, :generated, "bootstrap/main/webapp")
  task 'unzip_bootstrap' do
    download_task = download("downloads/bootstrap.zip" => "http://twitter.github.com/bootstrap/assets/bootstrap.zip")
    download_task.invoke
    unzip(target_dir => download_task.name).include('**/*.min.*').include('bootstrap/img/*').extract unless File.exist?(target_dir)
  end
  project.resources do
    task('unzip_bootstrap').invoke
  end

  target_dir
end

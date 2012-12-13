def add_leaflet_media(project)
  target_dir = project._(:target, :generated, "leaflet/main/webapp")
  project.file(target_dir) do
    download_task = download("downloads/leaflet.zip" => "https://github.com/CloudMade/Leaflet/zipball/v0.4.5")
    download_task.invoke
    leaflet_subdir = "#{target_dir}/leaflet"
    unless File.exist?(leaflet_subdir)
      unzip_task = unzip(leaflet_subdir => download_task.name)
      unzip_task.from_path('CloudMade-Leaflet-165e50f/dist')
      unzip_task.extract
    end
  end
  target_dir
end

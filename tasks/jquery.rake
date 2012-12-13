def define_jquery_dir(project)
  target_dir = project._(:target, :generated, "jquery/main/webapp")
  task 'unzip_jquery' do
    download_task = download("downloads/jquery.min.js" => "http://code.jquery.com/jquery-1.8.3.min.js")
    download_task.invoke
    base_dir = "#{target_dir}/jquery/js"
    mkdir_p base_dir
    cp download_task.name, "#{base_dir}/jquery.js"
  end
  project.resources do
    task('unzip_jquery').invoke
  end

  target_dir
end

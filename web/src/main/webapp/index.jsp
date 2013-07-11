<!DOCTYPE html>
<html>
<head>
  <title>FGIS</title>
  <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">
 <style>
      body {
        padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
      }
    </style>
  <link href="bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet">
</head>
<body>
    <div class="navbar navbar-inverse navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="brand" href="#">FGIS</a>
        </div>
      </div>
    </div>

    <div class="container">

      <h1>FGIS Sites</h1>

      <ul>
        <li><a class="brand" href="original.jsp">Original Site</a></li>
        <li><a class="brand" href="gwt.jsp">GWT Open Layers Test Site</a></li>
      </ul>

    </div> <!-- /container -->
    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <script src="jquery/js/jquery.js"></script>
    <script src="bootstrap/js/bootstrap.min.js"></script>
    <script src="leaflet/leaflet.js"></script>
    <script src="javascripts/simple.js"></script>
</body>
</html>
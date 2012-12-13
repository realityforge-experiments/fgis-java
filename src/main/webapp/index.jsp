<!DOCTYPE html>
<html>
<head>
  <title>FGIS</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="apple-mobile-web-app-status-bar-style" content="black"/>

  <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">
  <link href="bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet">

  <link rel="stylesheet" href="leaflet/leaflet.css"/>
  <!--[if lte IE 8]>
  <link rel="stylesheet" href="leaflet/leaflet.ie.css"/>
  <![endif]-->
  <link href="stylesheets/simple.css" rel="stylesheet">
</head>
<body>
<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <a class="brand" href="#">FGIS</a>
    <ul class="nav pull-right">
      <li class="show"><a href="#" class="connection-status connected">Connected <i
        class="icon-circle-arrow-up"></i></a></li>
      <li class="hidden"><a href="#" class="connection-status disconnected">Disconnected <i
        class="icon-circle-arrow-down"></i></a></li>
    </ul>
    <ul class="nav">
      <li class="active"><a href="#">Live Feed</a></li>
      <li><a href="team">My Team</a></li>
      <li><a href="analyzer">Map Analyzer</a></li>
      <li><a href="recon">Recon</a></li>
    </ul>
  </div>
</div>
<div class="main container-fluid" style="height:100%;">
  <div class="row-fluid">
    <ul class="breadcrumb">
      <li class="sub-link active" data-view="overview">Overview</li>
      <li class="sub-link" data-view="updates">Updates</li>
    </ul>
  </div>
  <div class="row-fluid" style="height:100%;">
    <div class="span4" style="height:100%;">
      <div class="hud">
        <div class="datetime clearfix">
          <em class="time">16:41</em>

          <div class="date">Thursday<br>22<sup>nd</sup> Dec</div>
          <div class="arbitrary-tiny-divider"></div>
        </div>

        <div class="details">
          <div class="team">Marysville Alpha</div>
          <div class="member">Julian Smith</div>
        </div>

        <div class="overview show">
          <div class="row-fluid">
            <p>Local Data</p>
          </div>
          <div class="row-fluid">
            <div class="span6">
              <p>Temp</p>

              <p>31c</p>
            </div>
            <div class="span6">
              <p>Humidity</p>

              <p>27%</p>
            </div>
          </div>
          <div class="row-fluid">
            <p>Sector Data</p>
          </div>
          <div class="row-fluid">
            <div class="span6">
              <p>Temp</p>

              <p>29c</p>
            </div>
            <div class="span6">
              <p>Humidity</p>

              <p>37%</p>
            </div>
          </div>
          <div class="row-fluid">
            <div class="span6">
              <p>Avg Wind Speed</p>

              <p>25k/h NW</p>
            </div>
            <div class="span6">
              <p>Gust Wind Spd</p>

              <p>55km/h</p>
            </div>
          </div>
        </div>
        <div class="updates hide" style="height: 100%;">
          <div class="row-fluid" style="height: 10%;">
            <div class="span12">
              <p>Updates From:</p>

              <div class="btn-group" id="updates-group" data-toggle="buttons-radio">
                <button class="btn btn-small active" id="updates_all">All</button>
                <button class="btn btn-small" id="updates_sector">Sector</button>
                <button class="btn btn-small" id="updates_team">Team</button>
                <button class="btn btn-small" id="updates_you">You</button>
              </div>
            </div>
          </div>
          <div class="row-fluid">
            <div class="span12">
              <div class="messages"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="span8" style="height:105%;">
      <div id="map"></div>
    </div>
    <div id="status">
      <em>Warning:</em> Winds due to increase to 95km/hr over the next hour.
      <div>
      </div>
    </div>
    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <script src="jquery/js/jquery.js"></script>
    <script src="bootstrap/js/bootstrap.min.js"></script>
    <script src="leaflet/leaflet.js"></script>
    <script src="javascripts/bootstrap.js"></script>
    <script src="javascripts/simple.js"></script>
</body>
</html>
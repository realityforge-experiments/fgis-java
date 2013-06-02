TopLayers = {}

configureFeedBehaviour = ->
  $('ul.breadcrumb li.sub-link').click ->
    selected_view = $(this).data "view"
    current_view = $('ul.breadcrumb li.active').data "view"
    $('ul.breadcrumb li.active').removeClass "active"
    $(this).addClass "active"
    $('div.hud .' + selected_view).toggle()
    $('div.hud .' + current_view).toggle()

  $('button#updates_all').click ->
    $('div.message').hide()
    $('div.message').fadeIn('fast')

  $('button#updates_sector').click ->
    $('div.message').hide()
    $('div.message[data-type="SECTOR"]').fadeIn('fast')

  $('button#updates_team').click ->
    $('div.message').hide()
    $('div.message[data-type="TEAM"]').fadeIn('fast')

  $('button#updates_you').click ->
    $('div.message').hide()
    $('div.message[data-type="YOU"]').fadeIn('fast')

addFeedItem = (time_diff, map, v) ->
  html = "<div class=\"message\" data-type=\"#{v.type}\">
            <i class=\"icon-chevron-right pull-right\" style=\"margin: 15px 10px;\"></i>
            <p class=\"pull-right\">#{time_diff}</p>
              <p>#{v.title}</p>
            <p>#{v.description}</p>
          </div>"

  $('div.messages').append($(html).data('geo', v.geo))

  $('div.message').click ->
    geoList = $(this).data('geo').features
    geoList.forEach (geo) ->
      switch geo.geometry.type
        when "Point" then map.panTo [geo.geometry.coordinates[1], geo.geometry.coordinates[0]]
        when "LineString" then map.panTo [geo.geometry.coordinates[0][1], geo.geometry.coordinates[0][0]]
        when "Polygon" then map.panTo [geo.geometry.coordinates[0][0][1], geo.geometry.coordinates[0][0][0]]

handleGeoJsonPacket = (map, v) ->
  console.log v
  color = 'green'
  if( v.type == 'Person' )
    color = 'blue'
  else if( v.type == 'Slip-On' )
    color = 'yellow'
  else if( v.type == 'Tanker' )
    color = 'red'
  current_time = new Date().getTime()
  feature_time = new Date(v.updated_at).getTime()
  time_diff_in_minutes = Math.ceil((current_time - feature_time)/1000/60)
  time_diff_in_hours = Math.floor((current_time - feature_time)/1000/60/60)
  time_diff_in_days = Math.floor((current_time - feature_time)/1000/60/60/24)
  if time_diff_in_days > 0
    time_diff = time_diff_in_days + " days ago"
  else
    time_diff = time_diff_in_hours + " hours ago"
    if time_diff_in_hours == 0
      time_diff = time_diff_in_minutes + " minutes ago"
  layer = L.geoJson(v.geo.features[0], {
    pointToLayer: (feature, latlon) ->
      markerIcon = L.icon
        iconUrl: 'images/marker-icon.png'
        shadowUrl: 'images/marker-shadow.png'
      L.marker(latlon, {icon: markerIcon}).addTo(map);
    style: (feature) ->
      return {color: color};
    onEachFeature: (feature, layer) ->
      layer.bindPopup v.type + " - " + v.title + "<br><span style=\"float: right; font-size: 0.8em;\">(#{time_diff})</span>"
  })
  if( !TopLayers[v.id] )
    TopLayers[v.id] = []
  TopLayers[v.id].unshift(layer)
  layer.addTo(map)
  if TopLayers[v.id].length > 3
    l = TopLayers[v.id].pop()
    map.removeLayer(l);

  addFeedItem(time_diff, map, v)

downloadResourceData = (map) ->
  handleDownloadCallback = (response) ->
    response.forEach (v) ->
      handleGeoJsonPacket(map, v)
  $.get '/fgis/api/resource', {}, handleDownloadCallback, 'json'

initMap = (lat, long) ->
  map = L.map('map').setView([lat,long], 14)
  L.tileLayer('http://{s}.tile.cloudmade.com/aeb94991e883413e8262bd55def34111/997/256/{z}/{x}/{y}.png',{
    attribution: 'Made with love at <a href="https://github.com/rhok-melbourne/fgis/">RHoK Melbourne</a>, Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>',
    maxZoom: 18
  }).addTo(map)

  downloadResourceData(map)
  intervalDownloadCallback = -> downloadResourceData(map)
  setInterval intervalDownloadCallback, 1000

showMapAtDefault = ->
  initMap(-37.793566209439, 144.94111608134)

showMapAtGeoPosition = (position) ->
  initMap( position.coords.latitude, position.coords.longitude)

getHashParams = ->
  hashParams = {}
  e
  a = /\+/g # Regex for replacing addition symbol with a space
  r = /([^&;=]+)=?([^&;]*)/g
  d = (s) ->
    decodeURIComponent( s.replace(a, " "))
  q = window.location.hash.substring(1)

  while (e = r.exec(q))
     hashParams[d(e[1])] = d(e[2])

  return hashParams

hash_state = getHashParams()
console.log(hash_state)

if navigator && navigator.geolocation
  navigator.geolocation.getCurrentPosition(showMapAtGeoPosition, showMapAtDefault)

  if !hash_state['track'] || 'false' != hash_state['track']
    sendLocationDataToServer = (position) ->
      sentDataSuccessCallback = (response) ->
        console.log "Sent data - hoorah!"
      resource_id = hash_state['resource_id'] || '8'
      $.get '/fgis/api/resource/' + resource_id + '/location?x=' + position.coords.longitude + '&y=' + position.coords.latitude, {}, sentDataSuccessCallback, 'json'
    sendLocationDataToServerCallback = ->
      navigator.geolocation.getCurrentPosition(sendLocationDataToServer)
    setInterval sendLocationDataToServerCallback, 1000
else
  showMapAtDefault

configureFeedBehaviour()

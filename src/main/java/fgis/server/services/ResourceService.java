package fgis.server.services;

import fgis.server.entity.fgis.Resource;
import fgis.server.entity.fgis.ResourceTrack;
import fgis.server.entity.fgis.dao.ResourceRepository;
import fgis.server.entity.fgis.dao.ResourceTrackRepository;
import java.util.Calendar;
import java.util.List;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings( { "UnusedDeclaration", "JavaDoc" } )
@TransactionAttribute( TransactionAttributeType.REQUIRED )
@Startup
@Singleton
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@Path( "/resource" )
@Produces( { MediaType.APPLICATION_JSON } )
@Consumes( { MediaType.APPLICATION_JSON } )
public class ResourceService
{
  @EJB
  private ResourceRepository _resourceService;

  @EJB
  private ResourceTrackRepository _resourceTrackService;

  @GET
  @Path( "/{id}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public String getResource( final @PathParam( "id" ) int resourceID )
    throws JSONException
  {
    final Resource resource = _resourceService.getByID( resourceID );
    final List<ResourceTrack> tracks = getResourceTracks( resourceID );

    return toJson( resource, tracks );
  }

  @GET
  @Path( "/geojson/{id}" )
  public String getResourceAsGeoJson( final @PathParam( "id" ) int resourceID )
    throws JSONException
  {
    final Resource resource = _resourceService.getByID( resourceID );
    final List<ResourceTrack> tracks = getResourceTracks( resourceID );

    return toGeoJson( resource, tracks );
  }

  private List<ResourceTrack> getResourceTracks( final int resourceID )
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.roll( Calendar.MINUTE, -1 );
    return _resourceTrackService.findAllByResourceSince( resourceID, calendar.getTime() );
  }

  private String toGeoJson( final Resource resource, final List<ResourceTrack> tracks )
    throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put( "title", resource.getName() );
    json.put( "description", resource.getName() );
    final JSONObject geoSectionJson = new JSONObject();
    json.put( "geo", geoSectionJson );
    geoSectionJson.put( "type", "FeatureCollection" );
    final JSONArray featuresJson = new JSONArray();
    geoSectionJson.put( "features", featuresJson );

    final JSONObject featureJson = new JSONObject();
    featuresJson.put( featureJson );
    featureJson.put( "type", "Feature" );
    final JSONObject propsJson = new JSONObject();
    featureJson.put( "properties", propsJson );

    propsJson.put( "type", "Trail" );
    propsJson.put( "description", "Trail" );
    propsJson.put( "resource_id", resource.getID() );
    propsJson.put( "color", "blue" );
    propsJson.put( "date_created", tracks.get( 0 ).getCollectedAt() );
    propsJson.put( "date_created", "Sat Dec 01 2012 17:49:08 GMT+1100 (EST)" );

    final JSONObject crsJson = new JSONObject();
    featureJson.put( "crs", crsJson );
    crsJson.put( "type", "name" );
    final JSONObject crsPropertiesJson = new JSONObject();
    crsJson.put( "properties", crsPropertiesJson );
    crsPropertiesJson.put( "name", "urn:ogc:def:crs:OGC:1.3:CRS84" );

    final JSONObject geometryJson = new JSONObject();
    featureJson.put( "geometry", geometryJson );
    geometryJson.put( "type", "LineString" );
    final JSONArray outerCoordinatesJson = new JSONArray();
    final JSONArray coordinatesJson = new JSONArray();
    outerCoordinatesJson.put( coordinatesJson );
    geometryJson.put( "coordinates", coordinatesJson );

    for ( final ResourceTrack track : tracks )
    {
      final JSONArray coordinateJson = new JSONArray();
      coordinatesJson.put( coordinateJson );
      coordinateJson.put( track.getLocation().getX() );
      coordinateJson.put( track.getLocation().getY() );
    }

    return json.toString();
  }

  private String toJson( final Resource resource, final List<ResourceTrack> tracks )
    throws JSONException
  {
    final JSONObject json = new JSONObject();
    final JSONArray jsonPoints = new JSONArray();
    json.put( "id", resource.getID() );
    json.put( "name", resource.getName() );
    json.put( "points", jsonPoints );

    int index = 0;
    for ( final ResourceTrack track : tracks )
    {
      final JSONObject jsonSector = new JSONObject();
      jsonSector.put( "id", track.getID() );
      jsonSector.put( "collected_at", track.getCollectedAt().toString() );
      jsonSector.put( "location", track.getLocation().asText() );
      jsonPoints.put( index++, jsonSector );
    }
    return json.toString();
  }
}

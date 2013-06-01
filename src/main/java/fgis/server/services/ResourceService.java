package fgis.server.services;

import fgis.server.entity.fgis.Resource;
import fgis.server.entity.fgis.ResourceTrack;
import fgis.server.entity.fgis.dao.ResourceRepository;
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

@SuppressWarnings({ "UnusedDeclaration", "JavaDoc" })
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Path("/resource")
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public class ResourceService
{
  @EJB
  private ResourceRepository _service;

  @GET
  @Path( "/{id}" )
  public String getResource( final @PathParam( "id" ) int resourceID )
    throws JSONException
  {
    final Resource resource = _service.getByID( resourceID );

    final JSONObject json = new JSONObject();
    final JSONArray jsonSectors = new JSONArray();
    json.put( "id", resource.getID() );
    json.put( "name", resource.getName() );
    json.put( "points", jsonSectors );

    int index = 0;
    for ( final ResourceTrack track : resource.getResourceTracks() )
    {
      final JSONObject jsonSector = new JSONObject();
      jsonSector.put( "id", track.getID() );
      jsonSector.put( "collected_at", track.getCollectedAt().toString() );
      jsonSector.put( "location", track.getLocation().asText() );
      jsonSectors.put( index++, jsonSector );
    }
    return json.toString();
  }
}

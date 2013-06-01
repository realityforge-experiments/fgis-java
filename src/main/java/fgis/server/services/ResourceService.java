package fgis.server.services;

import fgis.server.entity.fgis.Resource;
import fgis.server.entity.fgis.ResourceTrack;
import fgis.server.entity.fgis.dao.ResourceRepository;
import fgis.server.entity.fgis.dao.ResourceTrackRepository;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.glassfish.json.JsonGeneratorFactoryImpl;

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
  private ResourceRepository _resourceService;

  @EJB
  private ResourceTrackRepository _resourceTrackService;

  @GET
  @Path("/{id}")
  @Produces({ MediaType.APPLICATION_JSON })
  public String getResource( final @PathParam("id") int resourceID )
  {
    final Resource resource = _resourceService.findByID( resourceID );
    if ( null == resource )
    {
      throw new WebApplicationException( ResponseUtil.entityNotFoundResponse() );
    }

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
  {
    final JsonGeneratorFactory factory = new JsonGeneratorFactoryImpl();
    final StringWriter writer = new StringWriter();
    final JsonGenerator generator = factory.createGenerator( writer );

    generator.writeStartObject().
      write( "title", resource.getName() ).
      write( "description", resource.getName() ).
      writeStartObject( "geo" ).
        write( "type", "FeatureCollection" ).
        writeStartArray( "features" ).
          writeStartObject().
            write( "type", "Feature" ).
            writeStartObject( "properties" ).
              write( "type", resource.getName() + "'s Trail" ).
              write( "description", resource.getName() + "'s Trail" ).
              write( "color", "blue" ).
              write( "date_created", "Sat Dec 01 2012 17:49:08 GMT+1100 (EST)" ). // tracks.get( 0 ).getCollectedAt()
            writeEnd().
            writeStartObject( "geometry" ).
              write( "type", "LineString" ).
              writeStartArray( "coordinates" );

    for ( final ResourceTrack track : tracks )
    {
      generator.writeStartArray().write( track.getLocation().getX() ).write( track.getLocation().getY() ).writeEnd();
    }

    generator.
              writeEnd().
            writeEnd().
            writeStartObject( "crs" ).
              write( "type", "name" ).
              writeStartObject( "properties" ).
                write( "name", "urn:ogc:def:crs:OGC:1.3:CRS84" ).
              writeEnd().
            writeEnd().
          writeEnd().
        writeEnd().
      writeEnd().
    writeEnd().
    close();

    return writer.toString();
  }
}

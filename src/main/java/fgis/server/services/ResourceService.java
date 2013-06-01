package fgis.server.services;

import fgis.server.entity.fgis.Resource;
import fgis.server.entity.fgis.ResourceTrack;
import fgis.server.entity.fgis.dao.ResourceRepository;
import fgis.server.entity.fgis.dao.ResourceTrackRepository;
import fgis.server.support.FieldFilter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import javax.annotation.Nullable;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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
  @Produces({ MediaType.APPLICATION_JSON })
  public String getResources( @PathParam("types") final int resourceID,
                             @QueryParam( "fields" ) @Nullable final String fields )
    throws ParseException
  {
    final FieldFilter filter = FieldFilter.parse( fields );
    final Resource resource = _resourceService.findByID( resourceID );
    if ( null == resource )
    {
      throw new WebApplicationException( ResponseUtil.entityNotFoundResponse() );
    }

    final List<ResourceTrack> tracks = getResourceTracks( resourceID );


    return toGeoJson( filter, resource, tracks );
  }

  @GET
  @Path("/{id}")
  @Produces({ MediaType.APPLICATION_JSON })
  public String getResource( @PathParam("id") final int resourceID,
                             @QueryParam( "fields" ) @Nullable final String fields )
    throws ParseException
  {
    final FieldFilter filter = FieldFilter.parse( fields );

    final Resource resource = _resourceService.findByID( resourceID );
    if ( null == resource )
    {
      throw new WebApplicationException( ResponseUtil.entityNotFoundResponse() );
    }

    final List<ResourceTrack> tracks = getResourceTracks( resourceID );
    return toGeoJson( filter, resource, tracks );
  }

  private List<ResourceTrack> getResourceTracks( final int resourceID )
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.roll( Calendar.MINUTE, -1 );
    return _resourceTrackService.findAllByResourceSince( resourceID, calendar.getTime() );
  }

  private String toGeoJson( final FieldFilter filter,
                            final Resource resource,
                            final List<ResourceTrack> tracks )
  {
    final JsonGeneratorFactory factory = new JsonGeneratorFactoryImpl();
    final StringWriter writer = new StringWriter();
    final JsonGenerator g = factory.createGenerator( writer );

    g.writeStartObject();

    if( filter.allow( "title" ) )
    {
      g.write( "title", resource.getName() );
    }

    if( filter.allow( "description" ) )
    {
      g.write( "description", resource.getName() );
    }

    if( filter.allow( "geo" ) )
    {
      g.writeStartObject( "geo" ).
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
        writePoint( g, track.getLocation().getX(), track.getLocation().getY() );
      }

      g.
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
      writeEnd();
    }
    g.writeEnd().close();

    return writer.toString();
  }

  private void writePoint( final JsonGenerator g, final double x, final double y )
  {
    g.writeStartArray().write( x ).write( y ).writeEnd();
  }
}

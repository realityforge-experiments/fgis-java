package fgis.server.services;

import fgis.server.entity.fgis.Resource;
import fgis.server.entity.fgis.ResourceTrack;
import fgis.server.entity.fgis.Resource_;
import fgis.server.entity.fgis.dao.ResourceRepository;
import fgis.server.entity.fgis.dao.ResourceTrackRepository;
import fgis.server.support.FieldFilter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.PointSequenceBuilder;
import org.geolatte.geom.PointSequenceBuilders;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.crs.CrsId;
import org.glassfish.json.JsonGeneratorFactoryImpl;

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
  @PersistenceContext( unitName = "FGIS" )
  private EntityManager _em;

  @EJB
  private ResourceRepository _resourceService;

  @EJB
  private ResourceTrackRepository _resourceTrackService;

  @GET
  @Produces( { MediaType.APPLICATION_JSON } )
  public String getResources( @QueryParam( "types" ) @Nullable final String types,
                              @QueryParam( "fields" ) @Nullable final String fields,
                              @QueryParam( "bbox" ) @Nullable final String bbox,
                              @QueryParam( "offset" ) @DefaultValue( "0" ) final int offset,
                              @QueryParam( "limit" ) @DefaultValue( "50" ) final int limit )
    throws ParseException
  {
    final FieldFilter filter = FieldFilter.parse( fields );

    final CriteriaBuilder b = _em.getCriteriaBuilder();
    final CriteriaQuery<Resource> query = b.createQuery( Resource.class );
    final Root<Resource> entity = query.from( Resource.class );
    query.select( entity );
    final ArrayList<Predicate> predicates = new ArrayList<Predicate>();
    final Map<String, Object> params = new HashMap<String, Object>();

    if ( null != bbox )
    {
      final Polygon extents = parseBBox( bbox );
      final ParameterExpression<String> extent = b.parameter( String.class, "Extent" );

      predicates.add( b.isTrue( b.function( "ST_Intersects",
                                            Boolean.class,
                                            b.function( "ST_GeomFromText",
                                                        Geometry.class,
                                                        extent ),
                                            entity.get( Resource_.Location ) ) ) );
      params.put( "Extent", extents.asText() );
    }

    if ( null != types )
    {
      predicates.add( entity.get( Resource_.Type ).in( (Object[]) types.split( "," ) ) );
    }

    query.where( b.and( predicates.toArray( new Predicate[ predicates.size() ] ) ) );

    final TypedQuery<Resource> typedQuery = _em.createQuery( query );
    typedQuery.setFirstResult( offset );
    typedQuery.setMaxResults( limit );
    for ( final Entry<String, Object> entry : params.entrySet() )
    {
      typedQuery.setParameter( entry.getKey(), entry.getValue() );
    }
    final List<Resource> resources = typedQuery.getResultList();

    final ArrayList<ResourceEntry> entries = new ArrayList<>( resources.size() );
    for ( final Resource resource : resources )
    {
      final List<ResourceTrack> tracks = getResourceTracks( resource.getID() );
      entries.add( new ResourceEntry( resource, tracks ) );
    }

    return toGeoJson( filter, entries );
  }

  private Polygon parseBBox( final String bbox )
    throws ParseException
  {
    final String[] points = bbox.split( "," );
    if ( 4 != points.length )
    {
      throw new ParseException( "Unable to split 4 points", 0 );
    }
    final double x1 = Double.parseDouble( points[ 0 ] );
    final double y1 = Double.parseDouble( points[ 1 ] );
    final double x2 = Double.parseDouble( points[ 2 ] );
    final double y2 = Double.parseDouble( points[ 3 ] );

    final PointSequenceBuilder builder =
      PointSequenceBuilders.variableSized( DimensionalFlag.d2D, CrsId.UNDEFINED );
    builder.add( x1, y1 );
    builder.add( x2, y1 );
    builder.add( x2, y2 );
    builder.add( x1, y2 );
    builder.add( x1, y1 );

    return new Polygon( builder.toPointSequence() );
  }

  @GET
  @Path( "/{id}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public String getResource( @PathParam( "id" ) final int resourceID,
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
    return toGeoJson( filter, new ResourceEntry( resource, tracks ) );
  }

  private List<ResourceTrack> getResourceTracks( final int resourceID )
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.roll( Calendar.MINUTE, -1 );
    return _resourceTrackService.findAllByResourceSince( resourceID, calendar.getTime() );
  }

  private String toGeoJson( final FieldFilter filter,
                            final ResourceEntry resource )
  {
    final StringWriter writer = new StringWriter();
    final JsonGenerator g = newGenerator( writer );

    writeResource( g, filter, resource );

    g.close();

    return writer.toString();
  }

  private String toGeoJson( final FieldFilter filter,
                            final List<ResourceEntry> entries )
  {
    final StringWriter writer = new StringWriter();
    final JsonGenerator g = newGenerator( writer );

    g.writeStartArray();
    for ( final ResourceEntry entry : entries )
    {
      writeResource( g, filter, entry );
    }
    g.writeEnd();

    g.close();

    return writer.toString();
  }

  private JsonGenerator newGenerator( final StringWriter writer )
  {
    final JsonGeneratorFactory factory = new JsonGeneratorFactoryImpl();
    return factory.createGenerator( writer );
  }

  private void writeResource( final JsonGenerator g,
                              final FieldFilter filter,
                              final ResourceEntry entry )
  {
    final Resource resource = entry.getResource();
    final List<ResourceTrack> tracks = entry.getTracks();

    g.writeStartObject();

    if ( filter.allow( "id" ) )
    {
      g.write( "id", resource.getID() );
    }

    if ( filter.allow( "type" ) )
    {
      g.write( "type", resource.getType() );
    }

    if ( filter.allow( "title" ) )
    {
      g.write( "title", resource.getName() );
    }

    if ( filter.allow( "description" ) )
    {
      g.write( "description", resource.getName() );
    }

    if ( filter.allow( "geo" ) )
    {
      g.writeStartObject( "geo" ).
        write( "type", "FeatureCollection" ).
        writeStartArray( "features" ).
          writeStartObject().
            write( "type", "Feature" ).
            writeStartObject( "properties" ).
              write( "date_created", tracks.get( 0 ).getCollectedAt().getTime() ).
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
    g.writeEnd();
  }

  private void writePoint( final JsonGenerator g, final double x, final double y )
  {
    g.writeStartArray().write( x ).write( y ).writeEnd();
  }
}

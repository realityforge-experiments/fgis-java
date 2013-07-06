package fgis.server.services;

import fgis.server.data_type.fgis.LocationUpdateDTO;
import fgis.server.data_type.fgis.ResourceDetailsDTO;
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
import java.util.Date;
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
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;
import org.geolatte.geom.Point;
import org.geolatte.geom.PointSequenceBuilder;
import org.geolatte.geom.PointSequenceBuilders;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.crs.CrsId;
import org.realityforge.jeo.geojson.GeoJsonWriter;
import org.realityforge.jeo.geojson.GjElement;
import org.realityforge.jeo.geojson.GjFeature;
import org.realityforge.jeo.geojson.GjFeatureCollection;
import org.realityforge.jeo.geojson.GjGeometry;

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
    //System.out.println( "getResources(" + types + "," + fields + ")" );
    final FieldFilter filter = FieldFilter.parse( fields );

    final CriteriaBuilder b = _em.getCriteriaBuilder();
    final CriteriaQuery<Resource> query = b.createQuery( Resource.class );
    final Root<Resource> entity = query.from( Resource.class );
    query.select( entity );
    final ArrayList<Predicate> predicates = new ArrayList<>();
    final Map<String, Object> params = new HashMap<>();

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

    return serialize( buildFeatureCollection( filter, entries ) );
  }

  @POST
  @Produces( { MediaType.APPLICATION_JSON } )
  public void createResource( final ResourceDetailsDTO details )
    throws ParseException
  {
    final String name = details.getName();
    Resource resource = _resourceService.findByName( name );
    if ( null != resource )
    {
      throw new WebApplicationException( ResponseUtil.entityNotFoundResponse() );
    }

    _resourceService.persist( new Resource( details.getType(), name ) );
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

    final Resource resource = getResource( resourceID );

    final List<ResourceTrack> tracks = getResourceTracks( resourceID );
    return serialize( buildFeature( filter, new ResourceEntry( resource, tracks ) ) );
  }

  @GET
  @Path( "/{id}/location" )
  @Consumes( { MediaType.APPLICATION_JSON } )
  public String updateResourceLocation( @PathParam( "id" ) final int resourceID,
                                        @QueryParam( "x" ) final double x,
                                        @QueryParam( "y" ) final double y )
    throws ParseException
  {
    System.out.println( "updateResourceLocation(" + resourceID + "," + x + "," + y + ")" );
    updateResourceLocation( getResource( resourceID ), toPoint( x, y ) );
    return "{\"result\":\"OK\"}";
  }

  @POST
  @Path( "/{id}/location" )
  @Consumes( { MediaType.APPLICATION_JSON } )
  public void updateResourceLocation( @PathParam( "id" ) final int resourceID, final LocationUpdateDTO update )
    throws ParseException
  {
    updateResourceLocation( getResource( resourceID ),
                            toPoint( update.getCoordinatess().get( 0 ), update.getCoordinatess().get( 1 ) ) );
  }

  private Resource getResource( final int resourceID )
  {
    final Resource resource = _resourceService.findByID( resourceID );
    if ( null == resource )
    {
      throw new WebApplicationException( ResponseUtil.entityNotFoundResponse() );
    }
    return resource;
  }

  private Point toPoint( final double latPosition, final double longPosition )
  {
    final PointSequenceBuilder builder =
      PointSequenceBuilders.variableSized( DimensionalFlag.d2D, CrsId.UNDEFINED );
    builder.add( latPosition, longPosition );
    return new Point( builder.toPointSequence() );
  }

  private void updateResourceLocation( final Resource resource, final Point point )
  {
    _resourceTrackService.persist( new ResourceTrack( resource, new Date(), point ) );
    resource.setLocation( point );
  }

  private List<ResourceTrack> getResourceTracks( final int resourceID )
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.roll( Calendar.MINUTE, -1 );
    return _resourceTrackService.findAllByResourceSince( resourceID, calendar.getTime() );
  }

  private GjFeature buildFeature( final FieldFilter filter, final ResourceEntry entry )
  {
    final Resource resource = entry.getResource();
    final List<ResourceTrack> tracks = entry.getTracks();

    final JsonObjectBuilder b = Json.createObjectBuilder();

    if ( filter.allow( "resource_type" ) )
    {
      b.add( "resource_type", resource.getType() );
    }

    if ( filter.allow( "title" ) )
    {
      b.add( "title", resource.getName() );
    }

    if ( filter.allow( "description" ) )
    {
      b.add( "description", resource.getName() );
    }

    if ( filter.allow( "updated_at" ) && tracks.size() > 0 )
    {
      final long updatedAt;
      if ( tracks.size() > 0 )
      {
        updatedAt = tracks.get( 0 ).getCollectedAt().getTime();
      }
      else
      {
        updatedAt = 0;
      }
      b.add( "updated_at", updatedAt );
    }

    final JsonValue id = Json.createObjectBuilder().add( "id", resource.getID() ).build().get( "id" );
    final CrsId crsId = tracks.size() > 0 ? tracks.get( 0 ).getLocation().getCrsId() : CrsId.UNDEFINED;
    final DimensionalFlag dimensionalFlag =
      tracks.size() > 0 ? tracks.get( 0 ).getLocation().getDimensionalFlag() : DimensionalFlag.d2D;

    final PointSequenceBuilder builder =
      PointSequenceBuilders.fixedSized( tracks.size(), dimensionalFlag, crsId );
    for ( final ResourceTrack track : tracks )
    {
      builder.add( track.getLocation().getX(), track.getLocation().getY() );
    }
    final LineString lineString = new LineString( builder.toPointSequence() );

    return new GjFeature( id, new GjGeometry( lineString, null, null, null ), crsId, null, b.build() );
  }

  private GjFeatureCollection buildFeatureCollection( final FieldFilter filter, final List<ResourceEntry> entries )
  {
    final ArrayList<GjFeature> features = new ArrayList<>();
    for ( final ResourceEntry entry : entries )
    {
      features.add( buildFeature( filter, entry ) );
    }

    return new GjFeatureCollection( features, null, null, null );
  }

  private String serialize( final GjElement collection )
  {
    final StringWriter writer = new StringWriter();
    final JsonGenerator g = Json.createGenerator( writer );
    new GeoJsonWriter().emit( g, collection );
    g.close();

    return writer.toString();
  }
}

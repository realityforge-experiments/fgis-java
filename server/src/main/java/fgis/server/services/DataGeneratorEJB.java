package fgis.server.services;

import fgis.server.entity.fgis.Resource;
import fgis.server.entity.fgis.ResourceTrack;
import fgis.server.entity.fgis.dao.ResourceRepository;
import fgis.server.entity.fgis.dao.ResourceTrackRepository;
import java.util.Date;
import java.util.Random;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import org.geolatte.geom.Point;
import org.geolatte.geom.Points;

@javax.ejb.Local
@Stateless
public class DataGeneratorEJB
{
  private static final double INITIAL_X = 144.92978643046;
  private static final double INITIAL_Y = -37.794939500455;
  private static final double SCALE_FACTOR = 100.0;
  private static final Random RANDOM = new Random();

  @EJB
  private ResourceRepository _resourceService;

  @EJB
  private ResourceTrackRepository _resourceTrackRepository;


  @Schedule( second = "*/5", minute = "*", hour = "*", persistent = false )
  public void insertData()
  {
    System.out.println( "DataGeneratorEJB.insertData..." );
    jitterResource( "Person", "Peter" );
    jitterResource( "Person", "Phil" );
    jitterResource( "Person", "James" );
    jitterResource( "Person", "Karen" );
    jitterResource( "Slip-On", "THX-1138" );
    jitterResource( "Passenger Vehicle", "LUH-3417" );
    jitterResource( "Tanker", "SEN-5241" );
    System.out.println( "...DataGeneratorEJB.insertData" );
  }

  private void jitterResource( final String resourceType, final String resourceName )
  {
    final Resource resource = findOrCreateResource( resourceType, resourceName );

    final Point lastLocation = resource.getLocation();
    final double startX = null == lastLocation ? INITIAL_X : lastLocation.getX();
    final double startY = null == lastLocation ? INITIAL_Y : lastLocation.getY();
    final double latPosition = startX + jitterLocation();
    final double longPosition = startY + jitterLocation();

    final Point point = Points.create2D( latPosition, longPosition );

    updateResourceLocation( resource, point );
  }

  private double jitterLocation()
  {
    return ( RANDOM.nextDouble() / SCALE_FACTOR ) - ( 1 / SCALE_FACTOR / 2 );
  }

  private Resource findOrCreateResource( final String resourceType, final String resourceName )
  {
    Resource resource = _resourceService.findByName( resourceName );
    if ( null == resource )
    {
      resource = new Resource( resourceType, resourceName );
      _resourceService.persist( resource );
    }
    return resource;
  }

  private void updateResourceLocation( final Resource resource, final Point point )
  {
    resource.setLocation( point );
    _resourceTrackRepository.persist( new ResourceTrack( resource, new Date(), point ) );
  }
}

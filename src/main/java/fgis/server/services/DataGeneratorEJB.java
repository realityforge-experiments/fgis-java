package fgis.server.services;

import fgis.server.entity.fgis.Resource;
import fgis.server.entity.fgis.ResourceTrack;
import fgis.server.entity.fgis.dao.ResourceRepository;
import fgis.server.entity.fgis.dao.ResourceTrackRepository;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.Point;
import org.geolatte.geom.PointSequenceBuilder;
import org.geolatte.geom.PointSequenceBuilders;
import org.geolatte.geom.crs.CrsId;

@javax.ejb.Local
@Stateless
public class DataGeneratorEJB
{
  @EJB
  private ResourceRepository _resourceService;

  @EJB
  private ResourceTrackRepository _resourceTrackRepository;


  @Schedule( second = "*/5", minute = "*", hour = "*", persistent = false )
  public void insertData()
  {
    final String resourceName = "Peter";
    Resource resource = _resourceService.findByName( resourceName );
    if ( null == resource )
    {
      resource = new Resource( "Person", resourceName );
      _resourceService.persist( resource );
    }
    final Random random = new Random();

    final Point lastLocation = resource.getLocation();

    final PointSequenceBuilder builder =
      PointSequenceBuilders.variableSized( DimensionalFlag.d2D, CrsId.UNDEFINED );
    final double startX = null == lastLocation ? 144.92978643046 : lastLocation.getX();
    final double startY = null == lastLocation ? -37.794939500455 : lastLocation.getY();

    final double latPosition = startX + ( random.nextDouble() / 100 ) - 0.005;
    final double longPosition = startY + ( random.nextDouble() / 100 ) - 0.005;
    builder.add( latPosition , longPosition );


    System.out.println( "Generating point " + latPosition + ", " + longPosition +
                        " for resource " + resource.getName() );

    final Point point = new Point( builder.toPointSequence() );
    _resourceTrackRepository.persist( new ResourceTrack( resource, new Date(), point ) );
    resource.setLocation( point );
  }
}

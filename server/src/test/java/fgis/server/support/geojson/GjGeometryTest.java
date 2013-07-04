package fgis.server.support.geojson;

import org.geolatte.geom.Geometry;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class GjGeometryTest
  extends AbstractGjElementTest
{
  @Test
  public void geometry()
    throws Exception
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjGeometry e = new GjGeometry( geometry, null, null, null );
    assertEquals( e.getGeometry(), geometry );
    assertPropertyAllowed( e, "X" );
    assertPropertyNotAllowed( e, "coordinates" );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  public void nullGeometryPassedToGeometry()
    throws Exception
  {
    try
    {
      new GjGeometry( null, null, null, null );
    }
    catch ( final IllegalArgumentException e )
    {
      return;
    }
    fail( "Expected to fail when passing null geometry" );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  public void geometryCollectionPassedToGeometryShouldFail()
    throws Exception
  {
    try
    {
      new GjGeometry( fromWkT( "GEOMETRYCOLLECTION(POINT (1 1))" ), null, null, null );
    }
    catch ( final IllegalArgumentException e )
    {
      return;
    }
    fail( "Expected to fail when passing null geometry" );
  }
}

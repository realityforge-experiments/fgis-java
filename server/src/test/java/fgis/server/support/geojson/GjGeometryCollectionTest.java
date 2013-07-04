package fgis.server.support.geojson;

import java.util.Arrays;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class GjGeometryCollectionTest
  extends AbstractGjElementTest
{
  @Test
  public void basic()
    throws Exception
  {
    final GjGeometry g = new GjGeometry( fromWkT( "POINT (1 1)" ), null, null, null );
    final GjGeometryCollection e = new GjGeometryCollection( Arrays.asList( g ), null, null, null );
    assertEquals( e.getCollection().size(), 1 );
    assertTrue( e.getCollection().contains( g ) );
    assertPropertyAllowed( e, "X" );
    assertPropertyNotAllowed( e, "geometries" );
    try
    {
      e.getCollection().add( g );
    }
    catch ( Exception e1 )
    {
      return;
    }
    fail( "Expected collection list to be immutable but an edit was allowed" );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  public void nullGeometryCollectionPassed()
    throws Exception
  {
    try
    {
      new GjGeometryCollection( null, null, null, null );
    }
    catch ( final IllegalArgumentException e )
    {
      return;
    }
    fail( "Expected to fail when passing null collection" );
  }
}

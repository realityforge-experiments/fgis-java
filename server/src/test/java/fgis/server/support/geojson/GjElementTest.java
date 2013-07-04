package fgis.server.support.geojson;

import java.util.HashMap;
import javax.json.JsonValue;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.CrsRegistry;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class GjElementTest
  extends AbstractGjElementTest
{
  @Test
  public void basicInteraction()
    throws Exception
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final CrsId crsId = CrsRegistry.getCrsIdForEPSG( 1234 );
    final Envelope bbox = new Envelope( 0, 0, 2, 2, crsId );
    final HashMap<String, JsonValue> additional = new HashMap<>();
    additional.put( "foo", JsonValue.FALSE );
    final GjGeometry e = new GjGeometry( geometry, crsId, bbox, additional );
    assertEquals( e.getGeometry(), geometry );
    assertEquals( e.getBBox(), bbox );
    assertEquals( e.getCrsId(), crsId );
    assertEquals( e.getAdditionalProperties(), additional );
    assertPropertyAllowed( e, "X" );
    assertPropertyNotAllowed( e, "crs" );
    assertPropertyNotAllowed( e, "bbox" );
    assertPropertyNotAllowed( e, "type" );
  }

  @Test
  public void nullMetadata()
    throws Exception
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjGeometry e = new GjGeometry( geometry, null, null, null );
    assertEquals( e.getGeometry(), geometry );
    assertNull( e.getBBox() );
    assertNull( e.getCrsId() );
    assertEquals( e.getAdditionalProperties().size(), 0 );
  }

  @Test
  public void invalidAdditionalData()
    throws Exception
  {
    verifyAdditionalDataRestricted( "type" );
    verifyAdditionalDataRestricted( "crs" );
    verifyAdditionalDataRestricted( "bbox" );
  }

  private void verifyAdditionalDataRestricted( final String key )
  {
    final HashMap<String, JsonValue> additional = new HashMap<>();
    additional.put( key, JsonValue.FALSE );
    try
    {
      new GjGeometry( fromWkT( "POINT (1 1)" ), null, null, additional );
    }
    catch ( final IllegalArgumentException e )
    {
      return;
    }
    fail( "Expected to receive an exception for addition data: " + additional );
  }
}

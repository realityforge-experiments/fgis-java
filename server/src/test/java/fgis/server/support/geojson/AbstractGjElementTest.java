package fgis.server.support.geojson;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import static org.testng.Assert.*;

public abstract class AbstractGjElementTest
{
  protected final void assertPropertyAllowed( final GjElement e, final String key )
  {
    assertTrue( e.isPropertyAllowed( key ) );
  }

  protected final void assertPropertyNotAllowed( final GjElement e, final String key )
  {
    assertFalse( e.isPropertyAllowed( key ) );
  }

  @SuppressWarnings( "unchecked" )
  public final <T extends Geometry> T fromWkT( final String wkt )
  {
    return (T) Wkt.newDecoder( Dialect.POSTGIS_EWKT_1 ).decode( wkt );
  }
}

package fgis.server.support.geojson;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonGenerator;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class GeoJsonWriterTest
{
  @Test
  public void emitPointGeometry()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjElement e = new GjGeometry( geometry, null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson = "{\"type\":\"Point\",\"coordinates\":[1.0,1.0]}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitGeometryCollection()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjGeometry g = new GjGeometry( geometry, null, null, null );
    final GjElement e = new GjGeometryCollection( Arrays.asList( g ), null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson =
      "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\",\"coordinates\":[1.0,1.0]}]}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  private JsonStructure writeAndRead( final GjElement e )
  {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final JsonGenerator g = Json.createGenerator( baos );

    new GeoJsonWriter().emit( g, e );

    g.flush();

    return parse( baos.toByteArray() );
  }

  private JsonStructure parse( final byte[] bytes )
  {
    return parse( new String( bytes, Charset.forName( "US-ASCII" ) ) );
  }

  private JsonStructure parse( final String json )
  {
    return new JsonReader( new StringReader( json ) ).read();
  }


  @SuppressWarnings( "unchecked" )
  public final <T extends Geometry> T fromWkT( final String wkt )
  {
    return (T) Wkt.newDecoder( Dialect.POSTGIS_EWKT_1 ).decode( wkt );
  }
}

package fgis.server.support.geojson;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.CrsRegistry;
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
  public void emitLineStringGeometry()
  {
    final Geometry geometry = fromWkT( "LINESTRING ( 1 1, 2 1 )" );
    final GjElement e = new GjGeometry( geometry, null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson = "{\"type\":\"LineString\",\"coordinates\":[[1.0,1.0],[2.0,1.0]]}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitPolygonGeometry()
  {
    final Geometry geometry = fromWkT( "POLYGON ( ( 100.0 0.0, 101.0 0.0, 101.0 1.0, 100.0 1.0, 100.0 0.0 ) ( 100.2 0.2, 100.8 0.2, 100.8 0.8, 100.2 0.8, 100.2 0.2 ) )" );
    final GjElement e = new GjGeometry( geometry, null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson = "{ \"type\": \"Polygon\",\n" +
                                "  \"coordinates\": [\n" +
                                "    [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ],\n" +
                                "    [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ]\n" +
                                "    ]\n" +
                                " }";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitMultiPointGeometry()
  {
    final Geometry geometry = fromWkT( "MULTIPOINT ( (100.0 0.0) (101.0 1.0) )" );
    final GjElement e = new GjGeometry( geometry, null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson = "{ \"type\": \"MultiPoint\",\n" +
                                "  \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ]\n" +
                                "  }";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitMultiLineStringGeometry()
  {
    final Geometry geometry = fromWkT( "MULTILINESTRING ( ( 100.0 0.0, 101.0 1.0 ) ( 102.0 2.0, 103.0 3.0 ) )" );
    final GjElement e = new GjGeometry( geometry, null, null, null );

    final JsonStructure result = writeAndRead( e );


    final String expectedJson = "{ \"type\": \"MultiLineString\",\n" +
                                "  \"coordinates\": [\n" +
                                "      [ [100.0, 0.0], [101.0, 1.0] ],\n" +
                                "      [ [102.0, 2.0], [103.0, 3.0] ]\n" +
                                "    ]\n" +
                                "  }";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitMultiPolygonGeometry()
  {
    final Geometry geometry = fromWkT( "MULTIPOLYGON ( ( ( 1 1, 2 1, 2 2, 1 2, 1 1 ) ( 1.25 1.25, 1.75 1.25, 1.75 1.75, 1.25 1.75, 1.25 1.25 ) ) ( ( 1 1, 2 1, 2 2, 1 2, 1 1 ) ( 1.25 1.25, 1.75 1.25, 1.75 1.75, 1.25 1.75, 1.25 1.25 ) ) )" );
    final GjElement e = new GjGeometry( geometry, null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson = "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[1.0,1.0],[2.0,1.0],[2.0,2.0],[1.0,2.0],[1.0,1.0]],[[1.25,1.25],[1.75,1.25],[1.75,1.75],[1.25,1.75],[1.25,1.25]]],[[[1.0,1.0],[2.0,1.0],[2.0,2.0],[1.0,2.0],[1.0,1.0]],[[1.25,1.25],[1.75,1.25],[1.75,1.75],[1.25,1.75],[1.25,1.25]]]]}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitGeometryWithBBox()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjElement e = new GjGeometry( geometry, null, new Envelope( 0, 0, 2, 2 ), null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson = "{\"type\":\"Point\",\"bbox\":[0.0,0.0,2.0,2.0],\"coordinates\":[1.0,1.0]}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitGeometryWithAdditionalAttributes()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final HashMap<String, JsonValue> additionalProperties = new HashMap<>();
    additionalProperties.put( "Foo", JsonValue.FALSE );
    additionalProperties.put( "Bar", JsonValue.NULL );
    final GjElement e = new GjGeometry( geometry, null, null, additionalProperties );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson = "{\"type\":\"Point\",\"Foo\":false,\"Bar\":null,\"coordinates\":[1.0,1.0]}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitGeometryWithCrs()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final CrsId crsId = CrsRegistry.getCrsIdForEPSG( 3111 );
    final GjElement e = new GjGeometry( geometry, crsId, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson =
      "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:3111\"}},\"coordinates\":[1.0,1.0]}";
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

  @Test
  public void emitFeature()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjGeometry g = new GjGeometry( geometry, null, null, null );
    final GjElement e = new GjFeature( null, g, null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson =
      "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1.0,1.0]},\"properties\":{}}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitFeatureWithId()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjGeometry g = new GjGeometry( geometry, null, null, null );
    final GjElement e = new GjFeature( JsonValue.TRUE, g, null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson =
      "{\"type\":\"Feature\",\"id\":true,\"geometry\":{\"type\":\"Point\",\"coordinates\":[1.0,1.0]},\"properties\":{}}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitFeatureWithAdditionalProperties()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjGeometry g = new GjGeometry( geometry, null, null, null );
    final HashMap<String, JsonValue> additionalProperties = new HashMap<>();
    additionalProperties.put( "Foo", JsonValue.FALSE );
    additionalProperties.put( "Bar", JsonValue.NULL );
    final GjElement e = new GjFeature( null, g, null, null, additionalProperties );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson =
      "{\"type\":\"Feature\",\"Foo\":false,\"Bar\":null,\"geometry\":{\"type\":\"Point\",\"coordinates\":[1.0,1.0]},\"properties\":{\"Foo\":false,\"Bar\":null}}";
    final JsonObject expected = (JsonObject) parse( expectedJson );
    assertEquals( result, expected );
  }

  @Test
  public void emitFeatureCollection()
  {
    final Geometry geometry = fromWkT( "POINT (1 1)" );
    final GjGeometry g = new GjGeometry( geometry, null, null, null );
    final GjFeature f = new GjFeature( null, g, null, null, null );
    final GjElement e = new GjFeatureCollection( Arrays.asList( f ), null, null, null );

    final JsonStructure result = writeAndRead( e );

    final String expectedJson =
      "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1.0,1.0]},\"properties\":{}}]}";
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

package fgis.server.support.geojson;

import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;
import org.geolatte.geom.LinearRing;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Point;
import org.geolatte.geom.PointCollection;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.crs.CrsId;

public class GeoJsonWriter
{
  public void emit( @Nonnull final JsonGenerator g,
                    @Nonnull final GjElement element )
  {
    if ( element instanceof GjGeometry )
    {
      emit( g, (GjGeometry) element );
    }
    else if ( element instanceof GjGeometryCollection )
    {
      emit( g, (GjGeometryCollection) element );
    }
    else if ( element instanceof GjFeature )
    {
      emit( g, (GjFeature) element );
    }
    else if ( element instanceof GjFeatureCollection )
    {
      emit( g, (GjFeatureCollection) element );
    }
    else
    {
      throw new IllegalStateException( "Unknown element: " + element );
    }
  }

  public void emit( @Nonnull final JsonGenerator g,
                    @Nonnull final GjGeometry element )
  {
    g.writeStartObject();
    final Geometry geometry = element.getGeometry();
    if ( geometry instanceof Point )
    {
      emitHeader( g, "Point", element.getBBox(), element.getCrsId() );

      final Point p = (Point) geometry;
      g.writeStartArray( "coordinates" );
      emitPositionBody( g, p.getX(), p.getY(), p.is3D() ? p.getZ() : null, p.isMeasured() ? p.getM() : null );
      g.writeEnd();

    }
    else if ( geometry instanceof LineString )
    {
      emitHeader( g, "LineString", element.getBBox(), element.getCrsId() );

      g.writeStartArray( "coordinates" );
      emitLineStringBody( g, (LineString) g );
      g.writeEnd();
    }
    else if ( geometry instanceof Polygon )
    {
      emitHeader( g, "Polygon", geometry.getEnvelope(), element.getCrsId() );

      final Polygon p = (Polygon) geometry;

      g.writeStartArray( "coordinates" );
      emitPolygonBody( g, p );
      g.writeEnd();
    }
    else if ( geometry instanceof MultiPoint )
    {
      emitHeader( g, "MultiPoint", element.getBBox(), element.getCrsId() );
      final MultiPoint mp = (MultiPoint) geometry;
      g.writeStartArray( "coordinates" );
      final PointCollection p = mp.getPoints();
      final int size = p.size();
      for ( int i = 0; i < size; i++ )
      {
        emitPositionBody( g, p.getX( i ),
                          p.getY( i ),
                          p.is3D() ? p.getZ( i ) : null,
                          p.isMeasured() ? p.getM( i ) : null );
      }
      g.writeEnd();
    }
    else if ( geometry instanceof MultiLineString )
    {
      emitHeader( g, "MultiLineString", element.getBBox(), element.getCrsId() );
      g.writeStartArray( "coordinates" );

      final MultiLineString multiLineString = (MultiLineString) geometry;
      final int count = multiLineString.getNumGeometries();
      for ( int i = 0; i < count; i++ )
      {
        final LineString lineString = multiLineString.getGeometryN( i );
        g.writeStartArray();
        emitLineStringBody( g, lineString );
        g.writeEnd();
      }

      g.writeEnd();
    }
    else if ( geometry instanceof MultiPolygon )
    {
      emitHeader( g, "MultiPolygon", element.getBBox(), element.getCrsId() );
      final MultiPolygon mp = (MultiPolygon) geometry;

      g.writeStartArray( "coordinates" );
      final int size = mp.getNumGeometries();
      for ( int i = 0; i < size; i++ )
      {
        g.writeStartArray();
        emitPolygonBody( g, mp.getGeometryN( i ) );
        g.writeEnd();
      }
      g.writeEnd();
    }
    else
    {
      throw new IllegalArgumentException( "Unknown geometry type: " + geometry.getClass().getName() );
    }
    g.writeEnd();
  }

  private void emitPolygonBody( final JsonGenerator g, final Polygon p )
  {
    final LinearRing exteriorRing = p.getExteriorRing();
    g.writeStartArray();
    emitLinearRingBody( g, exteriorRing );
    g.writeEnd();
    final int count = p.getNumInteriorRing();
    for ( int i = 0; i < count; i++ )
    {
      final LinearRing inner = p.getInteriorRingN( i );
      g.writeStartArray();
      emitLinearRingBody( g, inner );
      g.writeEnd();
    }
  }

  private void emitLineStringBody( final JsonGenerator g, final LineString lineString )
  {
    emitPointCollection( g, lineString.getPoints() );
  }

  private void emitLinearRingBody( final JsonGenerator g, final LinearRing linearRing )
  {
    emitPointCollection( g, linearRing.getPoints() );
  }

  private void emitPointCollection( final JsonGenerator g, final PointCollection p )
  {
    final int size = p.size();
    for ( int i = 0; i < size; i++ )
    {
      emitPosition( g, p.getX( i ),
                    p.getY( i ),
                    p.is3D() ? p.getZ( i ) : null,
                    p.isMeasured() ? p.getM( i ) : null );
    }
  }

  private void emitPosition( @Nonnull final JsonGenerator g,
                             final double x,
                             final double y,
                             @Nullable final Double z,
                             @Nullable final Double m )
  {
    g.writeStartArray();
    emitPositionBody( g, x, y, z, m );
    g.writeEnd();
  }

  private void emitPositionBody( final JsonGenerator g, final double x, final double y, final Double z, final Double m )
  {
    g.write( x );
    g.write( y );
    if ( null != z )
    {
      g.write( z );
    }
    if ( null != m )
    {
      g.write( m );
    }
  }

  public void emit( @Nonnull final JsonGenerator g,
                    @Nonnull final GjGeometryCollection element )
  {
    g.writeStartObject();

    emitHeader( g, "GeometryCollection", element.getBBox(), element.getCrsId() );

    g.writeStartArray( "geometries" );
    for ( final GjGeometry geometry : element.getCollection() )
    {
      emit( g, geometry );
    }
    g.writeEnd();

    g.writeEnd();
  }

  public void emit( @Nonnull final JsonGenerator g,
                    @Nonnull final GjFeature element )
  {
    g.writeStartObject();
    emitHeader( g, "Feature", element.getBBox(), element.getCrsId() );
    if ( JsonValue.NULL != element.getId() )
    {
      g.write( "id", element.getId() );
    }
    final GjGeometry geometry = element.getGeometry();
    if ( null != geometry )
    {
      g.writeStartObject( "geometry" );
      emit( g, geometry );
      g.writeEnd();
    }
    else
    {
      final GjGeometryCollection geometryCollection = element.getGeometryCollection();
      if ( null != geometryCollection )
      {
        g.writeStartObject( "geometry" );
        emit( g, geometryCollection );
        g.writeEnd();
      }
      else
      {
        g.write( "geometry", JsonValue.NULL );
      }
    }

    g.writeStartObject( "properties" );
    writeProperties( g, element.getAdditionalProperties() );
    g.writeEnd();

    g.writeEnd();
  }

  public void emit( @Nonnull final JsonGenerator g,
                    @Nonnull final GjFeatureCollection element )
  {
    g.writeStartObject();
    emitHeader( g, "FeatureCollection", element.getBBox(), element.getCrsId() );
    g.writeStartArray( "features" );
    for ( final GjFeature feature : element.getCollection() )
    {
      emit( g, feature );
    }
    g.writeEnd();

    writeProperties( g, element.getAdditionalProperties() );

    g.writeEnd();
  }

  private void emitHeader( final JsonGenerator g,
                           final String type,
                           @Nullable final Envelope bbox,
                           @Nullable final CrsId crsId )
  {
    g.write( "type", type );
    writeBbox( g, bbox );
    writeCrsId( g, crsId );
  }

  private void writeCrsId( final JsonGenerator g, final CrsId crsId )
  {
    if ( null != crsId )
    {
      g.writeStartObject( "crs" );
      g.write( "type", "name" );
      g.writeStartObject( "properties" );
      g.write( "name", crsId.getAuthority() + ":" + crsId.getCode() );
      g.writeEnd();

      g.writeEnd();
    }
  }

  private void writeBbox( final JsonGenerator g, final Envelope bbox )
  {
    if ( null != bbox )
    {
      g.writeStartArray( "bbox" );
      emitPositionBody( g, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() );
      g.writeEnd();
    }
  }

  private void writeProperties( final JsonGenerator g, final Map<String, JsonValue> properties )
  {
    for ( final Entry<String, JsonValue> entry : properties.entrySet() )
    {
      g.write( entry.getKey(), entry.getValue() );
    }
  }
}

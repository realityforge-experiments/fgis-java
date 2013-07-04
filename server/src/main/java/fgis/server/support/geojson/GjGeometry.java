package fgis.server.support.geojson;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.JsonValue;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.crs.CrsId;

public final class GjGeometry
  extends GjElement
{
  @Nonnull
  private final Geometry _geometry;

  @SuppressWarnings( "ConstantConditions" )
  public GjGeometry( @Nonnull final Geometry geometry,
                     @Nullable final CrsId crsId,
                     @Nullable final Envelope bbox,
                     @Nullable final Map<String, JsonValue> additionalProperties )
  {
    super( crsId, bbox, additionalProperties );
    if ( null == geometry )
    {
      throw new IllegalArgumentException( "geometry is null" );
    }
    if ( geometry instanceof GeometryCollection )
    {
      throw new IllegalStateException( "geometry is a GeometryCollection" );
    }
    _geometry = geometry;
  }

  @Nonnull
  public Geometry getGeometry()
  {
    return _geometry;
  }

  @Override
  protected boolean isPropertyAllowed( final String name )
  {
    return super.isPropertyAllowed( name ) && !( "coordinates".equals( name ) );
  }
}

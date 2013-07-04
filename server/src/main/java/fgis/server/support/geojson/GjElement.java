package fgis.server.support.geojson;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.JsonValue;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.crs.CrsId;

public abstract class GjElement
{
  @Nonnull
  private final Map<String, JsonValue> _additionalProperties;
  @Nullable
  private final CrsId _crsId;
  @Nullable
  private final Envelope _bbox;

  protected GjElement( @Nullable final CrsId crsId,
                       @Nullable final Envelope bbox,
                       @Nullable final Map<String, JsonValue> additionalProperties )
  {
    if ( null != additionalProperties )
    {
      for ( final String key : additionalProperties.keySet() )
      {
        if ( !isPropertyAllowed( key ) )
        {
          throw new IllegalArgumentException( "Property named '" + key + "' is not allowed." );
        }
      }
    }
    _crsId = crsId;
    _bbox = bbox;
    _additionalProperties =
      null == additionalProperties ?
      Collections.<String, JsonValue>emptyMap() :
      Collections.unmodifiableMap( additionalProperties );
  }

  @Nullable
  public final CrsId getCrsId()
  {
    return _crsId;
  }

  @Nullable
  public final Envelope getBBox()
  {
    return _bbox;
  }

  @Nonnull
  public final Map<String, JsonValue> getAdditionalProperties()
  {
    return _additionalProperties;
  }

  protected boolean isPropertyAllowed( final String name )
  {
    return !( "type".equals( name ) ||
              "crs".equals( name ) ||
              "bbox".equals( name ) );
  }
}

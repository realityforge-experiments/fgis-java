package fgis.server.support.geojson;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.JsonValue;
import org.geolatte.geom.Envelope;

public abstract class GjElement
{
  @Nonnull
  private final Map<String, JsonValue> _additionalProperties;
  private final boolean _includeCRS;
  private final Envelope _bbox;

  protected GjElement( final boolean includeCRS,
                       final Envelope bbox,
                       @Nullable final Map<String, JsonValue> additionalProperties )
  {
    if ( null != additionalProperties )
    {
      for ( final String key : additionalProperties.keySet() )
      {
        if ( !isPropertyAllowed( key ) )
        {
          throw new IllegalStateException( "Property named '" + key + "' is not allowed." );
        }
      }
    }
    _includeCRS = includeCRS;
    _bbox = bbox;
    _additionalProperties =
      null == additionalProperties ?
      Collections.<String, JsonValue>emptyMap() :
      Collections.unmodifiableMap( additionalProperties );
  }

  public final boolean includeCRS()
  {
    return _includeCRS;
  }

  @Nullable
  public Envelope getBBox()
  {
    return _bbox;
  }

  @Nonnull
  public Map<String, JsonValue> getAdditionalProperties()
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

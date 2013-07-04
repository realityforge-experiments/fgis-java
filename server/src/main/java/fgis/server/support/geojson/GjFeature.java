package fgis.server.support.geojson;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.JsonValue;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.crs.CrsId;

public final class GjFeature
  extends GjElement
{
  @Nonnull
  private final JsonValue _id;
  @Nullable
  final GjGeometry _geometry;
  @Nullable
  final GjGeometryCollection _geometryCollection;

  public GjFeature( @Nullable final JsonValue id,
                    @Nonnull final GjGeometry geometry,
                    @Nullable final CrsId crsId,
                    @Nullable final Envelope bbox,
                    @Nullable final Map<String, JsonValue> additionalProperties )
  {
    this( id, crsId, bbox, additionalProperties, geometry, null );
  }

  public GjFeature( @Nullable final JsonValue id,
                    @Nonnull final GjGeometryCollection geometryCollection,
                    @Nullable final CrsId crsId,
                    @Nullable final Envelope bbox,
                    @Nullable final Map<String, JsonValue> additionalProperties )
  {
    this( id, crsId, bbox, additionalProperties, null, geometryCollection );
  }

  public GjFeature( @Nullable final JsonValue id,
                    @Nullable final CrsId crsId,
                    @Nullable final Envelope bbox,
                    @Nullable final Map<String, JsonValue> additionalProperties )
  {
    this( id, crsId, bbox, additionalProperties, null, null );
  }

  private GjFeature( @Nullable final JsonValue id,
                     @Nullable final CrsId crsId,
                     @Nullable final Envelope bbox,
                     @Nullable final Map<String, JsonValue> additionalProperties,
                     @Nullable final GjGeometry geometry,
                     @Nullable final GjGeometryCollection geometryCollection )
  {
    super( crsId, bbox, additionalProperties );
    _id = null == id ? JsonValue.NULL : id;
    _geometry = geometry;
    _geometryCollection = geometryCollection;
  }

  @Nonnull
  public JsonValue getId()
  {
    return _id;
  }

  @Nullable
  public GjGeometry getGeometry()
  {
    return _geometry;
  }

  @Nullable
  public GjGeometryCollection getGeometryCollection()
  {
    return _geometryCollection;
  }

  @Override
  protected boolean isPropertyAllowed( final String name )
  {
    return super.isPropertyAllowed( name ) &&
           !( "geometry".equals( name ) ||
              "properties".equals( name ) ||
              "id".equals( name ) );
  }
}

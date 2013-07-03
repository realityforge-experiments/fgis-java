/*
 * Copyright (C) 2008  Camptocamp
 *
 * This file is part of MapFish Server
 *
 * MapFish Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MapFish Server.  If not, see <http://www.gnu.org/licenses/>.
 */

package fgis.server.support.geojson;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.JsonValue;
import org.geolatte.geom.Envelope;

public final class GjFeatureCollection
  extends GjElement
{
  private final List<GjFeature> _collection;

  public GjFeatureCollection( @Nonnull final List<GjFeature> collection,
                              final boolean includeCRS,
                              final Envelope bbox,
                              @Nullable final Map<String, JsonValue> additionalProperties )
  {
    super( includeCRS, bbox, additionalProperties );
    _collection = Collections.unmodifiableList( collection );;
  }

  @Nonnull
  public List<GjFeature> getCollection()
  {
    return _collection;
  }

  @Override
  protected boolean isPropertyAllowed( final String name )
  {
    return super.isPropertyAllowed( name ) && !( "features".equals( name ) );
  }
}

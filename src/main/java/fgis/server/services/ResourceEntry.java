package fgis.server.services;

import fgis.server.entity.fgis.Resource;
import fgis.server.entity.fgis.ResourceTrack;
import java.util.List;

final class ResourceEntry
{
  private final Resource _resource;
  private final List<ResourceTrack> _tracks;

  ResourceEntry( final Resource resource, final List<ResourceTrack> tracks )
  {
    _resource = resource;
    _tracks = tracks;
  }

  Resource getResource()
  {
    return _resource;
  }

  List<ResourceTrack> getTracks()
  {
    return _tracks;
  }
}

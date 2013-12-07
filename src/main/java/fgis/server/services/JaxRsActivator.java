package fgis.server.services;

import fgis.server.service.FGISJaxRsApplication;
import java.util.Set;
import javax.ws.rs.ApplicationPath;

@ApplicationPath( FGISJaxRsApplication.APPLICATION_PATH )
public class JaxRsActivator
  extends FGISJaxRsApplication
{
  @Override
  public Set<Class<?>> getClasses()
  {
    final Set<Class<?>> classes = super.getClasses();
    classes.add( NoResultExceptionMapper.class );
    classes.add( ResourceService.class );
    return classes;
  }
}

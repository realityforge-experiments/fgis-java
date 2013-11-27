package fgis.server;

import com.googlecode.mgwt.linker.server.Html5ManifestServletBase;
import com.googlecode.mgwt.linker.server.propertyprovider.UserAgentPropertyProvider;

public class ManifestServlet
  extends Html5ManifestServletBase
{
  public ManifestServlet()
  {
    addPropertyProvider( new UserAgentPropertyProvider() );
  }
}

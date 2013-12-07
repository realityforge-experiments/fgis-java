package fgis.client.ioc;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import fgis.client.SimpleUI;

@GinModules( { BasicModule.class } )
public interface FgisGinjector
  extends Ginjector
{
  SimpleUI getSimpleUI();
}

package fgis.client.ioc;

import com.google.gwt.inject.client.AbstractGinModule;
import fgis.client.SimpleUI;

public class BasicModule
  extends AbstractGinModule
{
  @Override
  protected void configure()
  {
    bind( SimpleUI.class ).asEagerSingleton();
  }
}

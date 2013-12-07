package fgis.client.ioc;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import fgis.client.SimpleUI;

public class BasicModule
  extends AbstractGinModule
{
  @Override
  protected void configure()
  {
    bind( SimpleUI.class ).asEagerSingleton();
    bind( EventBus.class ).to( SimpleEventBus.class ).asEagerSingleton();
  }
}

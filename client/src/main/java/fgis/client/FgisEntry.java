package fgis.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FgisEntry
  implements EntryPoint
{
  private static final Logger LOG = Logger.getLogger( FgisEntry.class.getName() );

  public void onModuleLoad()
  {
    final Button button = new Button( "Place Pizza Order" );
    button.addClickHandler( new ClickHandler()
    {
      public void onClick( ClickEvent event )
      {
        LOG.log( Level.INFO, "Order Placed" );
        Window.alert( "Order Placed" );
      }

    } );
    RootPanel.get().add( button );
  }
}

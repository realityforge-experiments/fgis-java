package fgis.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;

public class SimpleUI
  extends Composite
{
  @Override
  public Widget asWidget()
  {
    final VerticalPanel panel = new VerticalPanel();
    panel.add( new MainFrame() );
    return panel;
  }
}

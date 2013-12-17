package fgis.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.svenjacobs.gwtbootstrap3.client.ui.Icon;
import com.svenjacobs.gwtbootstrap3.client.ui.constants.IconType;

public class MainFrame
  extends Composite
{
  private static MainFrameUiBinder uiBinder = GWT.create( MainFrameUiBinder.class );

  interface MainFrameUiBinder
    extends UiBinder<Widget, MainFrame>
  {
  }

  public MainFrame()
  {
    initWidget( uiBinder.createAndBindUi( this ) );
    setOnline( true );
  }

  @UiField
  Icon connectionIcon;
  @UiField
  SpanElement connectionText;

  public void setOnline( final boolean online )
  {
    connectionIcon.setType( online ? IconType.ARROW_CIRCLE_UP : IconType.ARROW_CIRCLE_DOWN );
    connectionText.setInnerText( online ? "Connected" : "Disconnected" );
    final Element element = connectionText.getParentElement();
    element.removeClassName( online ? "disconnected" : "connected" );
    element.addClassName( online ? "connected" : "disconnected" );
  }
}

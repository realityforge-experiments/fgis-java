package fgis.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.svenjacobs.gwtbootstrap3.client.ui.ListItem;

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
  }

  @UiField
  ListItem gridSystemLink;

  /*
  * Method name is not relevant, the binding is done according to the class
  * of the parameter.
  */
  @UiHandler( "gridSystemLink" )
  void doClickSubmit( final ClickEvent event )
  {
    Window.alert( "Click Linked!" );
  }
}

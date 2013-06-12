package fgis.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.logging.Logger;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.OpenLayers;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.layer.TransitionEffect;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;

public final class FgisEntry
  implements EntryPoint
{
  private static final Logger LOG = Logger.getLogger( FgisEntry.class.getName() );

  public void onModuleLoad()
  {
    OpenLayers.setProxyHost( GWT.getHostPageBaseURL() + "map_proxy?targetURL=" );
    //create some MapOptions
    final MapOptions defaultMapOptions = new MapOptions();
    //causes the mouse popup to display coordinates in this format
    defaultMapOptions.setDisplayProjection( new Projection( "EPSG:4326" ) );
    defaultMapOptions.setNumZoomLevels( 16 );

    //Create a MapWidget
    final MapWidget mapWidget = new MapWidget( "500px", "500px", defaultMapOptions );
    //Create a WMS layer as base layer
    final WMSParams wmsParams = new WMSParams();
    wmsParams.setFormat( "image/png" );
    wmsParams.setLayers( "topp:tasmania_state_boundaries" );
    wmsParams.setStyles( "" );

    //create a WMS layer
    final WMSOptions wmsLayerParams = new WMSOptions();
    wmsLayerParams.setUntiled();
    wmsLayerParams.setTransitionEffect( TransitionEffect.RESIZE );

    final String wmsUrl = "http://demo.opengeo.org/geoserver/wms";
    final WMS wmsLayer = new WMS( "Basic WMS", wmsUrl, wmsParams, wmsLayerParams );

    //Add the WMS to the map
    final Map map = mapWidget.getMap();
    map.addLayer( wmsLayer );

    //The actual refresh
    final Button butRefresh = new Button( "Refresh WMS", new ClickHandler()
    {
      public void onClick( final ClickEvent event )
      {
        final WMSParams wmsParams = new WMSParams();
        wmsParams.setParameter( ( (Double) Math.random() ).toString(), ( (Double) Math.random() ).toString() );
        wmsLayer.mergeNewParams( wmsParams );
        wmsLayer.redraw();
      }
    } );

    //Lets add some default controls to the map
    //+ sign in the upper right corner to display the layer switcher
    map.addControl( new LayerSwitcher() );
    //+ sign in the lower right to display the overview map
    map.addControl( new OverviewMap() );
    //Display the scale line
    map.addControl( new ScaleLine() );

    //Center and zoom to a location
    final LonLat center = new LonLat( 146.7, -41.8 );
    final int zoomLevel = 6;
    map.setCenter( center, zoomLevel );

    final Panel panel = new VerticalPanel();

    panel.add( mapWidget );
    panel.add( butRefresh );

    RootPanel.get().add( panel );

    //force the map to fall behind popups
    mapWidget.getElement().getFirstChildElement().getStyle().setZIndex( 0 );
  }
}

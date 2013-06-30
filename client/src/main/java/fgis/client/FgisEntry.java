package fgis.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.List;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.OpenLayers;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.StyleMap;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureSelectedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.format.GeoJSON;
import org.gwtopenmaps.openlayers.client.layer.TransitionEffect;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.protocol.HTTPProtocol;
import org.gwtopenmaps.openlayers.client.protocol.HTTPProtocolOptions;
import org.gwtopenmaps.openlayers.client.strategy.FixedStrategy;
import org.gwtopenmaps.openlayers.client.strategy.FixedStrategyOptions;
import org.gwtopenmaps.openlayers.client.strategy.Strategy;

public final class FgisEntry
  implements EntryPoint
{
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

    final Vector polyLayer = createLayerFromJson( "Polygon Layer", GWT.getHostPageBaseURL() + "data/poly.json" );
    map.addLayer( polyLayer );
    //In the json we have defined styles in the properties, here we set these properties
    final Style style = new Style();
    style.setFillColor( "${fill}" );
    style.setStrokeColor( "${stroke}" );

    final StyleMap styleMap = new StyleMap( style );
    polyLayer.setStyleMap( styleMap );

    polyLayer.redraw();

    //SelectFeature control to capture clicks on the vectors.
    //We use this to unSelect the selected feature
    final SelectFeature selectFeature = new SelectFeature( polyLayer );
    selectFeature.setAutoActivate( true );
    map.addControl( selectFeature );
    polyLayer.addVectorFeatureSelectedListener( new VectorFeatureSelectedListener()
    {
      public void onFeatureSelected( FeatureSelectedEvent eventObject )
      {
        final VectorFeature selectedFeature = eventObject.getVectorFeature();
        selectFeature.unSelect( eventObject.getVectorFeature() );

        final List<String> attrNames = selectedFeature.getAttributes().getAttributeNames();
        String s = "";
        for ( final String attrName : attrNames )
        {
          s += attrName + ": " + selectedFeature.getAttributes().getAttributeAsString( attrName ) + "\n";
        }

        Window.alert( "You clicked on a polygon with the following properties in the json file :\n " + s );
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

  public static Vector createLayerFromJson( final String layerName, final String url )
  {

    final FixedStrategyOptions fOptions = new FixedStrategyOptions();
    final FixedStrategy fStrategy = new FixedStrategy( fOptions );

    final GeoJSON geoJson = new GeoJSON();

    final HTTPProtocolOptions httpProtOptions = new HTTPProtocolOptions();
    httpProtOptions.setUrl( url );
    httpProtOptions.setFormat( geoJson );

    final HTTPProtocol httpProt = new HTTPProtocol( httpProtOptions );

    final VectorOptions options = new VectorOptions();
    options.setStrategies( new Strategy[]{ fStrategy } );
    options.setProtocol( httpProt );

    return new Vector( layerName, options );
  }
}

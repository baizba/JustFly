package com.example.justfly.map;

import com.example.justfly.overlay.DirectionLineOverlay;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapFacade {

    private final MapView mapView;
    private MyLocationNewOverlay myLocationNewOverlay;

    public MapFacade(MapView mapView) {
        this.mapView = mapView;
        initialize();
    }

    public void enableFollowMyLocation() {
        myLocationNewOverlay.enableFollowLocation();
    }

    public void resume() {
        if (mapView != null) {
            mapView.onResume();
            myLocationNewOverlay.enableMyLocation();
        }
    }

    public void pause() {
        if (mapView != null) {
            myLocationNewOverlay.disableMyLocation();
            mapView.onPause();
        }
    }

    private void initialize() {
        //map configuration
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(12.0);
        mapView.setMinZoomLevel(5.0);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        initializeMyLocationOverlay();
        initializeDirectionLine();
    }

    private void initializeMyLocationOverlay() {
        myLocationNewOverlay = new MyLocationNewOverlay(mapView);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationNewOverlay);
    }

    private void initializeDirectionLine() {
        DirectionLineOverlay directionLineOverlay = new DirectionLineOverlay(myLocationNewOverlay);
        mapView.getOverlays().add(directionLineOverlay);
    }

}

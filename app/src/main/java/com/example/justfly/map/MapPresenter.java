package com.example.justfly.map;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapView;

public class MapPresenter {

    public static final int MIN_ZOOM = 4;
    public static final int MAX_ZOOM = 11;
    public static final int TILE_SIZE = 512;
    public static final String FILENAME_ENDING = ".png";
    public static final String OPEN_VFR_SOURCE_NAME = "openvfr";
    public static final XYTileSource OPEN_VFR = new XYTileSource(OPEN_VFR_SOURCE_NAME, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, FILENAME_ENDING, new String[]{""});

    private final MapView mapView;

    public MapPresenter(MapView mapView, AirspaceView airspaceView) {
        this.mapView = mapView;
        airspaceView.showMyLocation();
        airspaceView.showAirspaces();
    }

    public void switchMapSource() {
        ITileSource tileSource = mapView.getTileProvider().getTileSource();
        if (OPEN_VFR_SOURCE_NAME.equals(tileSource.name())) {
            mapView.setTileSource(TileSourceFactory.OpenTopo);
        } else {
            mapView.setTileSource(OPEN_VFR);
        }
    }

}

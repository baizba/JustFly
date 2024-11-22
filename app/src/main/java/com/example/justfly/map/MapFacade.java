package com.example.justfly.map;

import android.os.Build;

import com.example.justfly.livedata.GpsData;
import com.example.justfly.overlay.DirectionLineOverlay;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.function.Consumer;

public class MapFacade {

    public static final int MIN_ZOOM = 4;
    public static final int MAX_ZOOM = 11;
    public static final int TILE_SIZE = 512;
    public static final String FILENAME_ENDING = ".png";
    public static final String OPEN_VFR_SOURCE_NAME = "openvfr";
    public static final XYTileSource OPEN_VFR = new XYTileSource(OPEN_VFR_SOURCE_NAME, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, FILENAME_ENDING, new String[]{""});

    private final MapView mapView;
    private final Consumer<GpsData> gpsDataConsumer;
    private MyLocationNewOverlay myLocationNewOverlay;

    public MapFacade(MapView mapView, Consumer<GpsData> gpsDataConsumer) {
        this.mapView = mapView;
        this.gpsDataConsumer = gpsDataConsumer;
        initialize();
    }

    public void enableFollowMyLocation() {
        myLocationNewOverlay.enableFollowLocation();
    }

    public void switchMapSource() {
        ITileSource tileSource = mapView.getTileProvider().getTileSource();
        if (OPEN_VFR_SOURCE_NAME.equals(tileSource.name())) {
            mapView.setTileSource(TileSourceFactory.OpenTopo);
        } else {
            mapView.setTileSource(OPEN_VFR);
        }
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
        mapView.setTileSource(OPEN_VFR);
        mapView.setUseDataConnection(false);
        mapView.getController().setZoom(11.0);
        mapView.setMinZoomLevel(4.0);
        mapView.setMaxZoomLevel(14.0);
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
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(mapView.getContext());
        gpsMyLocationProvider.setLocationUpdateMinTime(500);
        gpsMyLocationProvider.startLocationProvider((location, source) -> {
            GpsData gpsData = new GpsData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && location.hasMslAltitude()) {
                gpsData.setAltitude(location.getMslAltitudeMeters());
            } else {
                gpsData.setAltitude(location.getAltitude());
            }
            gpsData.setSpeed(location.getSpeed());
            gpsDataConsumer.accept(gpsData);
        });
    }

    private void initializeDirectionLine() {
        DirectionLineOverlay directionLineOverlay = new DirectionLineOverlay(myLocationNewOverlay);
        mapView.getOverlays().add(directionLineOverlay);
    }
}

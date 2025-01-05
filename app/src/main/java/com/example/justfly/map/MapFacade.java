package com.example.justfly.map;

import android.os.Build;

import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.parser.OpenairParser;
import com.example.justfly.livedata.GpsData;
import com.example.justfly.overlay.DirectionLineOverlay;
import com.example.justfly.util.ResourceFileUtil;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        initializeAirspaces();
    }

    private void initializeAirspaces() {
        List<String> openairData = ResourceFileUtil.readResourceFile("openair/lo_airspaces.openair.txt");
        OpenairParser parser = new OpenairParser();
        Openair openair = parser.parse(openairData);
        List<Polygon> polygonAirspaces = openair.getAirspaces()
                .stream()
                .filter(airspace -> airspace.getPolygonPoints().size() > 1)
                .map(airspace -> {
                    List<GeoPoint> geoPoints = airspace
                            .getPolygonPoints()
                            .stream()
                            .map(polygonPoint -> new GeoPoint(polygonPoint.getLatitude(), polygonPoint.getLongitude()))
                            .collect(Collectors.toCollection(ArrayList::new));
                    geoPoints.add(geoPoints.get(0));//add the first point at the end to add to make sure the overlay is closed
                    Polygon polygon = new Polygon();
                    polygon.setPoints(geoPoints);
                    return polygon;
                })
                .collect(Collectors.toList());
        //mapView.getOverlays().addAll(polygonAirspaces);
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

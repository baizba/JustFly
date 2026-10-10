package com.example.justfly.map;

import android.location.Location;
import android.os.Bundle;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.gps.LocationRepository;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Coordinates map initialization, lifecycle, GPS following and user controls. */
public final class MapController {
    private final MapSurfaceView surface;
    private final MapView mapView;
    private final MapPreferences preferences;
    private final MapBehavior behavior = new MapBehavior();
    private final ExecutorService files = Executors.newSingleThreadExecutor();
    private final float density;
    private final Consumer<String> reportError;
    private final LocationRepository locations;
    private final LocationRepository.Listener locationListener = this::onLocation;
    private final AirspaceRenderer airspaces;
    private MapLibreMap map;
    private Style style;
    private MapTileOverlays tiles;
    private AircraftRenderer aircraft;
    private Location lastLocation;
    private double cameraZoom = Double.NaN;
    private boolean resumed;
    private boolean destroyed;

    public MapController(MapSurfaceView surface, LocationRepository locations, Openair openair,
                         Consumer<String> reportError) {
        this.surface = Objects.requireNonNull(surface, "mapView cannot be null");
        this.locations = Objects.requireNonNull(locations);
        this.reportError = Objects.requireNonNull(reportError);
        mapView = surface.nativeMapView();
        preferences = new MapPreferences(surface.getContext());
        density = surface.getResources().getDisplayMetrics().density;
        airspaces = new AirspaceRenderer(Objects.requireNonNull(openair), density);
        surface.zoomControls().setZoomListener(this::zoom);
        mapView.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                behavior.onMapTouchDown();
                if (map != null) map.cancelTransitions();
            }
            return false;
        });
    }

    public void initializeMap(@Nullable Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(ready -> {
            if (destroyed) return;
            map = ready;
            map.getUiSettings().setLogoEnabled(false);
            map.getUiSettings().setAttributionEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setRotateGesturesEnabled(false);
            map.getUiSettings().setTiltGesturesEnabled(false);
            map.setMinZoomPreference(MapBehavior.toNativeZoom(MapBehavior.MIN_ZOOM, density));
            map.setMaxZoomPreference(MapBehavior.toNativeZoom(MapBehavior.MAX_ZOOM, density));
            map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(0, 0))
                    .zoom(MapBehavior.toNativeZoom(MapBehavior.INITIAL_ZOOM, density))
                    .bearing(0).tilt(0).build()));
            map.addOnCameraMoveListener(this::onCameraMove);
            map.addOnMapClickListener(point -> behavior.isOpenTopo() && style != null
                    && airspaces.contains(map, point));
            onCameraMove();
            loadTiles();
        });
    }

    private void loadTiles() {
        files.execute(() -> {
            try {
                MapTileOverlays prepared = new MapTileOverlays(preferences.mapsDirectory);
                surface.post(() -> {
                    if (destroyed) return;
                    tiles = prepared;
                    installStyle();
                });
            } catch (Exception exception) {
                surface.post(() -> {
                    if (!destroyed) {
                        reportError.accept("Could not load offline map data: " + exception.getMessage());
                    }
                });
            }
        });
    }

    private void installStyle() {
        // A local rendering description, with no remote style, sprite or glyph resources.
        map.setStyle(new Style.Builder().fromJson("{\"version\":8,\"sources\":{},\"layers\":[]}"), loaded -> {
            if (destroyed) return;
            try {
                tiles.addTo(loaded, behavior.isOpenTopo());
                aircraft = new AircraftRenderer(loaded, map, surface.getResources(), density);
                airspaces.addTo(loaded);
                style = loaded;
                applyVisibility();
                if (lastLocation != null) aircraft.updateLocation(lastLocation);
                centerOnLocation();
            } catch (RuntimeException exception) {
                reportError.accept("Could not initialize map rendering: " + exception.getMessage());
            }
        });
    }

    public void switchMapSource() {
        behavior.switchSource();
        applyVisibility();
    }

    public void enableFollowMyLocation() {
        behavior.follow();
        centerOnLocation();
    }

    public void startMap() { mapView.onStart(); }
    public void stopMap() { mapView.onStop(); }
    public void lowMemory() { mapView.onLowMemory(); }
    public void saveMapState(Bundle state) { mapView.onSaveInstanceState(state); }

    public void resumeMap() {
        if (destroyed || resumed) return;
        mapView.onResume();
        resumed = true;
        locations.addListener(locationListener);
    }

    public void pauseMap() {
        if (!resumed) return;
        resumed = false;
        locations.removeListener(locationListener);
        mapView.onPause();
    }

    public void destroyMap() {
        if (destroyed) return;
        destroyed = true;
        if (resumed) {
            resumed = false;
            locations.removeListener(locationListener);
        }
        files.shutdownNow();
        surface.releaseControls();
        mapView.setOnTouchListener(null);
        mapView.onDestroy();
        aircraft = null;
        style = null;
        map = null;
    }

    private void onLocation(Location location) {
        if (destroyed || !resumed) return;
        lastLocation = new Location(location);
        if (style != null) aircraft.updateLocation(lastLocation);
        centerOnLocation();
    }

    private void centerOnLocation() {
        if (style != null && lastLocation != null && behavior.isFollowing() && resumed) {
            map.animateCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())),
                    preferences.animationDuration);
        }
    }

    private void onCameraMove() {
        if (destroyed || map == null) return;
        double zoom = map.getCameraPosition().zoom;
        if (Double.compare(cameraZoom, zoom) == 0) return;
        cameraZoom = zoom;
        double legacyZoom = MapBehavior.toLegacyZoom(zoom, density);
        surface.zoomControls().updateEnabled(legacyZoom < MapBehavior.MAX_ZOOM - 0.0001,
                legacyZoom > MapBehavior.MIN_ZOOM + 0.0001);
        if (style != null) aircraft.updateZoom(legacyZoom);
    }

    private void applyVisibility() {
        if (style == null) return;
        tiles.setOpenTopo(style, behavior.isOpenTopo());
        airspaces.setVisible(behavior.isOpenTopo());
    }

    private void zoom(boolean zoomIn) {
        if (map == null) return;
        double zoom = MapBehavior.toLegacyZoom(map.getCameraPosition().zoom, density);
        double next = Math.max(MapBehavior.MIN_ZOOM, Math.min(MapBehavior.MAX_ZOOM, zoom + (zoomIn ? 1 : -1)));
        map.animateCamera(CameraUpdateFactory.zoomTo(MapBehavior.toNativeZoom(next, density)),
                preferences.animationDuration);
    }
}

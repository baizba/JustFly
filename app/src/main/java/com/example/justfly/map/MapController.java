package com.example.justfly.map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import com.example.justfly.R;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.overlay.AirspaceGeometry;
import com.example.justfly.dataformat.openair.overlay.GeoCoordinate;
import com.example.justfly.dataformat.openair.overlay.OpenairToOverlayMapper;
import com.example.justfly.gps.LocationRepository;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.FillLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.geojson.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import static org.maplibre.android.style.expressions.Expression.get;
import static org.maplibre.android.style.layers.Property.*;
import static org.maplibre.android.style.layers.PropertyFactory.*;

/**
 * Application-facing facade. All SDK objects, projections and style operations stay here,
 * while the location repository and aviation geometry remain independent of MapLibre.
 */
public final class MapController {
    private static final String AIRSPACES = "airspaces";
    private static final String AIRSPACE_FILL = "airspace-fill";
    private static final String AIRSPACE_LINE = "airspace-line";
    private static final String AIRCRAFT = "aircraft";
    private static final String HEADING = "heading";
    private final MapSurfaceView surface;
    private final MapView mapView;
    private final MapPreferences preferences;
    private final MapBehavior behavior = new MapBehavior();
    private final ExecutorService files = Executors.newSingleThreadExecutor();
    private final float density;
    private final Consumer<String> reportError;
    private final LocationRepository.Listener locationListener = this::onLocation;
    private MapLibreMap map;
    private Style style;
    private MapTileOverlays tiles;
    private LocationRepository locations;
    private Location lastLocation;
    private Bitmap aircraftBitmap;
    private List<AirspaceGeometry> airspaces = List.of();
    private boolean resumed;
    private boolean destroyed;
    private boolean subscribed;
    private boolean loadingStyle;

    public MapController(MapSurfaceView surface, Consumer<String> reportError) {
        this.surface = Objects.requireNonNull(surface, "mapView cannot be null");
        this.reportError = Objects.requireNonNull(reportError);
        mapView = surface.nativeMapView();
        preferences = new MapPreferences(surface.getContext());
        density = surface.getResources().getDisplayMetrics().density;
        surface.zoomControls().setZoomListener(this::zoom);
        mapView.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                behavior.onMapTouchDown();
                if (map != null) {
                    map.cancelTransitions();
                }
            }
            return false;
        });
    }

    public void initializeMap(@Nullable Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(ready -> {
            if (destroyed) {
                return;
            }
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
            map.addOnCameraMoveListener(this::updateProjectedAircraft);
            map.addOnCameraIdleListener(this::updateProjectedAircraft);
            map.addOnMapClickListener(point -> behavior.isOpenTopo() && style != null
                    && !map.queryRenderedFeatures(map.getProjection().toScreenLocation(point), AIRSPACE_FILL).isEmpty());
            loadStyleWhenReady();
        });
        files.execute(() -> {
            try {
                MapTileOverlays prepared = new MapTileOverlays(preferences.mapsDirectory);
                surface.post(() -> {
                    if (!destroyed) {
                        tiles = prepared;
                        loadStyleWhenReady();
                    }
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

    private void loadStyleWhenReady() {
        if (map == null || tiles == null || loadingStyle || destroyed) {
            return;
        }
        loadingStyle = true;
        // No remote style, sprite, glyph or mandatory online resource.
        map.setStyle(new Style.Builder().fromJson("{\"version\":8,\"sources\":{},\"layers\":[]}"), loaded -> {
            if (destroyed) {
                return;
            }
            try {
                style = loaded;
                tiles.addTo(style, behavior.isOpenTopo());
                addAircraftLayers();
                style.addSource(new GeoJsonSource(AIRSPACES, emptyFeatures()));
                style.addLayer(new FillLayer(AIRSPACE_FILL, AIRSPACES).withProperties(fillColor(Color.TRANSPARENT)));
                style.addLayer(new LineLayer(AIRSPACE_LINE, AIRSPACES).withProperties(
                        lineColor(Color.BLACK), lineWidth(10f / density), lineJoin(LINE_JOIN_MITER)));
                updateAirspaces();
                applyVisibility();
                updateAircraft();
                centerOnLocation();
            } catch (RuntimeException exception) {
                reportError.accept("Could not initialize map rendering: " + exception.getMessage());
            }
        });
    }

    public void showMyLocation(Resources resources, LocationRepository locations) {
        this.locations = Objects.requireNonNull(locations);
        aircraftBitmap = BitmapFactory.decodeResource(resources, R.drawable.airplane_icon_black);
        if (aircraftBitmap == null) {
            throw new IllegalStateException("Could not load aircraft icon");
        }
        if (resumed) {
            subscribe();
        }
    }

    public void addAirspaces(Openair openair) {
        airspaces = new OpenairToOverlayMapper().getPolygonAirspaces(openair);
        updateAirspaces();
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

    public void resumeMap(Context context) {
        mapView.onResume();
        resumed = true;
        subscribe();
    }

    public void pauseMap(Context context) {
        resumed = false;
        unsubscribe();
        mapView.onPause();
    }

    public void destroyMap() {
        destroyed = true;
        unsubscribe();
        files.shutdownNow();
        surface.releaseControls();
        mapView.setOnTouchListener(null);
        mapView.onDestroy();
        style = null;
        map = null;
        locations = null;
        aircraftBitmap = null;
    }

    private void subscribe() {
        if (!subscribed && locations != null && !destroyed) {
            subscribed = true;
            locations.addListener(locationListener);
        }
    }

    private void unsubscribe() {
        if (subscribed && locations != null) {
            locations.removeListener(locationListener);
            subscribed = false;
        }
    }

    private void onLocation(Location location) {
        if (destroyed || !resumed) {
            return;
        }
        lastLocation = new Location(location);
        updateAircraft();
        centerOnLocation();
    }

    private void centerOnLocation() {
        if (map != null && style != null && lastLocation != null && behavior.isFollowing() && resumed) {
            map.animateCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())),
                    preferences.animationDuration);
        }
    }

    private void addAircraftLayers() {
        if (aircraftBitmap != null) {
            style.addImage("aircraft-icon", aircraftBitmap);
        }
        style.addSource(new GeoJsonSource(AIRCRAFT, emptyFeatures()));
        style.addLayer(new CircleLayer("accuracy", AIRCRAFT).withProperties(
                circleColor(Color.rgb(100, 100, 255)), circleOpacity(50f / 255f),
                circleStrokeColor(Color.rgb(100, 100, 255)), circleStrokeOpacity(150f / 255f),
                circleStrokeWidth(1f / density), circleRadius(0f)));
        style.addLayer(new SymbolLayer("aircraft-icon-layer", AIRCRAFT).withProperties(
                iconImage("aircraft-icon"), iconAnchor(ICON_ANCHOR_CENTER),
                iconAllowOverlap(true), iconIgnorePlacement(true),
                iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP), iconRotate(get("bearing"))));
        style.addSource(new GeoJsonSource(HEADING, emptyFeatures()));
        style.addLayer(new LineLayer("heading-line", HEADING).withProperties(
                lineColor(Color.BLUE), lineWidth(MapBehavior.HEADING_WIDTH_PX / density),
                lineCap(LINE_CAP_BUTT)));
    }

    private void updateAircraft() {
        if (style == null || lastLocation == null || style.getSource(AIRCRAFT) == null) {
            return;
        }
        Feature aircraft = Feature.fromGeometry(Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude()));
        aircraft.addNumberProperty("bearing", lastLocation.hasBearing() ? lastLocation.getBearing() : 0);
        ((GeoJsonSource) style.getSource(AIRCRAFT)).setGeoJson(aircraft);
        updateProjectedAircraft();
    }

    private void updateProjectedAircraft() {
        if (destroyed || map == null) {
            return;
        }
        double legacyZoom = MapBehavior.toLegacyZoom(map.getCameraPosition().zoom, density);
        surface.zoomControls().updateEnabled(legacyZoom < MapBehavior.MAX_ZOOM - 0.0001,
                legacyZoom > MapBehavior.MIN_ZOOM + 0.0001);
        if (style == null || lastLocation == null || style.getLayer("accuracy") == null) {
            return;
        }
        float radius = (float) (lastLocation.getAccuracy()
                / MapBehavior.metersPerPixel(lastLocation.getLatitude(), legacyZoom, density) / density);
        style.getLayer("accuracy").setProperties(circleRadius(radius));
        GeoJsonSource heading = (GeoJsonSource) style.getSource(HEADING);
        if (!lastLocation.hasBearing()) {
            heading.setGeoJson(emptyFeatures());
            return;
        }
        LatLng location = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        PointF start = map.getProjection().toScreenLocation(location);
        PointF end = new PointF((float) MapBehavior.headingX(start.x, lastLocation.getBearing()),
                (float) MapBehavior.headingY(start.y, lastLocation.getBearing()));
        LatLng endpoint = map.getProjection().fromScreenLocation(end);
        heading.setGeoJson(LineString.fromLngLats(List.of(
                Point.fromLngLat(location.getLongitude(), location.getLatitude()),
                Point.fromLngLat(endpoint.getLongitude(), endpoint.getLatitude()))));
    }

    private void updateAirspaces() {
        if (style == null || style.getSource(AIRSPACES) == null) {
            return;
        }
        List<Feature> features = new ArrayList<>();
        for (AirspaceGeometry geometry : airspaces) {
            List<Point> points = new ArrayList<>();
            for (GeoCoordinate point : geometry.points()) {
                points.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }
            if (points.size() < 2) {
                continue;
            }
            Feature feature = Feature.fromGeometry(points.size() >= 4
                    ? Polygon.fromLngLats(List.of(points)) : LineString.fromLngLats(points));
            feature.addStringProperty("name", geometry.airspace().getAirspaceName());
            feature.addStringProperty("class", String.valueOf(geometry.airspace().getAirspaceClass()));
            feature.addStringProperty("altitudeLow", geometry.airspace().getAltitudeLow());
            feature.addStringProperty("altitudeHigh", geometry.airspace().getAltitudeHigh());
            features.add(feature);
        }
        ((GeoJsonSource) style.getSource(AIRSPACES)).setGeoJson(FeatureCollection.fromFeatures(features));
    }

    private void applyVisibility() {
        if (style == null || tiles == null || style.getLayer(AIRSPACE_LINE) == null) {
            return;
        }
        tiles.setOpenTopo(style, behavior.isOpenTopo());
        String visibility = behavior.isOpenTopo() ? VISIBLE : NONE;
        style.getLayer(AIRSPACE_FILL).setProperties(visibility(visibility));
        style.getLayer(AIRSPACE_LINE).setProperties(visibility(visibility));
    }

    private void zoom(boolean zoomIn) {
        if (map == null) {
            return;
        }
        double zoom = MapBehavior.toLegacyZoom(map.getCameraPosition().zoom, density);
        double next = Math.max(MapBehavior.MIN_ZOOM, Math.min(MapBehavior.MAX_ZOOM, zoom + (zoomIn ? 1 : -1)));
        map.animateCamera(CameraUpdateFactory.zoomTo(MapBehavior.toNativeZoom(next, density)),
                preferences.animationDuration);
    }

    private static FeatureCollection emptyFeatures() {
        return FeatureCollection.fromFeatures(new Feature[0]);
    }
}

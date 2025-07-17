package com.example.justfly.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.justfly.R;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.overlay.OpenairToOverlayMapper;
import com.example.justfly.overlay.DirectionLineOverlay;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class MapController {

    private final MapView mapView;
    private MyLocationNewOverlay myLocationNewOverlay;
    private MapTileOverlays mapTileOverlays;

    public MapController(MapView mapView) {
        Objects.requireNonNull(mapView, "mapView cannot be null");
        this.mapView = mapView;
    }

    public void initializeMap() {
        //mapView.setTileSource(OPEN_VFR);
        mapView.setUseDataConnection(false);
        mapView.getController().setZoom(11.0);
        mapView.setMinZoomLevel(4.0);
        mapView.setMaxZoomLevel(14.0);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);

        //add map tiles
        mapTileOverlays = new MapTileOverlays(mapView.getContext());
        mapView.getOverlays().addAll(mapTileOverlays.getOverlays().values());
    }

    public void resumeMap(Context context) {
        handlePreferences(Configuration.getInstance()::load, context);
        mapView.onResume();
        myLocationNewOverlay.enableMyLocation();
    }

    public void pauseMap(Context context) {
        handlePreferences(Configuration.getInstance()::save, context);
        myLocationNewOverlay.disableMyLocation();
        mapView.onPause();
    }

    public void showMyLocation(Resources resources) {
        myLocationNewOverlay = new MyLocationNewOverlay(mapView);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();

        Bitmap planeBitmap = BitmapFactory.decodeResource(resources, R.drawable.airplane_icon_black);
        if (planeBitmap != null) {
            myLocationNewOverlay.setPersonIcon(planeBitmap);  // when standing
            myLocationNewOverlay.setDirectionIcon(planeBitmap); // when moving
            myLocationNewOverlay.setPersonAnchor(0.5f, 0.5f); // center the plane icon
            myLocationNewOverlay.setDirectionAnchor(0.5f, 0.5f); // center the plane icon
        } else {
            Log.e(this.getClass().getName(), "Could not load plane icon");
        }

        mapView.getOverlays().add(myLocationNewOverlay);
        DirectionLineOverlay directionLineOverlay = new DirectionLineOverlay(myLocationNewOverlay);
        mapView.getOverlays().add(directionLineOverlay);
    }

    public void addAirspaces(Openair openair) {
        OpenairToOverlayMapper openairToOverlayMapper = new OpenairToOverlayMapper();
        List<Polygon> airspaces = openairToOverlayMapper.getPolygonAirspaces(openair);
        //initially hide them because OpenVFR is loaded at start
        airspaces.forEach(airspace -> airspace.setEnabled(false));
        mapView.getOverlays().addAll(airspaces);
    }

    public void switchMapSource() {
        mapView
                .getOverlays()
                .stream()
                .filter(o -> o instanceof TilesOverlay)
                .forEach(o -> o.setEnabled(!o.isEnabled()));

        //show airspaces only for OpenTopo maps
        Overlay openTopo = mapTileOverlays.getOverlays().get("openTopo");
        boolean openTopoEnabled = openTopo != null && openTopo.isEnabled();
        mapView.getOverlays()
                .stream()
                .filter(o -> o instanceof Polygon)
                .forEach(o -> o.setEnabled(openTopoEnabled));
    }

    public void enableFollowMyLocation() {
        myLocationNewOverlay.enableFollowLocation();
    }

    private void handlePreferences(BiConsumer<Context, SharedPreferences> operation, Context ctx) {
        String preferenceFileName = ctx.getPackageName() + "_preferences";
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        operation.accept(ctx, sharedPreferences);
    }

}

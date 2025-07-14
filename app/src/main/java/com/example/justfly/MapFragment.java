package com.example.justfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.overlay.OpenairToOverlayMapper;
import com.example.justfly.dataformat.openair.parser.OpenairParser;
import com.example.justfly.map.MapTileOverlays;
import com.example.justfly.overlay.DirectionLineOverlay;
import com.example.justfly.util.ResourceFileUtil;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;
import java.util.function.BiConsumer;

public class MapFragment extends Fragment {

    private MyLocationNewOverlay myLocationNewOverlay;
    private MapView mapView;
    private MapTileOverlays mapTileOverlays;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        handlePreferences(Configuration.getInstance()::load, view.getContext());
        mapView = initializeMap(view);
        showMyLocation();
        List<String> openairData = ResourceFileUtil.readResourceFile("openair/lo_airspaces.openair.txt");
        OpenairParser parser = new OpenairParser();
        Openair openair = parser.parse(openairData);
        this.addAirspaces(openair);
        view.findViewById(R.id.btnFollowMe).setOnClickListener(v -> this.enableFollowMyLocation());
        view.findViewById(R.id.btnSwitchMap).setOnClickListener(v -> this.switchMapSource());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handlePreferences(Configuration.getInstance()::load, requireContext());
        if (mapView != null) {
            mapView.onResume();
            myLocationNewOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handlePreferences(Configuration.getInstance()::save, requireContext());
        if (mapView != null) {
            myLocationNewOverlay.disableMyLocation();
            mapView.onPause();
        }
    }

    public void showMyLocation() {
        myLocationNewOverlay = new MyLocationNewOverlay(mapView);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();

        Bitmap planeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.airplane_icon_black);
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

    private void switchMapSource() {
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

    private void handlePreferences(BiConsumer<Context, SharedPreferences> operation, Context ctx) {
        String preferenceFileName = ctx.getPackageName() + "_preferences";
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        operation.accept(ctx, sharedPreferences);
    }

    private void enableFollowMyLocation() {
        myLocationNewOverlay.enableFollowLocation();
    }

    private MapView initializeMap(View view) {
        MapView mapView = view.findViewById(R.id.map);
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

        return mapView;
    }
}
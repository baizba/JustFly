package com.example.justfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.livedata.GpsData;
import com.example.justfly.map.AirspaceView;
import com.example.justfly.map.MapConstants;
import com.example.justfly.map.MapPresenter;
import com.example.justfly.overlay.DirectionLineOverlay;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MapFragment extends Fragment implements AirspaceView, MapConstants {

    private MyLocationNewOverlay myLocationNewOverlay;
    private Consumer<GpsData> gpsDataConsumer;
    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        handlePreferences(Configuration.getInstance()::load, view.getContext());
        MainActivity mainActivity = (MainActivity) Objects.requireNonNull(getActivity(), "MainActivity cannot be null");
        mapView = initializeMap(view);
        gpsDataConsumer = mainActivity::updateGpsData;
        MapPresenter mapPresenter = new MapPresenter(this);
        mapPresenter.showAirspaces();
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

    @Override
    public void showMyLocation() {
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
        DirectionLineOverlay directionLineOverlay = new DirectionLineOverlay(myLocationNewOverlay);
        mapView.getOverlays().add(directionLineOverlay);
    }

    @Override
    public void showAirspaces(Openair openair) {
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
        mapView.getOverlays().addAll(polygonAirspaces);
    }

    private void switchMapSource() {
        ITileSource tileSource = mapView.getTileProvider().getTileSource();
        if (OPEN_VFR.name().equals(tileSource.name())) {
            mapView.setTileSource(TileSourceFactory.OpenTopo);
        } else {
            mapView.setTileSource(OPEN_VFR);
        }
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
        mapView.setTileSource(OPEN_VFR);
        mapView.setUseDataConnection(false);
        mapView.getController().setZoom(11.0);
        mapView.setMinZoomLevel(4.0);
        mapView.setMaxZoomLevel(14.0);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        return mapView;
    }
}
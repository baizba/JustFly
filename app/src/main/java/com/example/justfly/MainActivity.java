package com.example.justfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justfly.permission.LocationPermissionHandler;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MyLocationNewOverlay myLocationNewOverlay;
    private LocationPermissionHandler locationPermissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPreferences();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupInsets();

        locationPermissionHandler = new LocationPermissionHandler(this);
        if (!locationPermissionHandler.hasLocationPermission()) {
            locationPermissionHandler.requestLocationPermission();
        } else {
            initializeMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionHandler.onRequestPermissionsResult(
                requestCode,
                grantResults,
                this::initializeMap,
                this::showLocationRequestMessage
        );
    }

    @Override
    protected void onResume() {
        myLocationNewOverlay.enableMyLocation();
        super.onResume();
        loadPreferences();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myLocationNewOverlay.disableMyLocation();
        savePreferences();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    private void initializeMap() {
        //map configuration
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(10.0);
        mapView.setMinZoomLevel(5.0);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapView.getController().setCenter(startPoint);

        //location tracking
        initializeMyLocationOverlay();
    }

    private void showLocationRequestMessage() {
        Toast.makeText(this, R.string.locationRequestMessage, Toast.LENGTH_SHORT).show();
    }

    private void initializeMyLocationOverlay() {
        myLocationNewOverlay = new MyLocationNewOverlay(mapView);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationNewOverlay);
    }

    private void savePreferences() {
        String preferenceFileName = getPackageName() + "_preferences";
        SharedPreferences sharedPreferences = getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        Context ctx = getApplicationContext();
        Configuration.getInstance().save(ctx, sharedPreferences);
    }

    private void loadPreferences() {
        String preferenceFileName = getPackageName() + "_preferences";
        SharedPreferences sharedPreferences = getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, sharedPreferences);
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
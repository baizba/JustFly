package com.example.justfly;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private MapView mapView;
    private MyLocationNewOverlay myLocationNewOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPreferences();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupInsets();

        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
            initializeMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                // Permission denied, show a message to the user
                String message = "Location permission is required for this app to function properly";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
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

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        String[] permissionsToRequest = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, permissionsToRequest, REQUEST_LOCATION_PERMISSION);
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
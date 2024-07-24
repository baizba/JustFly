package com.example.justfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPreferences();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupInsets();

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(10.0);
        mapView.setMinZoomLevel(5.0);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapView.getController().setCenter(startPoint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePreferences();
        if (mapView != null) {
            mapView.onPause();
        }
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
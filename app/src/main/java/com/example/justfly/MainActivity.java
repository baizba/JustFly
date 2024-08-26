package com.example.justfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justfly.handler.LocationPermissionHandler;
import com.example.justfly.map.MapFacade;

import org.osmdroid.config.Configuration;

import java.util.function.BiConsumer;

public class MainActivity extends AppCompatActivity {

    private LocationPermissionHandler locationPermissionHandler;
    private MapFacade mapFacade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        handlePreferences(Configuration.getInstance()::load);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupInsets();

        findViewById(R.id.btnFollowMe).setOnClickListener(v -> mapFacade.enableFollowMyLocation());

        locationPermissionHandler = new LocationPermissionHandler(this);
        if (!locationPermissionHandler.hasLocationPermission()) {
            locationPermissionHandler.requestLocationPermission();
        } else {
            mapFacade = new MapFacade(findViewById(R.id.map));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionHandler.onRequestPermissionsResult(
                requestCode,
                grantResults,
                () -> mapFacade = new MapFacade(findViewById(R.id.map)),
                this::showLocationRequestMessage
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        handlePreferences(Configuration.getInstance()::load);
        mapFacade.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handlePreferences(Configuration.getInstance()::save);
        mapFacade.pause();
    }

    private void showLocationRequestMessage() {
        Toast.makeText(this, R.string.locationRequestMessage, Toast.LENGTH_SHORT).show();
    }

    private void handlePreferences(BiConsumer<Context, SharedPreferences> operation) {
        String preferenceFileName = getPackageName() + "_preferences";
        SharedPreferences sharedPreferences = getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        Context ctx = getApplicationContext();
        operation.accept(ctx, sharedPreferences);
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
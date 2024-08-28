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
import androidx.fragment.app.FragmentTransaction;

import com.example.justfly.handler.LocationPermissionHandler;

import org.osmdroid.config.Configuration;

import java.util.function.BiConsumer;

public class MainActivity extends AppCompatActivity {

    private LocationPermissionHandler locationPermissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        locationPermissionHandler = new LocationPermissionHandler(this);
        if (!locationPermissionHandler.hasLocationPermission()) {
            locationPermissionHandler.requestLocationPermission();
        } else {
            loadFragments();
        }

        handlePreferences(Configuration.getInstance()::load);
        EdgeToEdge.enable(this);
        setupInsets();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionHandler.onRequestPermissionsResult(
                requestCode,
                grantResults,
                this::loadFragments,
                this::showLocationRequestMessage
        );
    }

    private void loadFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mapFragmentContainer, new Map());
        transaction.replace(R.id.gpsDataFragmentContainer, GpsData.newInstance("p1", "p2"));
        transaction.commit();
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
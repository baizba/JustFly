package com.example.justfly.handler;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LocationPermissionHandler {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private final AppCompatActivity activity;

    public LocationPermissionHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        String[] permissionsToRequest = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(activity, permissionsToRequest, REQUEST_LOCATION_PERMISSION);
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults, VoidCallback granted, VoidCallback denied) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                granted.execute();
            } else {
                denied.execute();
            }
        }
    }
}

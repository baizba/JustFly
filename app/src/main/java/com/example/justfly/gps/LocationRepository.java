package com.example.justfly.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Single fused-location owner for the map overlay, HUD, and GPX recorder.
 * GPS updates run while at least one listener is registered.
 */
public class LocationRepository {

    public interface Listener {
        void onLocation(@NonNull Location location);
    }

    private static final String TAG = LocationRepository.class.getSimpleName();
    private static final long LOCATION_UPDATE_INTERVAL_MS = 500;

    private final Context appContext;
    private final FusedLocationProviderClient fusedLocationClient;
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<>();
    private final LocationCallback locationCallback;

    @Nullable
    private Location lastLocation;
    private boolean updatesStarted;

    public LocationRepository(Context context) {
        this.appContext = Objects.requireNonNull(context, "context").getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext);
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        lastLocation = location;
                        for (Listener listener : listeners) {
                            listener.onLocation(location);
                        }
                    }
                }
            }
        };
    }

    public void addListener(Listener listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
        startUpdatesIfNeeded();
        Location snapshot = lastLocation;
        if (snapshot != null) {
            listener.onLocation(snapshot);
        }
    }

    public void removeListener(Listener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            stopUpdates();
        }
    }

    @Nullable
    public Location getLastLocation() {
        return lastLocation;
    }

    private synchronized void startUpdatesIfNeeded() {
        if (updatesStarted) {
            return;
        }
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted. Cannot start location updates.");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL_MS)
                .build();
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            updatesStarted = true;
            Log.i(TAG, "Requested location updates from FusedLocationProvider.");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while requesting location updates.", e);
        }
    }

    private synchronized void stopUpdates() {
        if (!updatesStarted) {
            return;
        }
        fusedLocationClient.removeLocationUpdates(locationCallback);
        updatesStarted = false;
        Log.i(TAG, "Stopped location updates.");
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}

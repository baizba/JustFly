package com.example.justfly;

import android.app.Application;
import android.content.Context;

import com.example.justfly.gps.LocationRepository;

public class JustFlyApp extends Application {

    private LocationRepository locationRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        locationRepository = new LocationRepository(this);
    }

    public LocationRepository getLocationRepository() {
        return locationRepository;
    }

    public static LocationRepository getLocationRepository(Context context) {
        return ((JustFlyApp) context.getApplicationContext()).getLocationRepository();
    }
}

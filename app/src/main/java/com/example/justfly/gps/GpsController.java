package com.example.justfly.gps;

import android.location.Location;
import android.os.Build;
import android.widget.TextView;

import com.example.justfly.util.UnitConversionUtil;

import java.util.Locale;
import java.util.Objects;

public class GpsController {

    private static final Locale DEFAULT_LOCALE = Locale.GERMANY;

    private final LocationRepository locationRepository;
    private LocationRepository.Listener locationListener;

    public GpsController(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository");
    }

    public void subscribeToGpsUpdates(TextView speedView, TextView altitudeView) {
        unsubscribeFromGpsUpdates();
        locationListener = location -> updateHud(speedView, altitudeView, location);
        locationRepository.addListener(locationListener);
    }

    public void unsubscribeFromGpsUpdates() {
        if (locationListener != null) {
            locationRepository.removeListener(locationListener);
            locationListener = null;
        }
    }

    private void updateHud(TextView speedView, TextView altitudeView, Location location) {
        double altitude;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && location.hasMslAltitude()) {
            altitude = location.getMslAltitudeMeters();
        } else {
            altitude = location.getAltitude();
        }
        long knots = UnitConversionUtil.msToKnots(location.getSpeed());
        long feet = UnitConversionUtil.metersToFeet(altitude);
        speedView.setText(String.format(DEFAULT_LOCALE, "%d KT", knots));
        altitudeView.setText(String.format(DEFAULT_LOCALE, "%d FT", feet));
    }
}

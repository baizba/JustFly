package com.example.justfly.gps;

import android.content.Context;
import android.os.Build;
import android.widget.TextView;

import com.example.justfly.util.UnitConversionUtil;

import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;

import java.util.Locale;

public class GpsController {

    private static final Locale DEFAULT_LOCALE = Locale.GERMANY;

    public void subscribeToGpsUpdates(TextView speedView, TextView altitudeView, Context context) {
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
        gpsMyLocationProvider.setLocationUpdateMinTime(500);
        gpsMyLocationProvider.startLocationProvider(getGpsUpdater(speedView, altitudeView));
    }

    public IMyLocationConsumer getGpsUpdater(TextView speedView, TextView altitudeView) {
        return (location, source) -> {
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
        };
    }
}

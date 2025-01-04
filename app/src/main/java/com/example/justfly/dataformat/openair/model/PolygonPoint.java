package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

import java.util.Locale;

public class PolygonPoint {

    private final double latitude;
    private final double longitude;

    public PolygonPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.GERMANY, "PolygonPoint{lat=%f, lon=%f}", latitude, longitude);
    }
}

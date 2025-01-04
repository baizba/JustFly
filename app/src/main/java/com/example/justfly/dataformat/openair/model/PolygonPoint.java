package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

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
        return "PolygonPoint{" +
                "lat=" + latitude +
                ", lon=" + longitude +
                '}';
    }
}

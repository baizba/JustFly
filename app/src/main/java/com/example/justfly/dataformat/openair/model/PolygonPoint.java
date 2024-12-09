package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

public class PolygonPoint {

    private double lat;
    private double lon;

    public PolygonPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @NonNull
    @Override
    public String toString() {
        return "PolygonPoint{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}

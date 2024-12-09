package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

public class PolygonPoint {

    private long lat;
    private long lon;

    @NonNull
    @Override
    public String toString() {
        return "PolygonPoint{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}

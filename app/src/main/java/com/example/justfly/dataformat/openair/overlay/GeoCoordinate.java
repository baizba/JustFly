package com.example.justfly.dataformat.openair.overlay;

/** A geographic coordinate independent of the map rendering SDK. */
public record GeoCoordinate(double latitude, double longitude) {
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}

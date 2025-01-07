package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Circle {

    private double radius;
    private double latitude;
    private double longitude;

    public Circle(double radius, double latitude, double longitude) {
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.GERMANY,"Circle{radius=%.2f, latitude=%.6f, longitude=%.6f}", radius, latitude, longitude);
    }
}

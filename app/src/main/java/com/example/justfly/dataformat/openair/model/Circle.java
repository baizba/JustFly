package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

public class Circle {

    double radius;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @NonNull
    @Override
    public String toString() {
        return "Circle{" +
                "radius=" + radius +
                '}';
    }
}

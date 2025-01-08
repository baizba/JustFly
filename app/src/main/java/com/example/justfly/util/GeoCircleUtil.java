package com.example.justfly.util;

import com.example.justfly.dataformat.openair.model.Circle;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class GeoCircleUtil {

    public static List<GeoPoint> getCirclePoints(Circle circle) {
        // Number of points to approximate the circle
        int points = 360;

        // Create a list of GeoPoints for the circle
        List<GeoPoint> circlePoints = new ArrayList<>();

        // Earth's radius in meters
        double earthRadius = 6371000;

        // Get the radius of the circle in meters
        double radiusMeters = UnitConversionUtil.milesToMeters(circle.getRadius());

        // Generate points for the circle
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i);
            double latitudeOffset = Math.toDegrees(radiusMeters / earthRadius) * Math.cos(angle);
            double longitudeOffset = Math.toDegrees(radiusMeters / earthRadius / Math.cos(Math.toRadians(circle.getLatitude()))) * Math.sin(angle);

            double newLatitude = circle.getLatitude() + latitudeOffset;
            double newLongitude = circle.getLongitude() + longitudeOffset;

            circlePoints.add(new GeoPoint(newLatitude, newLongitude));
        }

        //add the first point to the end to close the circle to make sure polygon is closed
        circlePoints.add(circlePoints.get(0));
        return circlePoints;
    }
}

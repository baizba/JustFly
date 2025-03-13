package com.example.justfly.util;

import com.example.justfly.dataformat.openair.model.Arc;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to generate points for an arc.
 * We have an arc with a center point, and start and end points.
 * These two points are start and end of the arc.
 * We need math to calculate list of points to draw an arc in OSM.
 */
public class GeoArcUtil {
    private static final int EARTH_RADIUS_METERS = 6371000;

    public static List<GeoPoint> getArcPoints(Arc arc) {
        // Convert degrees to radians
        double centerLatRad = Math.toRadians(arc.getCenterLatitude());
        double centerLonRad = Math.toRadians(arc.getCenterLongitude());
        double startLatRad = Math.toRadians(arc.getStartLatitude());
        double startLonRad = Math.toRadians(arc.getStartLongitude());
        double endLatRad = Math.toRadians(arc.getEndLatitude());
        double endLonRad = Math.toRadians(arc.getEndLongitude());

        // Calculate the radius (distance between center and start)
        double radius = haversineDistance(
                arc.getCenterLatitude(),
                arc.getCenterLongitude(),
                arc.getStartLatitude(),
                arc.getStartLongitude());

        // Calculate start and end angles
        double startAngle = Math.atan2(Math.sin(startLonRad - centerLonRad) * Math.cos(startLatRad),
                Math.cos(centerLatRad) * Math.sin(startLatRad) -
                        Math.sin(centerLatRad) * Math.cos(startLatRad) *
                                Math.cos(startLonRad - centerLonRad));

        double endAngle = Math.atan2(Math.sin(endLonRad - centerLonRad) * Math.cos(endLatRad),
                Math.cos(centerLatRad) * Math.sin(endLatRad) -
                        Math.sin(centerLatRad) * Math.cos(endLatRad) *
                                Math.cos(endLonRad - centerLonRad));

        // Normalize angles to [0, 2π]
        if (startAngle < 0) startAngle += 2 * Math.PI;
        if (endAngle < 0) endAngle += 2 * Math.PI;

        // Determine clockwise or counterclockwise drawing
        boolean isClockwise = endAngle > startAngle;

        // Generate points along the arc
        List<GeoPoint> arcPoints = new ArrayList<>();
        int pointsCount = 100; // More points = smoother arc
        double step = (isClockwise ? 1 : -1) * Math.abs(endAngle - startAngle) / pointsCount;

        for (int i = 0; i <= pointsCount; i++) {
            double angle = startAngle + i * step;
            double lat = arc.getCenterLatitude() + (radius / EARTH_RADIUS_METERS) * Math.cos(angle); // Earth's radius in meters
            double lon = arc.getCenterLongitude() + (radius / EARTH_RADIUS_METERS) * Math.sin(angle) / Math.cos(centerLatRad);
            arcPoints.add(new GeoPoint(Math.toDegrees(lat), Math.toDegrees(lon)));
        }

        // Create and add Polyline to the map
        //Polygon arcPolyline = new Polygon();
        //arcPolyline.setPoints(arcPoints);

        return arcPoints;
    }

    // Calculate distance between two coordinates (Haversine formula)
    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}

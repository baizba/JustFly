package com.example.justfly.util;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.Arc;
import com.example.justfly.dataformat.openair.model.DrawingDirection;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

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
        List<GeoPoint> arcPoints = new ArrayList<>();

        // Calculate radius in meters (from center to start point)
        double radius = haversineDistance(
                arc.getCenterLatitude(), arc.getCenterLongitude(),
                arc.getStartLatitude(), arc.getStartLongitude());

        // Compute initial and final bearings (in degrees)
        double startBearing = calculateBearing(arc.getCenterLatitude(), arc.getCenterLongitude(),
                arc.getStartLatitude(), arc.getStartLongitude());
        double endBearing = calculateBearing(arc.getCenterLatitude(), arc.getCenterLongitude(),
                arc.getEndLatitude(), arc.getEndLongitude());

        // Normalize to [0, 360)
        startBearing = (startBearing + 360) % 360;
        endBearing = (endBearing + 360) % 360;

        // Handle wrap-around if needed
        double angleStep = 1.0; // 1° step = ~100 points for 90° arc
        double totalAngle = isClockwise(arc)
                ? ((endBearing >= startBearing) ? endBearing - startBearing : 360 - (startBearing - endBearing))
                : ((startBearing >= endBearing) ? startBearing - endBearing : 360 - (endBearing - startBearing));

        int steps = (int) (totalAngle / angleStep);
        for (int i = 0; i <= steps; i++) {
            double bearingDeg = isClockwise(arc)
                    ? (startBearing + i * angleStep) % 360
                    : (startBearing - i * angleStep + 360) % 360;

            GeoPoint point = calculateDestinationPoint(
                    arc.getCenterLatitude(), arc.getCenterLongitude(),
                    bearingDeg, radius);
            arcPoints.add(point);
        }

        return arcPoints;
    }

    public static Polygon buildCombinedArcPolygon(Airspace airspace) {
        Arc arc1 = airspace.getArcs().get(0);
        Arc arc2 = airspace.getArcs().get(1);

        List<GeoPoint> arc1Points = getArcPoints(arc1);
        List<GeoPoint> arc2Points = getArcPoints(arc2);

        GeoPoint arc1End = arc1Points.get(arc1Points.size() - 1);
        GeoPoint arc2Start = arc2Points.get(0);
        GeoPoint arc2End = arc2Points.get(arc2Points.size() - 1);
        GeoPoint arc1Start = arc1Points.get(0);

        // Combine: arc1 → line to arc2 start → arc2 → line to arc1 start
        List<GeoPoint> fullPolygonPoints = new ArrayList<>();
        fullPolygonPoints.addAll(arc1Points);
        fullPolygonPoints.add(arc2Start);
        fullPolygonPoints.addAll(arc2Points);
        fullPolygonPoints.add(arc1Start);

        Polygon polygon = new Polygon();
        polygon.setPoints(fullPolygonPoints);
        return polygon;
    }

    private static boolean isClockwise(Arc arc) {
        return DrawingDirection.CLOCKWISE.equals(arc.getDirection());
    }


    private static GeoPoint calculateDestinationPoint(double lat, double lon, double bearingDeg, double distanceMeters) {
        double angularDistance = distanceMeters / EARTH_RADIUS_METERS;
        double bearingRad = Math.toRadians(bearingDeg);
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);

        double destLatRad = Math.asin(Math.sin(latRad) * Math.cos(angularDistance) +
                Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(bearingRad));

        double destLonRad = lonRad + Math.atan2(
                Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(latRad),
                Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(destLatRad)
        );

        return new GeoPoint(Math.toDegrees(destLatRad), Math.toDegrees(destLonRad));
    }

    private static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);

        return Math.toDegrees(Math.atan2(y, x));
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

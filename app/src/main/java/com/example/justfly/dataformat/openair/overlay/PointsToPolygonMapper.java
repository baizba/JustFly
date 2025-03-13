package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Airspace;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * polygonal airspaces (they consist only of geopoints)
 */
public class PointsToPolygonMapper {

    public Polygon toPolygons(Airspace airspace) {
        List<GeoPoint> geoPoints = airspace.getPolygonPoints()
                .stream()
                .map(polygonPoint -> new GeoPoint(polygonPoint.getLatitude(), polygonPoint.getLongitude()))
                .collect(Collectors.toCollection(ArrayList::new));
        geoPoints.add(geoPoints.get(0));//add the first point at the end to add to make sure the overlay is closed

        Polygon polygon = new Polygon();
        polygon.setPoints(geoPoints);
        return polygon;
    }

}

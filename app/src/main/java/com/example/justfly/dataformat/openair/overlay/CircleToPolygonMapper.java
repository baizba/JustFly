package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.util.GeoCircleUtil;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Circular airspaces
 */
public class CircleToPolygonMapper {

    public List<Polygon> toPolygons(Airspace airspace) {
        return airspace.getCircles()
                .stream()
                .map(circle -> {
                    List<GeoPoint> circlePoints = GeoCircleUtil.getCirclePoints(circle);
                    Polygon circlePolygon = new Polygon();
                    circlePolygon.setPoints(circlePoints);
                    return circlePolygon;
                })
                .collect(Collectors.toList());
    }
}

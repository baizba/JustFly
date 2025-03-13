package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.util.GeoArcUtil;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Arc-shaped airspaces consist of a center point, a start point, and an end point.
 * We need to calculate the radius and the angles to draw the arc.
 */
public class ArcToPolygonMapper {

    public List<Polygon> toPolygons(Airspace airspace) {
        return airspace.getArcs()
                .stream()
                .map(arc -> {
                    List<GeoPoint> arcPoints = GeoArcUtil.getArcPoints(arc);
                    Polygon arcPolyline = new Polygon();
                    arcPolyline.setPoints(arcPoints);
                    return arcPolyline;
                })
                .collect(Collectors.toList());
    }

}

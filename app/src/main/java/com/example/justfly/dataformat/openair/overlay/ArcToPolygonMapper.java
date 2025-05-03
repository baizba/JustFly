package com.example.justfly.dataformat.openair.overlay;

import android.util.Log;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.Arc;
import com.example.justfly.util.GeoArcUtil;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;

/**
 * Arc-shaped airspaces consist of a center point, a start point, and an end point.
 * We need to calculate the radius and the angles to draw the arc.
 */
public class ArcToPolygonMapper {

    private static final String TAG = ArcToPolygonMapper.class.getName();

    public Polygon toPolygon(Airspace airspace) {
        Log.d(TAG, "Airspace: " + airspace);
        Log.d(TAG, "Airspace Arcs: " + airspace.getArcs());

        if (airspace.getArcs().size() == 1) {
            Arc arc = airspace.getArcs().get(0);
            List<GeoPoint> points = GeoArcUtil.getArcPoints(arc);
            return createPolygon(points);
        } else if (airspace.getArcs().size() == 2) {
            List<GeoPoint> points = GeoArcUtil.buildCombinedArcPolygon(airspace);
            return createPolygon(points);
        } else {
            Log.e(TAG, "Unsupported arc count: " + airspace.getArcs().size());
            return null;
        }
    }

    private Polygon createPolygon(List<GeoPoint> points) {
        Polygon polygon = new Polygon();
        polygon.setPoints(points);
        return polygon;
    }

}

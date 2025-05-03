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

    public List<Polygon> toPolygons(Airspace airspace) {
        String tag = this.getClass().getName();
        Log.i(tag, "Airspace: " + airspace);
        Log.i(tag, "Airspace Arcs: " + airspace.getArcs());

        if (airspace.getArcs().size() == 1) {
            Arc arc = airspace.getArcs().get(0);
            List<GeoPoint> arcPoints = GeoArcUtil.getArcPoints(arc);
            Polygon arcPolyline = new Polygon();
            arcPolyline.setPoints(arcPoints);
            return List.of(arcPolyline);
        } else if (airspace.getArcs().size() == 2) {
            Polygon polygon = GeoArcUtil.buildCombinedArcPolygon(airspace);
            return List.of(polygon);
        } else {
            Log.w(tag, "Unsupported arc count: " + airspace.getArcs().size());
            return List.of();
        }
    }

}

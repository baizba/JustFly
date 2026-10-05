package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.util.GeoArcUtil;
import java.util.List;

/** Preserves the existing one-arc and two-arc boundary construction. */
public class ArcToPolygonMapper {
    public AirspaceGeometry toPolygon(Airspace airspace) {
        List<GeoCoordinate> points = List.of();
        if (airspace.getArcs().size() == 1) {
            points = GeoArcUtil.getSingleArcPoints(airspace.getArcs().get(0));
        } else if (airspace.getArcs().size() == 2) {
            points = GeoArcUtil.getCombinedArcPoints(airspace);
        } else {
            java.util.logging.Logger.getLogger(ArcToPolygonMapper.class.getName())
                    .warning("Unsupported arc count: " + airspace.getArcs().size());
        }
        return points.isEmpty() ? null : new AirspaceGeometry(airspace, points);
    }
}

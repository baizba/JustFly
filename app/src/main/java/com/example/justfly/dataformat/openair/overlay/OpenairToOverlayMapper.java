package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Openair;

import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenairToOverlayMapper {
    private final CircleToPolygonMapper circleToPolygonMapper;
    private final PointsToPolygonMapper pointsToPolygonMapper;
    private final ArcToPolygonMapper arcToPolygonMapper;

    public OpenairToOverlayMapper() {
        circleToPolygonMapper = new CircleToPolygonMapper();
        pointsToPolygonMapper = new PointsToPolygonMapper();
        arcToPolygonMapper = new ArcToPolygonMapper();
    }

    public List<Polygon> getPolygonAirspaces(Openair openair) {
        List<Polygon> airspaceOverlays = new ArrayList<>();

        openair.getAirspaces()
                .forEach(airspace -> {
                    if (!airspace.getArcs().isEmpty()) {
                        Polygon arcPolygon = arcToPolygonMapper.toPolygon(airspace);
                        if (Objects.nonNull(arcPolygon)) {
                            airspaceOverlays.add(arcPolygon);
                        }
                        return;
                    }

                    if (!airspace.getCircles().isEmpty()) {
                        airspaceOverlays.addAll(circleToPolygonMapper.toPolygons(airspace));
                        return;
                    }

                    if (airspace.getPolygonPoints().size() > 1) {
                        airspaceOverlays.add(pointsToPolygonMapper.toPolygon(airspace));
                    }
                });

        return airspaceOverlays;
    }

}

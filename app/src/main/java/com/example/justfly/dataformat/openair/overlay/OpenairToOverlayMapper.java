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

    public List<Polygon> getOverlays(Openair openair) {
        List<Polygon> airspaceOverlays = new ArrayList<>();

        openair.getAirspaces()
                .stream()
                .filter(airspace -> airspace.getPolygonPoints().size() > 1)
                .map(pointsToPolygonMapper::toPolygon)
                .forEach(airspaceOverlays::add);

        openair.getAirspaces()
                .stream()
                .filter(airspace -> !airspace.getCircles().isEmpty())
                .map(circleToPolygonMapper::toPolygons)
                .forEach(airspaceOverlays::addAll);

        openair.getAirspaces()
                .stream()
                .filter(airspace -> !airspace.getArcs().isEmpty())
                .map(arcToPolygonMapper::toPolygon)
                .filter(Objects::nonNull)
                .forEach(airspaceOverlays::add);

        return airspaceOverlays;
    }

}

package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Airspace;
import java.util.List;
import java.util.stream.Collectors;

public class PointsToPolygonMapper {
    public AirspaceGeometry toPolygon(Airspace airspace) {
        List<GeoCoordinate> points = airspace.getPolygonPoints().stream()
                .map(point -> new GeoCoordinate(point.getLatitude(), point.getLongitude()))
                .collect(Collectors.toList());
        return new AirspaceGeometry(airspace, points);
    }
}

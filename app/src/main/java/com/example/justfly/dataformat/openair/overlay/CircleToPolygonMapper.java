package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.util.GeoCircleUtil;
import java.util.List;
import java.util.stream.Collectors;

public class CircleToPolygonMapper {
    public List<AirspaceGeometry> toPolygons(Airspace airspace) {
        return airspace.getCircles().stream()
                .map(circle -> new AirspaceGeometry(airspace, GeoCircleUtil.getCirclePoints(circle)))
                .collect(Collectors.toList());
    }
}

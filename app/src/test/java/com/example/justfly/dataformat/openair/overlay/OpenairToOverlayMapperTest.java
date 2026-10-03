package com.example.justfly.dataformat.openair.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.Arc;
import com.example.justfly.dataformat.openair.model.DrawingDirection;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.model.PolygonPoint;

import org.junit.jupiter.api.Test;

import java.util.List;

class OpenairToOverlayMapperTest {

    @Test
    void getPolygonAirspaces_prioritizesArcGeometryOverPointGeometry() {
        Airspace airspace = new Airspace();
        airspace.setPolygonPoints(List.of(
                new PolygonPoint(47.0, 14.0),
                new PolygonPoint(47.1, 14.1),
                new PolygonPoint(47.2, 14.2)
        ));

        Arc arc = Arc.ArcBuilder.builder()
                .centerLatitude(47.0)
                .centerLongitude(14.0)
                .startLatitude(47.0)
                .startLongitude(14.1)
                .endLatitude(47.1)
                .endLongitude(14.0)
                .direction(DrawingDirection.CLOCKWISE)
                .build();
        airspace.setArcs(List.of(arc));

        Openair openair = new Openair();
        openair.addAirspace(airspace);

        OpenairToOverlayMapper mapper = new OpenairToOverlayMapper();
        int overlays = mapper.getPolygonAirspaces(openair).size();

        assertEquals(1, overlays);
    }
}

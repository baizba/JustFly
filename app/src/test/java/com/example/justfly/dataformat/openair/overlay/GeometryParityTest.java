package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.Arc;
import com.example.justfly.dataformat.openair.model.Circle;
import com.example.justfly.dataformat.openair.model.DrawingDirection;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.model.PolygonPoint;
import com.example.justfly.dataformat.openair.parser.OpenairParser;
import com.example.justfly.util.GeoArcUtil;
import com.example.justfly.util.GeoCircleUtil;
import com.example.justfly.util.ResourceFileUtil;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GeometryParityTest {
    @Test
    void circleRetains360SamplesLegacyRadiusConversionAndRingClosure() {
        List<GeoCoordinate> points = GeoCircleUtil.getCirclePoints(new Circle(1, 47, 14));
        assertEquals(361, points.size());
        assertEquals(points.get(0), points.get(360));
        // Original osmdroid-backed formula: radius rounded to 1609 metres.
        assertEquals(47.014470084639234, points.get(0).latitude(), 1e-10);
        assertEquals(14, points.get(0).longitude(), 1e-12);
        assertEquals(47, points.get(90).latitude(), 1e-12);
        assertTrue(points.get(90).longitude() > 14);
    }

    @Test
    void arcsRetainDirectionWraparoundAndOneDegreeSampling() {
        Arc clockwise = arc(DrawingDirection.CLOCKWISE);
        Arc counterClockwise = arc(DrawingDirection.COUNTER_CLOCKWISE);
        List<GeoCoordinate> longArc = GeoArcUtil.getSingleArcPoints(clockwise);
        List<GeoCoordinate> shortArc = GeoArcUtil.getSingleArcPoints(counterClockwise);
        assertEquals(271, longArc.size());
        assertEquals(91, shortArc.size());
        assertEquals(0, longArc.get(0).latitude(), 1e-12);
        assertEquals(1, longArc.get(0).longitude(), 1e-12);
        assertTrue(longArc.get(90).latitude() < -0.99);
        assertEquals(1, shortArc.get(90).latitude(), 1e-12);
        assertEquals(0, shortArc.get(90).longitude(), 1e-12);
    }

    @Test
    void combinedArcsKeepConnectingEdgesAndCloseWithoutAlteringSamples() {
        Airspace airspace = new Airspace();
        airspace.setArcs(List.of(arc(DrawingDirection.CLOCKWISE), arc(DrawingDirection.COUNTER_CLOCKWISE)));
        AirspaceGeometry geometry = new ArcToPolygonMapper().toPolygon(airspace);
        assertSame(airspace, geometry.airspace());
        assertEquals(364, geometry.points().size());
        assertEquals(geometry.points().get(0), geometry.points().get(363));
        assertEquals(geometry.points().get(0), geometry.points().get(271));
    }

    @Test
    void polygonsKeepCoordinateOrderAndOriginalModel() {
        Airspace airspace = new Airspace();
        airspace.setPolygonPoints(List.of(new PolygonPoint(47, 14),
                new PolygonPoint(47.1, 14.2), new PolygonPoint(47.2, 14.3)));
        AirspaceGeometry geometry = new PointsToPolygonMapper().toPolygon(airspace);
        assertSame(airspace, geometry.airspace());
        assertEquals(List.of(new GeoCoordinate(47, 14), new GeoCoordinate(47.1, 14.2),
                new GeoCoordinate(47.2, 14.3), new GeoCoordinate(47, 14)), geometry.points());
    }

    @Test
    void circlesStillTakePrecedenceOverPolygonPointsAndMultipleCirclesRemainSeparate() {
        Airspace airspace = new Airspace();
        airspace.setCircles(List.of(new Circle(1, 47, 14), new Circle(2, 48, 15)));
        airspace.setPolygonPoints(List.of(new PolygonPoint(47, 14), new PolygonPoint(48, 15)));
        Openair openair = new Openair();
        openair.addAirspace(airspace);
        List<AirspaceGeometry> geometries = new OpenairToOverlayMapper().getPolygonAirspaces(openair);
        assertEquals(2, geometries.size());
        assertEquals(361, geometries.get(0).points().size());
        assertEquals(361, geometries.get(1).points().size());
    }

    @Test
    void bundledAirspacesAllMapSuccessfullyWithFiniteClosedBoundaries() {
        Openair openair = new OpenairParser().parse(ResourceFileUtil.readResourceFile("openair/lo_airspaces.openair.txt"));
        assertEquals(162, openair.getAirspaces().size());
        List<AirspaceGeometry> geometries = assertDoesNotThrow(
                () -> new OpenairToOverlayMapper().getPolygonAirspaces(openair));
        assertFalse(geometries.isEmpty());
        for (AirspaceGeometry geometry : geometries) {
            assertTrue(geometry.points().size() >= 3);
            assertEquals(geometry.points().get(0), geometry.points().get(geometry.points().size() - 1));
            for (GeoCoordinate point : geometry.points()) {
                assertTrue(Double.isFinite(point.latitude()));
                assertTrue(Double.isFinite(point.longitude()));
            }
        }
    }

    private static Arc arc(DrawingDirection direction) {
        return Arc.ArcBuilder.builder().centerLatitude(0).centerLongitude(0)
                .startLatitude(0).startLongitude(1).endLatitude(1).endLongitude(0)
                .direction(direction).build();
    }
}

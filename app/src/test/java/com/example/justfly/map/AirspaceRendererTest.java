package com.example.justfly.map;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.overlay.AirspaceGeometry;
import com.example.justfly.dataformat.openair.overlay.GeoCoordinate;
import com.example.justfly.dataformat.openair.overlay.OpenairToOverlayMapper;
import com.example.justfly.dataformat.openair.parser.OpenairParser;
import com.example.justfly.util.ResourceFileUtil;
import org.junit.jupiter.api.Test;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.geojson.Polygon;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AirspaceRendererTest {
    @Test
    void bundledPolygonsCirclesAndArcsRetainEveryCoordinateAndRingClosure() {
        Openair openair = new OpenairParser().parse(ResourceFileUtil.readResourceFile("openair/lo_airspaces.openair.txt"));
        List<AirspaceGeometry> geometries = new OpenairToOverlayMapper().getPolygonAirspaces(openair);
        List<Feature> features = AirspaceRenderer.toFeatures(geometries).features();
        assertNotNull(features);
        assertEquals(geometries.size(), features.size());
        for (int i = 0; i < geometries.size(); i++) {
            Polygon polygon = assertInstanceOf(Polygon.class, features.get(i).geometry());
            assertEquals(1, polygon.coordinates().size());
            List<Point> ring = polygon.coordinates().get(0);
            List<GeoCoordinate> original = geometries.get(i).points();
            assertEquals(original.size(), ring.size());
            for (int j = 0; j < ring.size(); j++) {
                assertEquals(original.get(j).longitude(), ring.get(j).longitude());
                assertEquals(original.get(j).latitude(), ring.get(j).latitude());
            }
            assertEquals(ring.get(0), ring.get(ring.size() - 1));
        }
    }

    @Test
    void shortBoundariesRemainLinesAndEmptyOrSinglePointsAreSkipped() {
        Airspace airspace = new Airspace();
        List<AirspaceGeometry> geometries = List.of(
                new AirspaceGeometry(airspace, List.of()),
                new AirspaceGeometry(airspace, List.of(new GeoCoordinate(47, 14))),
                new AirspaceGeometry(airspace, List.of(new GeoCoordinate(47, 14), new GeoCoordinate(48, 15))));
        List<Feature> features = AirspaceRenderer.toFeatures(geometries).features();
        assertNotNull(features);
        assertEquals(1, features.size());
        LineString line = assertInstanceOf(LineString.class, features.get(0).geometry());
        assertEquals(List.of(Point.fromLngLat(14, 47), Point.fromLngLat(15, 48),
                Point.fromLngLat(14, 47)), line.coordinates());
    }
}

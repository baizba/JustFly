package com.example.justfly.map;

import android.graphics.Color;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.overlay.AirspaceGeometry;
import com.example.justfly.dataformat.openair.overlay.GeoCoordinate;
import com.example.justfly.dataformat.openair.overlay.OpenairToOverlayMapper;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.FillLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.geojson.Polygon;
import java.util.ArrayList;
import java.util.List;
import static org.maplibre.android.style.layers.Property.*;
import static org.maplibre.android.style.layers.PropertyFactory.*;

/** Static OpenAir geometry is converted once; source switching only changes visibility. */
final class AirspaceRenderer {
    private final FeatureCollection features;
    private final float density;
    private FillLayer fill;
    private LineLayer boundary;

    AirspaceRenderer(Openair openair, float density) {
        features = toFeatures(new OpenairToOverlayMapper().getPolygonAirspaces(openair));
        this.density = density;
    }

    void addTo(Style style) {
        style.addSource(new GeoJsonSource("airspaces", features));
        fill = new FillLayer("airspace-fill", "airspaces").withProperties(fillColor(Color.TRANSPARENT));
        boundary = new LineLayer("airspace-line", "airspaces").withProperties(
                lineColor(Color.BLACK), lineWidth(10f / density), lineJoin(LINE_JOIN_MITER));
        style.addLayer(fill);
        style.addLayer(boundary);
    }

    void setVisible(boolean visible) {
        String value = visible ? VISIBLE : NONE;
        fill.setProperties(visibility(value));
        boundary.setProperties(visibility(value));
    }

    boolean contains(MapLibreMap map, LatLng point) {
        return fill != null && !map.queryRenderedFeatures(
                map.getProjection().toScreenLocation(point), fill.getId()).isEmpty();
    }

    static FeatureCollection toFeatures(List<AirspaceGeometry> geometries) {
        List<Feature> features = new ArrayList<>();
        for (AirspaceGeometry geometry : geometries) {
            List<Point> points = new ArrayList<>();
            for (GeoCoordinate point : geometry.points()) {
                points.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }
            if (points.size() >= 2) {
                features.add(Feature.fromGeometry(points.size() >= 4
                        ? Polygon.fromLngLats(List.of(points)) : LineString.fromLngLats(points)));
            }
        }
        return FeatureCollection.fromFeatures(features);
    }
}

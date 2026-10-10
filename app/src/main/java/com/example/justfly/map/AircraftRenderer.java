package com.example.justfly.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import com.example.justfly.R;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import java.util.List;
import static org.maplibre.android.style.expressions.Expression.get;
import static org.maplibre.android.style.layers.Property.*;
import static org.maplibre.android.style.layers.PropertyFactory.*;

/** One aircraft bitmap, its GPS accuracy circle and its screen-length heading line. */
final class AircraftRenderer {
    private final MapLibreMap map;
    private final float density;
    private final GeoJsonSource aircraft = new GeoJsonSource("aircraft", emptyFeatures());
    private final GeoJsonSource heading = new GeoJsonSource("heading", emptyFeatures());
    private final CircleLayer accuracy;
    private Location location;
    private double zoom;

    AircraftRenderer(Style style, MapLibreMap map, Resources resources, float density) {
        this.map = map;
        this.density = density;
        zoom = MapBehavior.toLegacyZoom(map.getCameraPosition().zoom, density);
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.airplane_icon_black);
        if (bitmap == null) throw new IllegalStateException("Could not load aircraft icon");
        style.addImage("aircraft-icon", bitmap);
        style.addSource(aircraft);
        accuracy = new CircleLayer("accuracy", "aircraft").withProperties(
                circleColor(Color.rgb(100, 100, 255)), circleOpacity(50f / 255f),
                circleStrokeColor(Color.rgb(100, 100, 255)), circleStrokeOpacity(150f / 255f),
                circleStrokeWidth(1f / density), circleRadius(0f));
        style.addLayer(accuracy);
        style.addLayer(new SymbolLayer("aircraft-icon-layer", "aircraft").withProperties(
                iconImage("aircraft-icon"), iconAnchor(ICON_ANCHOR_CENTER),
                iconAllowOverlap(true), iconIgnorePlacement(true),
                iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP), iconRotate(get("bearing"))));
        style.addSource(heading);
        style.addLayer(new LineLayer("heading-line", "heading").withProperties(
                lineColor(Color.BLUE), lineWidth(MapBehavior.HEADING_WIDTH_PX / density),
                lineCap(LINE_CAP_BUTT)));
    }

    void updateLocation(Location location) {
        this.location = location;
        Feature point = Feature.fromGeometry(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
        point.addNumberProperty("bearing", location.hasBearing() ? location.getBearing() : 0);
        aircraft.setGeoJson(point);
        updateAccuracyAndHeading();
    }

    void updateZoom(double zoom) {
        this.zoom = zoom;
        updateAccuracyAndHeading();
    }

    private void updateAccuracyAndHeading() {
        if (location == null) return;
        accuracy.setProperties(circleRadius((float) (location.getAccuracy()
                / MapBehavior.metersPerPixel(location.getLatitude(), zoom, density) / density)));
        if (!location.hasBearing()) {
            heading.setGeoJson(emptyFeatures());
            return;
        }
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        PointF start = map.getProjection().toScreenLocation(position);
        PointF end = new PointF((float) MapBehavior.headingX(start.x, location.getBearing()),
                (float) MapBehavior.headingY(start.y, location.getBearing()));
        LatLng endpoint = map.getProjection().fromScreenLocation(end);
        heading.setGeoJson(LineString.fromLngLats(List.of(
                Point.fromLngLat(position.getLongitude(), position.getLatitude()),
                Point.fromLngLat(endpoint.getLongitude(), endpoint.getLatitude()))));
    }

    private static FeatureCollection emptyFeatures() {
        return FeatureCollection.fromFeatures(new Feature[0]);
    }
}

package com.example.justfly.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class DirectionLineOverlay extends Overlay {

    private static final float LINE_LENGTH = 600.0f;
    private static final float STROKE_WIDTH = 5.0f;

    private final MyLocationNewOverlay myLocationNewOverlay;
    private final Paint paint;

    public DirectionLineOverlay(MyLocationNewOverlay myLocationNewOverlay) {
        this.myLocationNewOverlay = myLocationNewOverlay;
        this.paint = createPaint();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        GeoPoint currentLocation = myLocationNewOverlay.getMyLocation();
        Location lastFix = myLocationNewOverlay.getLastFix();

        if (currentLocation == null || lastFix == null || !lastFix.hasBearing()) {
            return; // If there is no location or bearing, do not draw the line
        }

        Point userPointOnScreen = new Point();
        mapView.getProjection().toPixels(currentLocation, userPointOnScreen);

        drawDirectionLine(canvas, userPointOnScreen, lastFix.getBearing());
    }

    private void drawDirectionLine(Canvas canvas, Point userPointOnScreen, float bearing) {
        Path path = new Path();
        path.moveTo(userPointOnScreen.x, userPointOnScreen.y);
        path.lineTo(userPointOnScreen.x, userPointOnScreen.y - LINE_LENGTH);

        canvas.save();
        canvas.rotate(bearing, userPointOnScreen.x, userPointOnScreen.y);
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    private Paint createPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        return paint;
    }
}

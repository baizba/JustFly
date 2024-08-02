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

    private static final String TAG = "DirectionLineOverlay";
    private final MyLocationNewOverlay myLocationNewOverlay;

    public DirectionLineOverlay(MyLocationNewOverlay myLocationNewOverlay) {
        this.myLocationNewOverlay = myLocationNewOverlay;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        GeoPoint currentLocation = myLocationNewOverlay.getMyLocation();

        // The direction the user is facing
        Location lastFix = myLocationNewOverlay.getLastFix();

        if (currentLocation == null || !lastFix.hasBearing()) {
            return;
        }

        Point screenPoint = new Point();
        mapView.getProjection().toPixels(currentLocation, screenPoint);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5.0f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        Path path = new Path();
        path.moveTo(screenPoint.x, screenPoint.y);
        path.lineTo(screenPoint.x, screenPoint.y - 600);

        //save and restore to restore original canvas rotation (check javadoc of these 2 methods)
        canvas.save();
        canvas.rotate(lastFix.getBearing(), screenPoint.x, screenPoint.y);
        canvas.drawPath(path, paint);
        canvas.restore();
    }
}

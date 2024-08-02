package com.example.justfly.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

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
        float heading = myLocationNewOverlay.getLastFix().getBearing();
        if (currentLocation == null)
            return;

        Point screenPoint = new Point();
        mapView.getProjection().toPixels(currentLocation, screenPoint);
        Log.d(TAG, "screenPoint " + screenPoint);

        // Calculate the endpoint of the line based on the heading and map size
        int mapWidth = mapView.getWidth();
        int mapHeight = mapView.getHeight();
        float angle = (float) Math.toRadians(heading);
        Log.d(TAG, "angle " + angle);

        float lineLength = (float) Math.sqrt(mapWidth * mapWidth + mapHeight * mapHeight);
        Log.d(TAG, "lineLength " + lineLength);

        float endX = screenPoint.x + lineLength * (float) Math.cos(angle);
        float endY = screenPoint.y - lineLength * (float) Math.sin(angle);
        Log.d(TAG, "endX, endY: " + endX + ", " + endY);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5.0f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        Path path = new Path();
        path.moveTo(screenPoint.x, screenPoint.y);
        path.lineTo(endX, endY);

        canvas.drawPath(path, paint);
    }
}

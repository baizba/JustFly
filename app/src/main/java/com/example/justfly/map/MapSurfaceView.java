package com.example.justfly.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ZoomControls;
import androidx.annotation.Nullable;
import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapLibreMapOptions;
import org.maplibre.android.maps.MapView;

/** Application-owned map widget; external callers do not need MapLibre types. */
public final class MapSurfaceView extends FrameLayout {
    private final MapView nativeMapView;
    private final ZoomControls zoomControls;

    public MapSurfaceView(Context context, @Nullable AttributeSet attributes) {
        super(context, attributes);
        MapLibre.getInstance(context.getApplicationContext());
        nativeMapView = new MapView(context, MapLibreMapOptions.createFromAttributes(context, null).textureMode(true));
        addView(nativeMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        zoomControls = new ZoomControls(context);
        addView(zoomControls, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
    }

    MapView nativeMapView() { return nativeMapView; }
    ZoomControls zoomControls() { return zoomControls; }

    void releaseControls() {
        zoomControls.setOnZoomInClickListener(null);
        zoomControls.setOnZoomOutClickListener(null);
    }
}

package com.example.justfly.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapLibreMapOptions;
import org.maplibre.android.maps.MapView;

/** Application-owned map widget; external callers do not need MapLibre types. */
public final class MapSurfaceView extends FrameLayout {
    private final MapView nativeMapView;
    private final MapZoomControls zoomControls;
    private final MapArtwork artwork;

    public MapSurfaceView(Context context, @Nullable AttributeSet attributes) {
        super(context, attributes);
        MapLibre.getInstance(context.getApplicationContext());
        artwork = new MapArtwork(context);
        nativeMapView = new MapView(context, MapLibreMapOptions.createFromAttributes(context, null).textureMode(true));
        addView(nativeMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        zoomControls = new MapZoomControls(context, artwork);
        addView(zoomControls, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    MapView nativeMapView() { return nativeMapView; }
    MapZoomControls zoomControls() { return zoomControls; }

    public void configureButtons(ImageButton switchButton, ImageButton followButton) {
        switchButton.setImageBitmap(artwork.bitmap("ic_menu_mapmode"));
        followButton.setImageBitmap(artwork.bitmap("osm_ic_center_map"));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            zoomControls.activate();
        }
        return super.dispatchTouchEvent(event);
    }

    void releaseControls() { zoomControls.release(); }
}

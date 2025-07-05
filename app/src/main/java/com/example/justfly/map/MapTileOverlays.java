package com.example.justfly.map;

import android.content.Context;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapTileOverlays {

    private static final List<String> ACTIVE_REGIONS = List.of("openvfr_lo", "openvfr_lh", "openvfr_lj");

    private static final int MIN_ZOOM = 4;
    private static final int MAX_ZOOM = 11;
    private static final int TILE_SIZE = 512;
    private static final String FILENAME_ENDING = ".png";

    private final Map<String, Overlay> overlays = new HashMap<>();

    public MapTileOverlays(Context context) {
        ACTIVE_REGIONS.forEach(region -> createOverlay(region, context));

        //additionally add the openTopo tiles
        createOpenTopoOverlay(context);
    }

    //by default openTopo is disabled (not shown)
    private void createOpenTopoOverlay(Context context) {
        MapTileProviderBasic tileProvider = new MapTileProviderBasic(context, TileSourceFactory.OpenTopo);
        TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, context);
        tilesOverlay.setEnabled(false);
        overlays.put("openTopo", tilesOverlay);
    }

    //by default openVFR and openVFR_lh are shown
    private void createOverlay(String region, Context context) {
        ITileSource tileSource = new XYTileSource(region, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, FILENAME_ENDING, new String[]{""});
        MapTileProviderBasic tileProvider = new MapTileProviderBasic(context, tileSource);
        overlays.put(region, new TilesOverlay(tileProvider, context));
    }

    public Map<String, Overlay> getOverlays() {
        return overlays;
    }

}

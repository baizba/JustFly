package com.example.justfly.map;

import android.content.Context;
import android.graphics.Color;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapTileOverlays {

    //old zip file and subfolder format
    //private static final List<String> ACTIVE_REGIONS = List.of("openvfr_lo", "openvfr_lh", "openvfr_lj");
    private static final List<String> ACTIVE_REGIONS = List.of("lo", "lh", "lj");

    private static final int MIN_ZOOM = 4;
    private static final int MAX_ZOOM = 11;
    private static final int TILE_SIZE = 256;
    private static final String FILENAME_ENDING = ".png";
    public static final String MBTILES = ".mbtiles";

    private final Map<String, Overlay> overlays = new HashMap<>();

    public MapTileOverlays(Context context) {

        ACTIVE_REGIONS.forEach(region -> {
            File mapsDir = new File(Configuration.getInstance().getOsmdroidBasePath(), "maps");
            File mbArchive = new File(mapsDir, region + MBTILES);

            if (!mbArchive.isFile()) {
                throw new IllegalStateException("MBTiles file not found: " + mbArchive.getAbsolutePath());
            }

            createOverlay(region, mbArchive, context);
        });

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

    private void createOverlay(String region, File mbTilesFile, Context context) {
        ITileSource tileSource = new XYTileSource(region, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, FILENAME_ENDING, new String[]{""});
        MBTilesFileArchive mbArchive = MBTilesFileArchive.getDatabaseFileArchive(mbTilesFile);

        SimpleRegisterReceiver receiver = new SimpleRegisterReceiver(context);

        MapTileFileArchiveProvider archiveProvider =
                new MapTileFileArchiveProvider(receiver, tileSource, new IArchiveFile[]{mbArchive});

        MapTileProviderArray tileProvider =
                new MapTileProviderArray(tileSource, receiver, new MapTileModuleProviderBase[]{archiveProvider});

        //the line below was old system for one zip file with folders
        //MapTileProviderBasic tileProvider = new MapTileProviderBasic(context, tileSource);

        //important not to cover parts of other tiles with non transparent grey tiles
        TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, context);
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        tilesOverlay.setLoadingLineColor(Color.TRANSPARENT);

        overlays.put(region, tilesOverlay);
    }

    public Map<String, Overlay> getOverlays() {
        return overlays;
    }

}

package com.example.justfly.map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.RasterLayer;
import org.maplibre.android.style.sources.RasterSource;
import org.maplibre.android.style.sources.TileSet;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.maplibre.android.style.layers.Property.*;
import static org.maplibre.android.style.layers.PropertyFactory.*;

/** Raster rendering only: existing aviation databases are never converted or downloaded. */
final class MapTileOverlays {
    // The old HashMap insertion order determines region overlap drawing order.
    private static final List<String> REGIONS = legacyRegionOrder();
    private final Map<String, File> regions = new HashMap<>();
    private final List<String> vfrLayers = new ArrayList<>();
    private final List<String> topoLayers = new ArrayList<>();

    MapTileOverlays(File basePath) {
        for (String region : REGIONS) {
            File file = new File(new File(basePath, "maps"), region + ".mbtiles");
            if (!file.isFile() || !file.canRead()) {
                throw new IllegalStateException("MBTiles file not found or unreadable: " + file.getAbsolutePath());
            }
            // Validate the same tiles table osmdroid consumed, without requiring new metadata.
            try (SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null,
                    SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                 Cursor ignored = db.rawQuery("SELECT zoom_level,tile_column,tile_row,tile_data FROM tiles LIMIT 1", null)) {
                ignored.moveToFirst();
            } catch (RuntimeException exception) {
                throw new IllegalStateException("Invalid MBTiles database: " + file.getAbsolutePath(), exception);
            }
            regions.put(region, file);
        }
    }

    void addTo(Style style, boolean openTopo) {
        for (String region : REGIONS) {
            String id = "openvfr-" + region;
            addRaster(style, id, mbTiles(regions.get(region), 4, 11), vfrLayers);
        }
        // Optional online tiles use MapLibre's default HTTP handling and ambient cache.
        TileSet topo = new TileSet("2.0.0",
                "https://a.tile.opentopomap.org/{z}/{x}/{y}.png",
                "https://b.tile.opentopomap.org/{z}/{x}/{y}.png",
                "https://c.tile.opentopomap.org/{z}/{x}/{y}.png");
        topo.minZoom = 0f;
        topo.maxZoom = 17f;
        addRaster(style, "openTopo", topo, topoLayers);
        setOpenTopo(style, openTopo);
    }

    void setOpenTopo(Style style, boolean openTopo) {
        for (String id : vfrLayers) {
            style.getLayer(id).setProperties(visibility(openTopo ? NONE : VISIBLE));
        }
        for (String id : topoLayers) {
            style.getLayer(id).setProperties(visibility(openTopo ? VISIBLE : NONE));
        }
    }

    private void addRaster(Style style, String id, TileSet tiles, List<String> group) {
        style.addSource(new RasterSource(id, tiles, 256));
        style.addLayer(new RasterLayer(id, id).withProperties(
                rasterFadeDuration(0f), rasterResampling(RASTER_RESAMPLING_LINEAR)));
        if (group != null) {
            group.add(id);
        }
    }

    private static TileSet mbTiles(File file, int minZoom, int maxZoom) {
        String path = Uri.encode(file.getAbsolutePath(), "/");
        // Native MBTilesFileSource flips XYZ y to TMS and reads tile_data directly.
        TileSet tiles = new TileSet("2.0.0", "mbtiles://" + path + "?file={x}/{y}/{z}.png");
        tiles.minZoom = (float) minZoom;
        tiles.maxZoom = (float) maxZoom;
        return tiles;
    }

    private static List<String> legacyRegionOrder() {
        Map<String, Boolean> regions = new HashMap<>();
        for (String region : List.of("lo", "lh", "lj")) {
            regions.put(region, true);
        }
        return List.copyOf(regions.keySet());
    }
}

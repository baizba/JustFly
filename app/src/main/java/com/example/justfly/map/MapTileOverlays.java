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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.maplibre.android.style.layers.Property.*;
import static org.maplibre.android.style.layers.PropertyFactory.*;

/** Renders regional MBTiles directly without substituting missing tiles with parent tiles. */
final class MapTileOverlays {
    private final List<TileSet> regionalTiles = new ArrayList<>();
    private final List<String> vfrLayers = new ArrayList<>();
    private final List<String> topoLayers = new ArrayList<>();

    MapTileOverlays(File mapsDirectory) {
        for (File file : discoverMaps(mapsDirectory)) regionalTiles.add(regionalTiles(file));
    }

    void addTo(Style style, boolean openTopo) {
        for (int index = 0; index < regionalTiles.size(); index++) {
            String id = "openvfr-region-" + index;
            RasterSource source = new RasterSource(id, regionalTiles.get(index), 256);
            source.setMaxOverscaleFactorForParentTiles(0);
            source.setPrefetchZoomDelta(0);
            style.addSource(source);
            addRasterLayer(style, id, vfrLayers);
        }
        // Optional online tiles use MapLibre's default HTTP handling and ambient cache.
        TileSet topo = new TileSet("2.0.0",
                "https://a.tile.opentopomap.org/{z}/{x}/{y}.png",
                "https://b.tile.opentopomap.org/{z}/{x}/{y}.png",
                "https://c.tile.opentopomap.org/{z}/{x}/{y}.png");
        topo.minZoom = 0f;
        topo.maxZoom = 17f;
        style.addSource(new RasterSource("openTopo", topo, 256));
        addRasterLayer(style, "openTopo", topoLayers);
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

    private void addRasterLayer(Style style, String id, List<String> group) {
        style.addLayer(new RasterLayer(id, id).withProperties(
                rasterFadeDuration(0f), rasterResampling(RASTER_RESAMPLING_LINEAR)));
        group.add(id);
    }

    private static TileSet regionalTiles(File file) {
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null,
                SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
             Cursor zooms = database.rawQuery("SELECT MIN(zoom_level),MAX(zoom_level),COUNT(*) FROM tiles", null)) {
            if (!zooms.moveToFirst() || zooms.getLong(2) == 0
                    || zooms.getInt(0) < 0 || zooms.getInt(1) > 30) {
                throw new IllegalStateException("Empty or invalid MBTiles zoom coverage: " + file);
            }
            String path = Uri.encode(file.getAbsolutePath(), "/");
            // Native MBTilesFileSource flips XYZ y to TMS and reads tile_data directly.
            TileSet tiles = new TileSet("2.0.0", "mbtiles://" + path + "?file={x}/{y}/{z}.png");
            tiles.minZoom = (float) zooms.getInt(0);
            tiles.maxZoom = (float) zooms.getInt(1);
            try (Cursor metadata = database.rawQuery("SELECT value FROM metadata WHERE name='bounds'", null)) {
                if (metadata.moveToFirst()) {
                    String[] bounds = metadata.getString(0).split(",");
                    if (bounds.length != 4) throw new IllegalArgumentException("Invalid MBTiles bounds: " + file);
                    tiles.setBounds(Float.parseFloat(bounds[0]), Float.parseFloat(bounds[1]),
                            Float.parseFloat(bounds[2]), Float.parseFloat(bounds[3]));
                }
            }
            return tiles;
        }
    }

    static List<File> discoverMaps(File mapsDirectory) {
        File[] files = mapsDirectory.listFiles(file -> file.isFile()
                && file.getName().toLowerCase(Locale.ROOT).endsWith(".mbtiles"));
        if (files == null) {
            throw new IllegalStateException("Map directory not found or unreadable: " + mapsDirectory);
        }
        if (files.length == 0) {
            throw new IllegalStateException("No MBTiles files found in: " + mapsDirectory);
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        // Retain the basename-based HashMap ordering used for existing region overlaps.
        // Grouping also keeps files with identical basenames and different extension case.
        Map<String, List<File>> regions = new HashMap<>();
        for (File file : files) {
            String name = file.getName();
            String basename = name.substring(0, name.length() - ".mbtiles".length());
            regions.computeIfAbsent(basename, ignored -> new ArrayList<>()).add(file);
        }
        List<File> ordered = new ArrayList<>();
        for (List<File> region : regions.values()) {
            ordered.addAll(region);
        }
        return List.copyOf(ordered);
    }
}

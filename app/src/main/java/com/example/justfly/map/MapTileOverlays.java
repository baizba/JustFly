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

/** Raster rendering only: existing aviation databases are never converted or downloaded. */
final class MapTileOverlays {
    private final List<File> maps;
    private final List<String> vfrLayers = new ArrayList<>();
    private final List<String> topoLayers = new ArrayList<>();

    MapTileOverlays(File mapsDirectory) {
        maps = discoverMaps(mapsDirectory);
        for (File file : maps) {
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
        }
    }

    void addTo(Style style, boolean openTopo) {
        for (int index = 0; index < maps.size(); index++) {
            String id = "openvfr-" + index;
            addRaster(style, id, mbTiles(maps.get(index), 4, 11), vfrLayers);
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

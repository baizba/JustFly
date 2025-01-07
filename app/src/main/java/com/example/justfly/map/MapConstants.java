package com.example.justfly.map;

import org.osmdroid.tileprovider.tilesource.XYTileSource;

public interface MapConstants {
    int MIN_ZOOM = 4;
    int MAX_ZOOM = 11;
    int TILE_SIZE = 512;
    String FILENAME_ENDING = ".png";
    String OPEN_VFR_TILES_NAME = "openvfr";
    XYTileSource OPEN_VFR = new XYTileSource(OPEN_VFR_TILES_NAME, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, FILENAME_ENDING, new String[]{""});
}

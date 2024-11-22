package com.example.justfly.map;

import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;

import java.io.File;

/**
 * Just an example how to load files exploded from folder
 */
@Deprecated
public class LocalTileProvider extends MapTileProviderBase {

    public static final int ZOOM_MIN_LEVEL = 4;
    public static final int ZOOM_MAX_LEVEL = 11;
    public static final int TILE_SIZE = 512;
    public static final String FILENAME_TYPE = ".png";
    private final File tileDirectory;

    public LocalTileProvider(File tileDirectory) {
        super(new XYTileSource("Local", ZOOM_MIN_LEVEL, ZOOM_MAX_LEVEL, TILE_SIZE, FILENAME_TYPE, new String[0]));
        this.tileDirectory = tileDirectory;
    }


    @Override
    public Drawable getMapTile(long pMapTileIndex) {
        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        int x = MapTileIndex.getX(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);
        File tileFile = new File(tileDirectory, zoom + "/" + x + "/" + y + ".png");
        return Drawable.createFromPath(tileFile.getAbsolutePath());
    }

    @Override
    public int getMinimumZoomLevel() {
        return ZOOM_MIN_LEVEL;
    }

    @Override
    public int getMaximumZoomLevel() {
        return ZOOM_MAX_LEVEL;
    }

    @Override
    public IFilesystemCache getTileWriter() {
        return null;
    }

    @Override
    public long getQueueSize() {
        return 50;
    }
}

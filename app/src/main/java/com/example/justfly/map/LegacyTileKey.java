package com.example.justfly.map;

/** osmdroid cache keys are not MBTiles XYZ coordinates. */
final class LegacyTileKey {
    record Tile(int zoom, int x, int y) {
        int tmsRow() { return (1 << zoom) - 1 - y; }
    }

    static Tile decode(long key) {
        for (int zoom = 0; zoom <= 22; zoom++) {
            long side = 1L << zoom;
            long remainder = key - zoom * side * side;
            if (remainder >= 0 && remainder < side * side) {
                return new Tile(zoom, (int) (remainder / side), (int) (remainder % side));
            }
        }
        throw new IllegalArgumentException("Invalid legacy tile key: " + key);
    }
}

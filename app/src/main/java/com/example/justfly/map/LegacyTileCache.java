package com.example.justfly.map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Imports only cached Mapnik/OpenTopo images into read-only-renderable MBTiles.
 * The original cache is opened read-only. A complete staging file is atomically published,
 * so cancellation or an interrupted import cannot replace a previously usable cache.
 */
final class LegacyTileCache {
    private static final Map<String, String> PROVIDERS = new LinkedHashMap<>();
    static {
        PROVIDERS.put("Mapnik", "legacy-mapnik.mbtiles");
        PROVIDERS.put("OpenTopoMap", "legacy-opentopo.mbtiles");
    }

    static synchronized Map<String, File> prepare(Context context, File cacheDirectory) throws IOException {
        Map<String, File> result = new LinkedHashMap<>();
        File directory = new File(context.getFilesDir(), "maplibre/legacy-tiles");
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Could not create compatibility cache: " + directory);
        }
        File original = new File(cacheDirectory, "cache.db");
        String fingerprint = original.getAbsolutePath() + ":" + original.lastModified() + ":" + original.length();
        if (original.isFile()) {
            try (SQLiteDatabase source = SQLiteDatabase.openDatabase(original.getAbsolutePath(), null,
                    SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS)) {
                for (Map.Entry<String, String> entry : PROVIDERS.entrySet()) {
                    checkCancelled();
                    File target = new File(directory, entry.getValue());
                    if (!isCurrent(target, fingerprint)) {
                        importProvider(source, entry.getKey(), target, fingerprint);
                    }
                }
            }
        }
        for (Map.Entry<String, String> entry : PROVIDERS.entrySet()) {
            File file = new File(directory, entry.getValue());
            if (file.isFile()) {
                result.put(entry.getKey(), file);
            }
        }
        return result;
    }

    private static boolean isCurrent(File file, String fingerprint) {
        if (!file.isFile()) {
            return false;
        }
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null,
                SQLiteDatabase.OPEN_READONLY);
             Cursor cursor = database.rawQuery("SELECT value FROM metadata WHERE name='legacy_fingerprint'", null)) {
            return cursor.moveToFirst() && fingerprint.equals(cursor.getString(0));
        } catch (RuntimeException exception) {
            return false; // An incomplete old compatibility cache must be regenerated.
        }
    }

    private static void importProvider(SQLiteDatabase source, String provider, File target,
                                       String fingerprint) throws IOException {
        File staging = new File(target.getAbsolutePath() + ".pending");
        SQLiteDatabase.deleteDatabase(staging);
        try {
            try (SQLiteDatabase destination = SQLiteDatabase.openOrCreateDatabase(staging, null)) {
                destination.execSQL("CREATE TABLE metadata (name TEXT PRIMARY KEY, value TEXT)");
                destination.execSQL("CREATE TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER,"
                        + " tile_data BLOB, PRIMARY KEY (zoom_level, tile_column, tile_row))");
                destination.beginTransaction();
                try (Cursor cursor = source.rawQuery("SELECT [key], tile FROM tiles WHERE provider=?",
                        new String[]{provider})) {
                    while (cursor.moveToNext()) {
                        checkCancelled();
                        LegacyTileKey.Tile tile = LegacyTileKey.decode(cursor.getLong(0));
                        ContentValues values = new ContentValues();
                        values.put("zoom_level", tile.zoom());
                        values.put("tile_column", tile.x());
                        values.put("tile_row", tile.tmsRow());
                        values.put("tile_data", cursor.getBlob(1));
                        destination.insertOrThrow("tiles", null, values);
                    }
                    metadata(destination, "format", "png");
                    metadata(destination, "name", provider);
                    metadata(destination, "legacy_fingerprint", fingerprint);
                    destination.setTransactionSuccessful();
                } finally {
                    destination.endTransaction();
                }
            }
            checkCancelled();
            Files.move(staging.toPath(), target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            SQLiteDatabase.deleteDatabase(staging);
        }
    }

    private static void metadata(SQLiteDatabase database, String key, String value) {
        ContentValues values = new ContentValues();
        values.put("name", key);
        values.put("value", value);
        database.insertOrThrow("metadata", null, values);
    }

    private static void checkCancelled() throws IOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new IOException("Map cache import cancelled");
        }
    }
}

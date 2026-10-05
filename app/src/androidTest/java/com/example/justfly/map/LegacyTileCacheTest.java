package com.example.justfly.map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Map;
import static org.junit.Assert.*;

/** Device-side SQLite verification; this test never opens or modifies the installed map data. */
@RunWith(AndroidJUnit4.class)
public class LegacyTileCacheTest {
    private Context context;
    private File fixture;
    private File sourceFile;
    private byte[] sourceHash;
    private static final byte[] TILE = {1, 2, 3, 4};

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        fixture = new File(context.getCacheDir(), "legacy-cache-fixture");
        fixture.mkdirs();
        sourceFile = new File(fixture, "cache.db");
        SQLiteDatabase.deleteDatabase(sourceFile);
        try (SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(sourceFile, null)) {
            database.execSQL("CREATE TABLE tiles ([key] INTEGER, provider TEXT, tile BLOB, expires INTEGER)");
            insert(database, 46, "Mapnik");
            insert(database, 7, "OpenTopoMap");
            insert(database, 4, "UnrelatedProvider");
        }
        sourceHash = hash(sourceFile);
        // Isolate compatibility output from the application using a wrapped files directory.
        final Context base = context;
        context = new android.content.ContextWrapper(base) {
            @Override public File getFilesDir() {
                return new File(fixture, "output");
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        Thread.interrupted();
        if (fixture != null && fixture.exists()) {
            try (java.util.stream.Stream<java.nio.file.Path> paths = Files.walk(fixture.toPath())) {
                for (java.nio.file.Path path : paths.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList())) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    @Test
    public void importsProvidersSeparatelyAndConvertsTmsWithoutChangingOriginal() throws Exception {
        Map<String, File> caches = LegacyTileCache.prepare(context, fixture);
        assertEquals(2, caches.size());
        assertTile(caches.get("Mapnik"), 2, 3, 1);
        assertTile(caches.get("OpenTopoMap"), 1, 1, 0);
        assertArrayEquals(sourceHash, hash(sourceFile));
    }

    @Test
    public void repeatedPreparationDoesNotRewriteUsableCompatibilityFiles() throws Exception {
        Map<String, File> caches = LegacyTileCache.prepare(context, fixture);
        byte[] initial = hash(caches.get("Mapnik"));
        long modified = caches.get("Mapnik").lastModified();
        LegacyTileCache.prepare(context, fixture);
        assertArrayEquals(initial, hash(caches.get("Mapnik")));
        assertEquals(modified, caches.get("Mapnik").lastModified());
        assertArrayEquals(sourceHash, hash(sourceFile));
    }

    @Test
    public void incompleteStagingFileIsReplacedWithCompleteImport() throws Exception {
        File output = new File(context.getFilesDir(), "maplibre/legacy-tiles");
        output.mkdirs();
        File staging = new File(output, "legacy-mapnik.mbtiles.pending");
        try (SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(staging, null)) {
            database.execSQL("CREATE TABLE incomplete (x INTEGER)");
        }
        Map<String, File> caches = LegacyTileCache.prepare(context, fixture);
        assertFalse(staging.exists());
        assertTile(caches.get("Mapnik"), 2, 3, 1);
        assertArrayEquals(sourceHash, hash(sourceFile));
    }

    @Test
    public void cancelledPreparationLeavesPreviouslyImportedFilesUntouched() throws Exception {
        Map<String, File> caches = LegacyTileCache.prepare(context, fixture);
        byte[] initial = hash(caches.get("Mapnik"));
        Thread.currentThread().interrupt();
        try {
            LegacyTileCache.prepare(context, fixture);
            fail("Expected cancellation");
        } catch (IOException expected) {
            // A cancellation must not publish a partial replacement.
        } finally {
            Thread.interrupted();
        }
        assertArrayEquals(initial, hash(caches.get("Mapnik")));
        assertArrayEquals(sourceHash, hash(sourceFile));
    }

    private static void insert(SQLiteDatabase database, long key, String provider) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("provider", provider);
        values.put("tile", TILE);
        values.put("expires", 0);
        database.insertOrThrow("tiles", null, values);
    }

    private static void assertTile(File file, int zoom, int x, int row) {
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
             Cursor cursor = database.rawQuery("SELECT zoom_level,tile_column,tile_row,tile_data FROM tiles", null)) {
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(zoom, cursor.getInt(0));
            assertEquals(x, cursor.getInt(1));
            assertEquals(row, cursor.getInt(2));
            assertArrayEquals(TILE, cursor.getBlob(3));
        }
    }

    private static byte[] hash(File file) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file.toPath()));
    }
}

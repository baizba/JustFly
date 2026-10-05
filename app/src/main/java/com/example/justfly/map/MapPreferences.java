package com.example.justfly.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Retains the installed application's storage keys; never relocates existing map files. */
final class MapPreferences {
    final SharedPreferences preferences;
    final File basePath;
    final File cachePath;
    final int animationDuration;

    MapPreferences(Context context) {
        preferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String configured = preferences.getString("osmdroid.basePath", null);
        File resolved = configured == null ? discoverBasePath(context) : new File(configured);
        basePath = resolved;
        cachePath = new File(preferences.getString("osmdroid.cachePath", new File(basePath, "tiles").getAbsolutePath()));
        animationDuration = Math.max(1, preferences.getInt("osmdroid.ZoomSpeedDefault", 500));
        MapHttpConfiguration.install(context.getApplicationContext(), preferences);
        if (configured == null) {
            preferences.edit().putString("osmdroid.basePath", basePath.getAbsolutePath())
                    .putString("osmdroid.cachePath", cachePath.getAbsolutePath()).apply();
        }
    }

    private static File discoverBasePath(Context context) {
        List<File> candidates = new ArrayList<>();
        candidates.add(new File(Environment.getExternalStorageDirectory(), "osmdroid"));
        File external = context.getExternalFilesDir(null);
        if (external != null) {
            candidates.add(new File(external, "osmdroid"));
            candidates.add(external);
        }
        candidates.add(new File(context.getFilesDir(), "osmdroid"));
        candidates.add(context.getFilesDir());
        for (File candidate : candidates) {
            File maps = new File(candidate, "maps");
            if (new File(maps, "lo.mbtiles").canRead()
                    && new File(maps, "lh.mbtiles").canRead()
                    && new File(maps, "lj.mbtiles").canRead()) {
                return candidate;
            }
        }
        // Same internal fallback used by the old Configuration loader under scoped storage.
        return new File(context.getFilesDir(), "osmdroid");
    }
}

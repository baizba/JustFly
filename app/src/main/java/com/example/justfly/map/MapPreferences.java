package com.example.justfly.map;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;

/** Fixed offline map directory and the existing camera animation preference. */
final class MapPreferences {
    final File mapsDirectory;
    final int animationDuration;

    MapPreferences(Context context) {
        mapsDirectory = new File(context.getFilesDir(), "osmdroid/maps");
        SharedPreferences preferences = context.getSharedPreferences(
                context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        animationDuration = Math.max(1, preferences.getInt("osmdroid.ZoomSpeedDefault", 500));
    }
}

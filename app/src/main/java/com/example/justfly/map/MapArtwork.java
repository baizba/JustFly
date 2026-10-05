package com.example.justfly.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Original osmdroid PNGs, stored losslessly as text to preserve the map controls. */
final class MapArtwork {
    private final Context context;
    private final JSONObject images;

    MapArtwork(Context context) {
        this.context = context;
        try (InputStream input = context.getAssets().open("map_controls.json")) {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            images = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load map-control artwork", exception);
        }
    }

    Bitmap bitmap(String name) {
        int targetDensity = context.getResources().getDisplayMetrics().densityDpi;
        int sourceDensity = 160;
        String directory = "res/drawable/";
        if (!name.equals("ic_menu_mapmode")) {
            int[] densities = {160, 240, 320, 480, 640};
            String[] directories = {"mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};
            int selected = densities.length - 1;
            for (int index = 0; index < densities.length - 1; index++) {
                int low = densities[index];
                int high = densities[index + 1];
                // Android's density-resource selection prefers downscaling when similarly close.
                if (targetDensity <= low || ((2 * low - targetDensity) * high > targetDensity * targetDensity)) {
                    selected = index;
                    break;
                }
            }
            sourceDensity = densities[selected];
            directory = "res/drawable-" + directories[selected] + "-v4/";
        }
        try {
            byte[] data = Base64.decode(images.getString(directory + name + ".png"), Base64.DEFAULT);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDensity = sourceDensity;
            options.inTargetDensity = targetDensity;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (bitmap == null) {
                throw new IllegalStateException("Invalid map-control bitmap: " + name);
            }
            return bitmap;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load map-control bitmap: " + name, exception);
        }
    }
}

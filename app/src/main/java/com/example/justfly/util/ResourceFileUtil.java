package com.example.justfly.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceFileUtil {

    private static final String TAG = ResourceFileUtil.class.getSimpleName();

    public static List<String> readResourceFile(String filePath) {
        InputStream stream = ResourceFileUtil.class.getClassLoader().getResourceAsStream(filePath);
        if (stream == null) {
            throw new IllegalArgumentException("could not find resource file: " + filePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            String errorMessage = "could not read resource file " + filePath;
            Log.e(TAG, errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
}

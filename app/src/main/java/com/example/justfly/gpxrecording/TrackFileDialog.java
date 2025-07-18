package com.example.justfly.gpxrecording;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.Arrays;

public class TrackFileDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        File[] files = getGpxFiles();

        return new AlertDialog.Builder(requireContext())
                .setTitle("GPX Tracks")
                .setItems(getFileNames(files), (dialog, which) -> {
                    File selectedFile = files[which];
                    shareFile(selectedFile);
                })
                .setNegativeButton("Cancel", null)
                .create();
    }

    private File[] getGpxFiles() {
        Context context = requireContext();
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles((f, name) -> name.endsWith(".gpx"));
            return files != null ? files : new File[0];
        }
        return new File[0];
    }

    private String[] getFileNames(File[] files) {
        return Arrays.stream(files)
                .map(File::getName)
                .toArray(String[]::new);
    }

    private void shareFile(File file) {
        Context context = requireContext();
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/gpx+xml");  // or "*/*" for max compatibility
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share GPX file"));
    }
}

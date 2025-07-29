package com.example.justfly.gpxrecording;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.example.justfly.R;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GpxFileDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gpx_files, container, false);
        ListView fileListView = view.findViewById(R.id.fileListView);

        File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            showErrorDialog("GPX directory not found.");
            return view;
        }

        List<File> gpxFiles = getGpxFiles(dir);
        if (gpxFiles.isEmpty()) {
            showErrorDialog("GPX files not found.");
            return view;
        }


        ArrayAdapter<File> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, gpxFiles) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                TextView textView = row.findViewById(android.R.id.text1);
                textView.setText(gpxFiles.get(position).getName());
                return row;
            }
        };

        fileListView.setAdapter(adapter);

        fileListView.setOnItemClickListener((parent, view1, position, id) -> {
            File selectedFile = gpxFiles.get(position);
            shareFile(selectedFile);
        });

        fileListView.setOnItemLongClickListener((parent, view12, position, id) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete file?")
                    .setMessage("Do you want to delete this GPX file?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        gpxFiles.get(position).delete();
                        dismiss(); // Close dialog and reopen to refresh
                        new GpxFileDialogFragment().show(getParentFragmentManager(), "gpx_files_dialog");
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        return view;
    }

    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK", (dialog, which) -> dismiss())
                .show();
    }

    private List<File> getGpxFiles(File directory) {
        File[] files = directory.listFiles((f, name) -> name.endsWith(".gpx"));
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    private void shareFile(File file) {
        Context context = requireContext();
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/gpx+xml");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share GPX file"));
    }
}

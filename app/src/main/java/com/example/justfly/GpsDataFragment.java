package com.example.justfly;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.justfly.gps.GpsController;
import com.example.justfly.gpxrecording.GpxRecordingController;
import com.example.justfly.gpxrecording.GpxRecordingService;
import com.example.justfly.gpxrecording.TrackFileDialog;

public class GpsDataFragment extends Fragment {

    private GpxRecordingController gpxRecordingController;

    @Override
    public void onStart() {
        super.onStart();
        gpxRecordingController.bindService(requireContext(), getGpxRecordingServiceIntent());
    }

    @Override
    public void onStop() {
        super.onStop();
        gpxRecordingController.unbindService(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gps_data, container, false);
        view.findViewById(R.id.infoButton).setOnClickListener(v -> showInfoDialog());
        view.findViewById(R.id.btnViewGpx).setOnClickListener(v -> showGpxDialog());
        TextView speedTextView = view.findViewById(R.id.textSpeed);
        TextView altitudeTextView = view.findViewById(R.id.textAltitude);
        ImageButton recordButton = view.findViewById(R.id.btnRecord);

        GpsController gpsController = new GpsController();
        gpsController.subscribeToGpsUpdates(speedTextView, altitudeTextView, requireContext());
        recordButton.setColorFilter(android.graphics.Color.GRAY);
        gpxRecordingController = new GpxRecordingController(recordButton);
        gpxRecordingController.addToggleRecordingFunctionality(requireContext(), getGpxRecordingServiceIntent());
        return view;
    }

    private void showInfoDialog() {
        String infoMessage = getString(R.string.infoDialogMessage, BuildConfig.VERSION_NAME);
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.infoDialogTitle))
                .setMessage(infoMessage)
                .setPositiveButton(getString(R.string.closeButtonText), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showGpxDialog() {
        new TrackFileDialog().show(getChildFragmentManager(), "gpx_files_dialog");
    }

    private Intent getGpxRecordingServiceIntent() {
        return new Intent(requireContext(), GpxRecordingService.class);
    }

}
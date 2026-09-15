package com.example.justfly;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.justfly.gps.GpsController;
import com.example.justfly.gpxrecording.GpxFileDialogFragment;
import com.example.justfly.gpxrecording.GpxRecordingController;
import com.example.justfly.gpxrecording.GpxRecordingService;

public class GpsDataFragment extends Fragment {

    private GpxRecordingController gpxRecordingController;
    private GpsController gpsController;
    private TextView speedTextView;
    private TextView altitudeTextView;

    @Override
    public void onStart() {
        super.onStart();
        gpsController.subscribeToGpsUpdates(speedTextView, altitudeTextView);
        gpxRecordingController.bindService(requireContext(), getGpxRecordingServiceIntent());
    }

    @Override
    public void onStop() {
        super.onStop();
        gpsController.unsubscribeFromGpsUpdates();
        gpxRecordingController.unbindService(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requestNotificationPermission();
        View view = inflater.inflate(R.layout.fragment_gps_data, container, false);
        view.findViewById(R.id.infoButton).setOnClickListener(v -> showInfoDialog());
        view.findViewById(R.id.btnViewGpx).setOnClickListener(v -> showGpxDialog());
        speedTextView = view.findViewById(R.id.textSpeed);
        altitudeTextView = view.findViewById(R.id.textAltitude);
        ImageButton recordButton = view.findViewById(R.id.btnRecord);

        gpsController = new GpsController(JustFlyApp.getLocationRepository(requireContext()));
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
        //new TrackFileDialog().show(getChildFragmentManager(), "gpx_files_dialog");
        new GpxFileDialogFragment().show(getParentFragmentManager(), "gpx_files_dialog");
    }

    private Intent getGpxRecordingServiceIntent() {
        return new Intent(requireContext(), GpxRecordingService.class);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    100
            );
        }
    }

}

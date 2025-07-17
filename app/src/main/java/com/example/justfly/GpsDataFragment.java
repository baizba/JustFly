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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GpsDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GpsDataFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageButton recordButton;

    private GpsController gpsController;
    private GpxRecordingController gpxRecordingController;

    public GpsDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GpsData.
     */
    public static GpsDataFragment newInstance(String param1, String param2) {
        GpsDataFragment fragment = new GpsDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

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
        gpsController = new GpsController();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gps_data, container, false);
        view.findViewById(R.id.infoButton).setOnClickListener(v -> this.showInfoDialog());
        TextView speedTextView = view.findViewById(R.id.textSpeed);
        TextView altitudeTextView = view.findViewById(R.id.textAltitude);
        gpsController.subscribeToGpsUpdates(speedTextView, altitudeTextView, requireContext());
        recordButton = view.findViewById(R.id.btnRecord);
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

    private Intent getGpxRecordingServiceIntent() {
        return new Intent(requireContext(), GpxRecordingService.class);
    }

}
package com.example.justfly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.parser.OpenairParser;
import com.example.justfly.map.MapController;
import com.example.justfly.map.MapSurfaceView;
import com.example.justfly.util.ResourceFileUtil;
import java.util.List;

public class MapFragment extends Fragment {
    private MapController mapController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapSurfaceView map = view.findViewById(R.id.map);
        map.configureButtons(view.findViewById(R.id.btnSwitchMap), view.findViewById(R.id.btnFollowMe));
        mapController = new MapController(map, this::showMapError);
        mapController.showMyLocation(getResources(), JustFlyApp.getLocationRepository(requireContext()));
        List<String> openairData = ResourceFileUtil.readResourceFile("openair/lo_airspaces.openair.txt");
        Openair openair = new OpenairParser().parse(openairData);
        mapController.addAirspaces(openair);
        mapController.initializeMap(savedInstanceState);
        view.findViewById(R.id.btnFollowMe).setOnClickListener(v -> mapController.enableFollowMyLocation());
        view.findViewById(R.id.btnSwitchMap).setOnClickListener(v -> mapController.switchMapSource());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapController != null) mapController.startMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapController != null) mapController.resumeMap(requireContext());
    }

    @Override
    public void onPause() {
        if (mapController != null) mapController.pauseMap(requireContext());
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mapController != null) mapController.stopMap();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        if (mapController != null) mapController.saveMapState(state);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapController != null) mapController.lowMemory();
    }

    @Override
    public void onDestroyView() {
        if (mapController != null) {
            mapController.destroyMap();
            mapController = null;
        }
        super.onDestroyView();
    }

    private void showMapError(String message) {
        if (isAdded() && getView() != null) {
            new AlertDialog.Builder(requireContext()).setTitle("Error").setMessage(message)
                    .setPositiveButton(android.R.string.ok, null).show();
        }
    }
}

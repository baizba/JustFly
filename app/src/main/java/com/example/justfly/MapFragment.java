package com.example.justfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.parser.OpenairParser;
import com.example.justfly.map.MapController;
import com.example.justfly.util.ResourceFileUtil;

import org.osmdroid.config.Configuration;

import java.util.List;
import java.util.function.BiConsumer;

public class MapFragment extends Fragment {

    private MapController mapController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        handlePreferences(Configuration.getInstance()::load, view.getContext());

        mapController = new MapController(view.findViewById(R.id.map));
        mapController.initializeMap();
        mapController.showMyLocation(getResources());
        List<String> openairData = ResourceFileUtil.readResourceFile("openair/lo_airspaces.openair.txt");
        OpenairParser parser = new OpenairParser();
        Openair openair = parser.parse(openairData);
        mapController.addAirspaces(openair);
        view.findViewById(R.id.btnFollowMe).setOnClickListener(v -> mapController.enableFollowMyLocation());
        view.findViewById(R.id.btnSwitchMap).setOnClickListener(v -> mapController.switchMapSource());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapController.resumeMap(requireContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        mapController.pauseMap(requireContext());
    }

    private void handlePreferences(BiConsumer<Context, SharedPreferences> operation, Context ctx) {
        String preferenceFileName = ctx.getPackageName() + "_preferences";
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        operation.accept(ctx, sharedPreferences);
    }

}
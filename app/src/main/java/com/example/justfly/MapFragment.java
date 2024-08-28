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

import com.example.justfly.map.MapFacade;

import org.osmdroid.config.Configuration;

import java.util.function.BiConsumer;

public class MapFragment extends Fragment {

    private MapFacade mapFacade;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        handlePreferences(Configuration.getInstance()::load, view.getContext());

        view.findViewById(R.id.btnFollowMe).setOnClickListener(v -> mapFacade.enableFollowMyLocation());

        mapFacade = new MapFacade(view.findViewById(R.id.map));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handlePreferences(Configuration.getInstance()::load, requireContext());
        mapFacade.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        handlePreferences(Configuration.getInstance()::save, requireContext());
        mapFacade.pause();
    }

    private void handlePreferences(BiConsumer<Context, SharedPreferences> operation, Context ctx) {
        String preferenceFileName = ctx.getPackageName() + "_preferences";
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        operation.accept(ctx, sharedPreferences);
    }
}
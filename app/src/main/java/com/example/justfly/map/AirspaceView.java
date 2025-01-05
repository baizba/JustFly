package com.example.justfly.map;

import com.example.justfly.dataformat.openair.model.Openair;

public interface AirspaceView {
    void showMyLocation();
    void showAirspaces(Openair openair);
}

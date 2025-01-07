package com.example.justfly.map;

import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.parser.OpenairParser;
import com.example.justfly.util.ResourceFileUtil;

import java.util.List;

public class MapPresenter {

    private final AirspaceView airspaceView;

    public MapPresenter(AirspaceView airspaceView) {
        this.airspaceView = airspaceView;
        airspaceView.showMyLocation();
    }

    public void showAirspaces() {
        List<String> openairData = ResourceFileUtil.readResourceFile("openair/lo_airspaces.openair.txt");
        OpenairParser parser = new OpenairParser();
        Openair openair = parser.parse(openairData);
        airspaceView.showAirspaces(openair);
    }

}

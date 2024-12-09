package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Openair {

    private List<Airspace> airspaces;

    public List<Airspace> getAirspaces() {
        return airspaces;
    }

    public void setAirspaces(List<Airspace> airspaces) {
        this.airspaces = airspaces;
    }

    public void addAirspace(Airspace airspace) {
        if (airspaces == null) {
            airspaces = new ArrayList<>();
        }
        airspaces.add(airspace);
    }

    @NonNull
    @Override
    public String toString() {
        return "Openair{" +
                "airspaces=" + airspaces +
                '}';
    }
}

package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Openair {

    private List<Airspace> airspaces;

    public List<Airspace> getAirspaces() {
        return airspaces;
    }

    public List<Airspace> getAirspacesByName(String airspaceName) {
        return airspaces.stream()
                .filter(airspace -> airspace.getAirspaceName().equalsIgnoreCase(airspaceName))
                .collect(Collectors.toList());
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

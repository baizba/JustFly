package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Airspace {

    private AirspaceClass airspaceClass;
    private String airspaceName;
    private String altitudeHigh;
    private String altitudeLow;
    private List<PolygonPoint> polygonPoints;
    private List<Circle> circles;
    private List<Arc> arcs;

    public AirspaceClass getAirspaceClass() {
        return airspaceClass;
    }

    public void setAirspaceClass(AirspaceClass airspaceClass) {
        this.airspaceClass = airspaceClass;
    }

    public String getAirspaceName() {
        return airspaceName;
    }

    public void setAirspaceName(String airspaceName) {
        this.airspaceName = airspaceName;
    }

    public String getAltitudeHigh() {
        return altitudeHigh;
    }

    public void setAltitudeHigh(String altitudeHigh) {
        this.altitudeHigh = altitudeHigh;
    }

    public String getAltitudeLow() {
        return altitudeLow;
    }

    public void setAltitudeLow(String altitudeLow) {
        this.altitudeLow = altitudeLow;
    }

    public List<PolygonPoint> getPolygonPoints() {
        return polygonPoints != null ? polygonPoints : new ArrayList<>();
    }

    public void setPolygonPoints(List<PolygonPoint> polygonPoints) {
        this.polygonPoints = polygonPoints;
    }

    public List<Circle> getCircles() {
        return circles != null ? circles : new ArrayList<>();
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
    }

    public List<Arc> getArcs() {
        return arcs != null ? arcs : new ArrayList<>();
    }

    public void setArcs(List<Arc> arcs) {
        this.arcs = arcs;
    }

    @NonNull
    @Override
    public String toString() {
        return String
                .format(
                        Locale.GERMANY,
                        "Airspace{airspaceClass=%s, airspaceName='%s', altitudeHigh='%s', altitudeLow='%s', circles=%s, arcs=%s, polygonPoints=%s}",
                        airspaceClass,
                        airspaceName,
                        altitudeHigh,
                        altitudeLow,
                        circles,
                        arcs,
                        polygonPoints
                );
    }
}

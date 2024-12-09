package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Airspace {

    private AirspaceClass airspaceClass;
    private String airspaceName;
    private String altitudeHigh;
    private String altitudeLow;
    private List<PolygonPoint> polygonPoints;

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
        return polygonPoints;
    }

    public void setPolygonPoints(List<PolygonPoint> polygonPoints) {
        this.polygonPoints = polygonPoints;
    }

    public void addPolygonPoint(PolygonPoint polygonPoint) {
        if (polygonPoints == null) {
            polygonPoints = new ArrayList<>();
        }
        polygonPoints.add(polygonPoint);
    }

    @NonNull
    @Override
    public String toString() {
        return "Airspace{" +
                "airspaceClass=" + airspaceClass +
                ", airspaceName='" + airspaceName + '\'' +
                ", altitudeHigh='" + altitudeHigh + '\'' +
                ", altitudeLow='" + altitudeLow + '\'' +
                ", polygonPoints=" + polygonPoints +
                '}';
    }
}

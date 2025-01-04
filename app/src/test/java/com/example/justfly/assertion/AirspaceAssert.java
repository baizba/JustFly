package com.example.justfly.assertion;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.AirspaceClass;

import java.util.Objects;

public class AirspaceAssert {

    private final Airspace airspace;

    private AirspaceAssert(Airspace actual) {
        this.airspace = actual;
    }

    public static AirspaceAssert assertThat(Airspace actual) {
        return new AirspaceAssert(actual);
    }

    public AirspaceAssert hasAirspaceName(String expected) {
        if (!expected.equals(airspace.getAirspaceName())) {
            throw new AssertionError("Expected name to be " + expected + " but was " + airspace.getAirspaceName());
        }
        return this;
    }

    public AirspaceAssert hasAirspaceClass(AirspaceClass expected) {
        if (!expected.equals(airspace.getAirspaceClass())) {
            throw new AssertionError("Expected type to be " + expected + " but was " + airspace.getAirspaceClass());
        }
        return this;
    }

    public AirspaceAssert hasAltitudeHigh(String expected) {
        if (!Objects.equals(airspace.getAltitudeHigh(), expected)) {
            throw new AssertionError("Expected alt high to be " + expected + " but was " + airspace.getAltitudeHigh());
        }
        return this;
    }

    public AirspaceAssert hasAltitudeLow(String expected) {
        if (!Objects.equals(airspace.getAltitudeLow(), expected)) {
            throw new AssertionError("Expected alt low to be " + expected + " but was " + airspace.getAltitudeLow());
        }
        return this;
    }
}

package com.example.justfly.assertion;

import com.example.justfly.dataformat.openair.model.Openair;

public class OpenairAssert {

    private final Openair openair;

    private OpenairAssert(Openair openair) {
        this.openair = openair;
    }

    public static OpenairAssert assertThat(Openair openair) {
        return new OpenairAssert(openair);

    }

    public OpenairAssert hasAirspaceCount(int expected) {
        if (openair.getAirspaces().size() != expected) {
            throw new AssertionError("Expected airspace count to be " + expected + " but was " + openair.getAirspaces().size());
        }
        return this;
    }

    public void hasAirspaceNamed(String airspaceName) {
        boolean airspaceFound = openair.getAirspaces()
                .stream()
                .anyMatch(airspace -> airspace.getAirspaceName().equalsIgnoreCase(airspaceName));
        if (!airspaceFound) {
            throw new AssertionError("Expected airspace with name " + airspaceName + " to be found");
        }
    }

}

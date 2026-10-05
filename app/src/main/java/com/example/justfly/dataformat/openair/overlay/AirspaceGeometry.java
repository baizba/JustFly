package com.example.justfly.dataformat.openair.overlay;

import com.example.justfly.dataformat.openair.model.Airspace;
import java.util.ArrayList;
import java.util.List;

/** The original airspace together with its closed rendering boundary. */
public record AirspaceGeometry(Airspace airspace, List<GeoCoordinate> points) {
    public AirspaceGeometry {
        List<GeoCoordinate> ring = new ArrayList<>(points);
        if (!ring.isEmpty() && !ring.get(0).equals(ring.get(ring.size() - 1))) {
            ring.add(ring.get(0));
        }
        points = List.copyOf(ring);
    }
}

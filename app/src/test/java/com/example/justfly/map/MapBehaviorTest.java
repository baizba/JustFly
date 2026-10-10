package com.example.justfly.map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MapBehaviorTest {
    @Test
    void touchStopsFollowingAndFollowButtonRestoresItWithoutChangingMode() {
        MapBehavior behavior = new MapBehavior();
        assertTrue(behavior.isFollowing());
        assertFalse(behavior.isOpenTopo());
        behavior.switchSource();
        behavior.onMapTouchDown();
        assertFalse(behavior.isFollowing());
        assertTrue(behavior.isOpenTopo());
        behavior.follow();
        assertTrue(behavior.isFollowing());
        assertTrue(behavior.isOpenTopo());
        behavior.switchSource();
        assertFalse(behavior.isOpenTopo());
    }

    @Test
    void headingLineHasSameLengthAtCardinalAndIntermediateBearings() {
        for (double bearing : new double[]{0, 45, 90, 180, 270, 359}) {
            double dx = MapBehavior.headingX(125, bearing) - 125;
            double dy = MapBehavior.headingY(300, bearing) - 300;
            assertEquals(600, Math.hypot(dx, dy), 1e-10);
        }
        assertEquals(-300, MapBehavior.headingY(300, 0), 1e-10);
        assertEquals(725, MapBehavior.headingX(125, 90), 1e-10);
    }

}

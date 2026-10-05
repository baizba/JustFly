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
    void cameraScaleMatchesLegacyMapAtDifferentDisplayDensities() {
        for (float density : new float[]{1, 1.5f, 2, 2.625f, 3, 4}) {
            for (double zoom : new double[]{4, 7.5, 11, 14}) {
                double nativeZoom = MapBehavior.toNativeZoom(zoom, density);
                double nativeWorldWidth = 512 * density * Math.pow(2, nativeZoom);
                double legacyWorldWidth = (int) (256 * density) * Math.pow(2, zoom);
                assertEquals(legacyWorldWidth, nativeWorldWidth, 0.00001);
                assertEquals(zoom, MapBehavior.toLegacyZoom(nativeZoom, density), 1e-12);
            }
        }
        assertEquals(10, MapBehavior.toNativeZoom(11, 2), 1e-12);
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

    @Test
    void accuracyRadiusUsesLegacyPhysicalPixelScale() {
        assertEquals(156543.03392804097, MapBehavior.metersPerPixel(0, 0, 1), 1e-8);
        assertEquals(156543.03392804097 / 4, MapBehavior.metersPerPixel(0, 1, 2), 1e-8);
    }
}

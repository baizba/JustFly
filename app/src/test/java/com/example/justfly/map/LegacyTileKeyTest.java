package com.example.justfly.map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LegacyTileKeyTest {
    @Test
    void decodesKnownOsmdroidCacheKeysAndFlipsTmsRow() {
        assertEquals(new LegacyTileKey.Tile(0, 0, 0), LegacyTileKey.decode(0));
        assertEquals(new LegacyTileKey.Tile(1, 0, 0), LegacyTileKey.decode(4));
        assertEquals(new LegacyTileKey.Tile(1, 1, 1), LegacyTileKey.decode(7));
        assertEquals(new LegacyTileKey.Tile(2, 3, 2), LegacyTileKey.decode(46));
        assertEquals(1, LegacyTileKey.decode(46).tmsRow());
        assertEquals(0, LegacyTileKey.decode(7).tmsRow());
        assertEquals(1, LegacyTileKey.decode(4).tmsRow());
    }

    @Test
    void handlesKeysBeyondSignedIntegerRange() {
        // z=17, x=70680, y=44915, captured format: z*4^z + x*2^z + y.
        assertEquals(new LegacyTileKey.Tile(17, 70680, 44915),
                LegacyTileKey.decode(301321990003L));
    }

    @Test
    void rejectsKeysInUnusedRangesInsteadOfImportingWrongCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> LegacyTileKey.decode(-1));
        assertThrows(IllegalArgumentException.class, () -> LegacyTileKey.decode(3));
        assertThrows(IllegalArgumentException.class, () -> LegacyTileKey.decode(8));
    }
}

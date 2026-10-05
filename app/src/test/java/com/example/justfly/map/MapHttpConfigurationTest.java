package com.example.justfly.map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapHttpConfigurationTest {
    @Test
    void overrideTakesPrecedenceAndDoesNotReceiveExtension() {
        assertEquals(5000, MapHttpConfiguration.expirationLifetime("max-age=10", 20000L, 1000, 5000, 300));
        assertEquals(-2, MapHttpConfiguration.expirationLifetime(null, null, 1000, -2, 300));
    }

    @Test
    void serverMaxAgeTakesPrecedenceOverExpires() {
        assertEquals(10300, MapHttpConfiguration.expirationLifetime("public, max-age=10", 20000L, 1000, -1, 300));
    }

    @Test
    void InvalidCacheControlFallsBackToExpiresOrSevenDays() {
        assertEquals(19300, MapHttpConfiguration.expirationLifetime("max-age=invalid", 20000L, 1000, -1, 300));
        assertEquals(604800300, MapHttpConfiguration.expirationLifetime(null, null, 1000, -1, 300));
        assertEquals(-700, MapHttpConfiguration.expirationLifetime(null, 0L, 1000, -1, 300));
    }
}

package com.example.justfly.map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.zip.CRC32;
import static org.junit.jupiter.api.Assertions.*;

class MapArtworkIntegrityTest {
    @Test
    void originalControlPngsRemainCompleteAtEveryDensity() throws Exception {
        JsonObject artwork = JsonParser.parseString(new String(Files.readAllBytes(
                Path.of("src/main/assets/map_controls.json")), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(16, artwork.size());
        for (Map.Entry<String, JsonElement> entry : artwork.entrySet()) {
            byte[] data = Base64.getDecoder().decode(entry.getValue().getAsString());
            ByteBuffer png = ByteBuffer.wrap(data);
            assertEquals(0x89504e470d0a1a0aL, png.getLong(), entry.getKey());
            boolean end = false;
            while (png.hasRemaining()) {
                int size = png.getInt();
                int type = png.getInt();
                assertTrue(size >= 0 && size <= png.remaining() - 4, entry.getKey());
                CRC32 crc = new CRC32();
                crc.update(data, png.position() - 4, size + 4);
                png.position(png.position() + size);
                assertEquals((int) crc.getValue(), png.getInt(), entry.getKey());
                if (type == 0x49454e44) end = true; // IEND
            }
            assertTrue(end, entry.getKey());
        }
    }
}

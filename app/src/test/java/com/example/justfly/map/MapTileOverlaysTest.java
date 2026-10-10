package com.example.justfly.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class MapTileOverlaysTest {
    @TempDir
    Path mapsDirectory;

    @Test
    void discoversArbitraryNamesAndIgnoresOtherFilesAndDirectories() throws Exception {
        Files.createFile(mapsDirectory.resolve("Austria 2026.mbtiles"));
        Files.createFile(mapsDirectory.resolve("north.MBTILES"));
        Files.createFile(mapsDirectory.resolve("notes.txt"));
        Files.createDirectory(mapsDirectory.resolve("folder.mbtiles"));

        Set<String> names = MapTileOverlays.discoverMaps(mapsDirectory.toFile()).stream()
                .map(File::getName).collect(Collectors.toSet());

        assertEquals(Set.of("Austria 2026.mbtiles", "north.MBTILES"), names);
    }

    @Test
    void sortsFilesByName() throws Exception {
        for (String region : List.of("lo", "lh", "lj")) {
            Files.createFile(mapsDirectory.resolve(region + ".mbtiles"));
        }
        List<String> actual = MapTileOverlays.discoverMaps(mapsDirectory.toFile()).stream()
                .map(File::getName).toList();

        assertEquals(List.of("lh.mbtiles", "lj.mbtiles", "lo.mbtiles"), actual);
    }

    @Test
    void reportsMissingDirectoryWithItsPath() {
        File missing = mapsDirectory.resolve("missing").toFile();
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> MapTileOverlays.discoverMaps(missing));
        assertTrue(exception.getMessage().contains(missing.getPath()));
    }

    @Test
    void reportsDirectoryWithoutMbTiles() throws Exception {
        Files.createFile(mapsDirectory.resolve("notes.txt"));
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> MapTileOverlays.discoverMaps(mapsDirectory.toFile()));
        assertTrue(exception.getMessage().contains("No MBTiles"));
    }
}

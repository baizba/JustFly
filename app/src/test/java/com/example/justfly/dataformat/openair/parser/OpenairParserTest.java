package com.example.justfly.dataformat.openair.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.AirspaceClass;
import com.example.justfly.dataformat.openair.model.Openair;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class OpenairParserTest {

    private static final List<String> OPENAIR_DATA = new ArrayList<>();

    private static final String MATZ_AIGEN = "MATZ AIGEN";
    private static final String HIGH_5000FT = "5000 ft msl";
    private static final String LOW_SFC = "SFC";

    private static final String TRA_WIENERWALD_WEST = "TRA WIENERWALD-WEST";
    private static final String HIGH_6500FT = "6500 ft msl";
    private static final String LOW_4500FT = "4500 ft msl";

    private final OpenairParser parser = new OpenairParser();

    @BeforeAll
    static void setup() {
        try (
                InputStream openairResource = OpenairParserTest.class.getClassLoader().getResourceAsStream("lo_airspaces.openair.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(openairResource))
        ) {
            OPENAIR_DATA.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                OPENAIR_DATA.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void test_parseAirspace() {
        Openair openair = parser.parse(OPENAIR_DATA);
        assertEquals(162, openair.getAirspaces().size());

        //1st airspace in lo_airspaces.openair.txt
        assertAirspace(
                openair.getAirspaces().get(0),
                AirspaceClass.D,
                MATZ_AIGEN,
                HIGH_5000FT,
                LOW_SFC
        );

        //last airspace in lo_airspaces.openair.txt
        assertAirspace(
                openair.getAirspaces().get(161),
                AirspaceClass.W,
                TRA_WIENERWALD_WEST,
                HIGH_6500FT,
                LOW_4500FT
        );

        System.out.println(openair.getAirspaces().get(0));
    }

    private void assertAirspace(
            Airspace airspace,
            AirspaceClass airspaceClass,
            String airspaceName,
            String altitudeHigh,
            String altitudeLow
    ) {
        assertEquals(airspaceClass, airspace.getAirspaceClass());
        assertEquals(airspaceName, airspace.getAirspaceName());
        assertEquals(altitudeHigh, airspace.getAltitudeHigh());
        assertEquals(altitudeLow, airspace.getAltitudeLow());
    }
}
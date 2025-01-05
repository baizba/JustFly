package com.example.justfly.dataformat.openair.parser;

import static com.example.justfly.assertion.AirspaceAssert.assertThat;

import com.example.justfly.assertion.OpenairAssert;
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
    private static final String MSL_5000FT = "5000 ft msl";
    private static final String SFC = "SFC";

    private static final String TRA_WIENERWALD_WEST = "TRA WIENERWALD-WEST";
    private static final String MSL_6500FT = "6500 ft msl";
    private static final String MSL_4500FT = "4500 ft msl";

    public static final String TRA_HOCHALM = "TRA HOCHALM";
    private static final String MSL_8000FT = "8000 ft msl";

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

        //there are 162 airspaces in lo_airspaces.openair.txt
        OpenairAssert.assertThat(openair)
                .hasAirspaceCount(162)
                .hasAirspaceNamed(TRA_HOCHALM);

        //1st airspace in lo_airspaces.openair.txt
        assertThat(openair.getAirspaces().get(0))
                .hasAirspaceClass(AirspaceClass.D)
                .hasAirspaceName(MATZ_AIGEN)
                .hasAltitudeHigh(MSL_5000FT)
                .hasAltitudeLow(SFC);

        //last airspace in lo_airspaces.openair.txt
        assertThat(openair.getAirspaces().get(161))
                .hasAirspaceClass(AirspaceClass.W)
                .hasAirspaceName(TRA_WIENERWALD_WEST)
                .hasAltitudeHigh(MSL_6500FT)
                .hasAltitudeLow(MSL_4500FT);

        //TRA HOCHALM
        List<Airspace> traHochalm = openair.getAirspacesByName(TRA_HOCHALM);

        assertThat(traHochalm.get(0))
                .hasAirspaceName(TRA_HOCHALM)
                .hasAirspaceClass(AirspaceClass.W)
                .hasAltitudeHigh(MSL_8000FT)
                .hasAltitudeLow(SFC);

        assertThat(traHochalm.get(1))
                .hasAirspaceName(TRA_HOCHALM)
                .hasAirspaceClass(AirspaceClass.W)
                .hasAltitudeHigh(MSL_8000FT)
                .hasAltitudeLow(MSL_6500FT);
    }

}

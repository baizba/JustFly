package com.example.justfly.dataformat.openair.parser;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.AirspaceClass;
import com.example.justfly.dataformat.openair.model.Openair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class OpenairParser {

    /*
     * Steps:
     * 1. remove empty lines and comments
     * 2. break into airspace blocks (from AC until another AC)
     * 3. process each block (airspace)
     */
    public Openair parse(List<String> openairData) {
        //clean
        List<String> cleanOpenair = openairData
                .stream()
                .filter(line -> line != null && line.trim().matches("^(?!\\*).*\\w.*"))
                .collect(Collectors.toList());

        //splint into airspace blocks
        List<List<String>> airspaceBlocks = new ArrayList<>();
        for (String line : cleanOpenair) {
            if (line.startsWith("AC")) {
                airspaceBlocks.add(new ArrayList<>());
            }
            if(airspaceBlocks.isEmpty()) {
                throw new IllegalStateException("airspace blocks empty, openair data did ont start with AC");
            }
            airspaceBlocks.get(airspaceBlocks.size() - 1).add(line);
        }

        //process each block (airspace)
        Openair openair = new Openair();
        for (List<String> airspaceBlock : airspaceBlocks) {
            Airspace airspace = getAirspace(airspaceBlock);
            openair.addAirspace(airspace);
        }

        return openair;
    }

    private Airspace getAirspace(List<String> airspaceBlock) {
        Airspace airspace = new Airspace();
        for (String line : airspaceBlock) {
            if (line.startsWith("AC")) {
                String airspaceClass = line.replaceAll("AC\\s*", "");
                airspace.setAirspaceClass(AirspaceClass.valueOf(airspaceClass));
            } else if (line.startsWith("AN")) {
                airspace.setAirspaceName(line.replaceAll("AN\\s*", ""));
            } else if (line.startsWith("AH")) {
                airspace.setAltitudeHigh(line.replaceAll("AH\\s*", ""));
            } else if (line.startsWith("AL")) {
                airspace.setAltitudeLow(line.replaceAll("AL\\s*", ""));
            }
        }
        return airspace;
    }
}

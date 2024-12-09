package com.example.justfly.dataformat.openair.parser;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.AirspaceClass;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.model.PolygonPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OpenairParser {

    public static final Pattern COORDINATES_PATTERN = Pattern.compile("(\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+(N|S)\\s+(\\d{1,3}:\\d{1,2}:\\d{1,2})\\s+(E|W)");
    public static final String DMS_SEPARATOR = ":";

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
            if (airspaceBlocks.isEmpty()) {
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
            } else if (line.startsWith("DP")) {
                airspace.addPolygonPoint(getPolygonPoint(line));
            }
        }
        return airspace;
    }

    private PolygonPoint getPolygonPoint(String line) {
        //example line: DP 47:32:19 N 014:04:58 E
        Matcher matcher = COORDINATES_PATTERN.matcher(line);
        if (matcher.find()) {
            String lat = matcher.group(1);
            String latDir = matcher.group(2);
            String lon = matcher.group(3);
            String lonDir = matcher.group(4);

            //example lat: 47:32:19 N
            String[] latDegMinSec = lat.split(DMS_SEPARATOR);
            int latDegrees = Integer.parseInt(latDegMinSec[0]);
            int latMinutes = Integer.parseInt(latDegMinSec[1]);
            int latSeconds = Integer.parseInt(latDegMinSec[2]);

            //example lon: 014:04:58 E
            String[] lonDegMinSec = lon.split(DMS_SEPARATOR);
            int lonDegrees = Integer.parseInt(lonDegMinSec[0]);
            int lonMinutes = Integer.parseInt(lonDegMinSec[1]);
            int lonSeconds = Integer.parseInt(lonDegMinSec[2]);

            double latitude = dmsToDecimal(latDegrees, latMinutes, latSeconds, latDir);
            double longitude = dmsToDecimal(lonDegrees, lonMinutes, lonSeconds, lonDir);

            return new PolygonPoint(latitude, longitude);
        }
        throw new IllegalArgumentException("Invalid line format: " + line);
    }

    private double dmsToDecimal(int degrees, int minutes, int seconds, String direction) {
        double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);
        // Adjust for direction
        if (direction.equalsIgnoreCase("S") || direction.equalsIgnoreCase("W")) {
            decimal = -decimal;
        }
        return decimal;
    }
}

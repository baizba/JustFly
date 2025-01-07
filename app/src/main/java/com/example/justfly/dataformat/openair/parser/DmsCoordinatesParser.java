package com.example.justfly.dataformat.openair.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DmsCoordinatesParser {

    private static final Pattern DMS_COORDINATES_PATTERN =
            Pattern.compile("(\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+(N|S)\\s+(\\d{1,3}:\\d{1,2}:\\d{1,2})\\s+(E|W)");
    private static final String DMS_SEPARATOR = ":";
    private static final String SOUTH = "S";
    private static final String WEST = "W";

    private final double latitude;
    private final double longitude;

    private DmsCoordinatesParser(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static DmsCoordinatesParser parse(String dmsCoordinates) {
        Matcher matcher = DMS_COORDINATES_PATTERN.matcher(dmsCoordinates);
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

            return new DmsCoordinatesParser(latitude, longitude);
        }
        throw new IllegalArgumentException("Invalid line format: " + dmsCoordinates);
    }

    private static double dmsToDecimal(int degrees, int minutes, int seconds, String direction) {
        double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);
        // Adjust for direction
        if (direction.equalsIgnoreCase(SOUTH) || direction.equalsIgnoreCase(WEST)) {
            decimal = -decimal;
        }
        return decimal;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

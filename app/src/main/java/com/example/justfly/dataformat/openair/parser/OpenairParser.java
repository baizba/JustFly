package com.example.justfly.dataformat.openair.parser;

import com.example.justfly.dataformat.openair.model.Airspace;
import com.example.justfly.dataformat.openair.model.AirspaceClass;
import com.example.justfly.dataformat.openair.model.Arc;
import com.example.justfly.dataformat.openair.model.Circle;
import com.example.justfly.dataformat.openair.model.DrawingDirection;
import com.example.justfly.dataformat.openair.model.Openair;
import com.example.justfly.dataformat.openair.model.PolygonPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OpenairParser {

    private static final String EMPTY = "";

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
                //if the line starts with AC that means we are reading a new Airspace block
                airspaceBlocks.add(new ArrayList<>());
            }
            if (airspaceBlocks.isEmpty()) {
                throw new IllegalStateException("airspace blocks empty, openair data did ont start with AC");
            }
            // the lines are always added to the last active airspace block
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
        //general airspace information
        Airspace airspace = new Airspace();
        airspace.setAirspaceClass(AirspaceClass.valueOf(getAirspaceElement(airspaceBlock, "AC")));
        airspace.setAirspaceName(getAirspaceElement(airspaceBlock, "AN"));
        airspace.setAltitudeHigh(getAirspaceElement(airspaceBlock, "AH"));
        airspace.setAltitudeLow(getAirspaceElement(airspaceBlock, "AL"));

        //if there are circle airspaces
        if (airspaceBlockContainsElement(airspaceBlock, "V X") && airspaceBlockContainsElement(airspaceBlock, "DC")) {
            airspace.setCircles(getCircles(airspaceBlock));
        }

        //if there are arcs in the block
        if (airspaceBlockContainsElement(airspaceBlock, "V X") && airspaceBlockContainsElement(airspaceBlock, "DB")) {
            airspace.setArcs(getArcs(airspaceBlock));
        }

        //if we have DB (points)
        if (airspaceBlockContainsElement(airspaceBlock, "DP")) {
            airspace.setPolygonPoints(getPolygonPoints(airspaceBlock));
        }

        return airspace;
    }

    private boolean airspaceBlockContainsElement(List<String> airspaceBlock, String elementPattern) {
        return airspaceBlock.stream().anyMatch(line -> line.startsWith(elementPattern));
    }

    private List<PolygonPoint> getPolygonPoints(List<String> airspaceBlock) {
        return airspaceBlock.stream().filter(line -> line.startsWith("DP")).map(this::getPolygonPoint).toList();
    }

    private List<Arc> getArcs(List<String> airspaceBlock) {
        double centerLat = 0;
        double centerLon = 0;
        double startLat = 0;
        double startLon = 0;
        double endLat = 0;
        double endLon = 0;
        DrawingDirection drawingDirection = null;
        List<Arc> arcs = new ArrayList<>();
        for (String line : airspaceBlock) {
            if (line.startsWith("V X")) {
                DmsCoordinatesParser dmsCoords = DmsCoordinatesParser.parse(line);
                centerLat = dmsCoords.getLatitude();
                centerLon = dmsCoords.getLongitude();
            } else if (line.startsWith("DB")) {
                //line example: DB 47:17:55 N 014:50:46 E,47:17:08 N 014:43:54 E
                String[] arcCoords = line.replaceAll("DB", EMPTY).trim().split(",");
                startLat = DmsCoordinatesParser.parse(arcCoords[0]).getLatitude();
                startLon = DmsCoordinatesParser.parse(arcCoords[0]).getLongitude();
                endLat = DmsCoordinatesParser.parse(arcCoords[1]).getLatitude();
                endLon = DmsCoordinatesParser.parse(arcCoords[1]).getLongitude();
            } else if (line.startsWith("V D")) {
                //there can be a direction: V D=+ or V D=-
                String directionClean = line.replaceAll("V\\s*D\\s*=", EMPTY).trim();
                drawingDirection = DrawingDirection.fromSymbol(directionClean);
            }

            //if we have all the elements for the arc then build arc and reset variables for next arcs (if we have them)
            if (centerLat != 0 && centerLon != 0 && startLat != 0 && startLon != 0 && endLat != 0 && endLon != 0) {
                Arc arc = Arc.ArcBuilder
                        .builder()
                        .centerLatitude(centerLat)
                        .centerLongitude(centerLon)
                        .startLatitude(startLat)
                        .startLongitude(startLon)
                        .endLatitude(endLat)
                        .endLongitude(endLon)
                        .direction(drawingDirection != null ? drawingDirection : DrawingDirection.CLOCKWISE)
                        .build();
                arcs.add(arc);

                //reset variables if we have multiple arcs
                centerLat = 0;
                centerLon = 0;
                startLat = 0;
                startLon = 0;
                endLat = 0;
                drawingDirection = null;
            }
        }
        return arcs;
    }

    private List<Circle> getCircles(List<String> airspaceBlock) {
        double lat = 0;
        double lon = 0;
        double radius = 0;
        List<Circle> circles = new ArrayList<>();
        for (String line : airspaceBlock) {
            if (line.startsWith("V X")) {
                DmsCoordinatesParser dmsCoords = DmsCoordinatesParser.parse(line);
                lat = dmsCoords.getLatitude();
                lon = dmsCoords.getLongitude();
            } else if (line.startsWith("DC")) {
                radius = Double.parseDouble(line.replaceAll("DC\\s*", EMPTY).trim());
            }

            /*
             if we have all the data for a circle then create a circle and add it to the list
             also reset the variable if maybe there are more circles defined in this block
             */
            if (lat != 0 && lon != 0 && radius != 0) {
                circles.add(new Circle(radius, lat, lon));
                lat = 0;
                lon = 0;
                radius = 0;
            }
        }
        return circles;
    }

    private String getAirspaceElement(List<String> airspaceBlock, String elementPattern) {
        return airspaceBlock
                .stream()
                .filter(line -> line.startsWith(elementPattern))
                .findFirst()//find the line starting with this pattern
                .orElse(EMPTY)//if nothing is found just return empty string
                .replaceAll(elementPattern, EMPTY)//remove the starting pattern
                .trim();//trim the string, we do not need blank spaces
    }

    private PolygonPoint getPolygonPoint(String line) {
        //example line: DP 47:32:19 N 014:04:58 E
        DmsCoordinatesParser dmsCoords = DmsCoordinatesParser.parse(line);
        return new PolygonPoint(dmsCoords.getLatitude(), dmsCoords.getLongitude());
    }

}

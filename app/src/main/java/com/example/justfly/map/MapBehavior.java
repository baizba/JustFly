package com.example.justfly.map;

/** Map interaction state, native zoom limits and heading geometry. */
final class MapBehavior {
    static final double MIN_ZOOM = 3;
    static final double MAX_ZOOM = 13;
    static final double INITIAL_ZOOM = 10;
    static final float HEADING_LENGTH_PX = 600;
    static final float HEADING_WIDTH_PX = 5;
    private boolean following = true;
    private boolean openTopo;

    boolean isFollowing() { return following; }
    boolean isOpenTopo() { return openTopo; }
    void follow() { following = true; }
    void onMapTouchDown() { following = false; }
    void switchSource() { openTopo = !openTopo; }

    static double headingX(double startX, double bearing) {
        return startX + HEADING_LENGTH_PX * Math.sin(Math.toRadians(bearing));
    }

    static double headingY(double startY, double bearing) {
        return startY - HEADING_LENGTH_PX * Math.cos(Math.toRadians(bearing));
    }
}

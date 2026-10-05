package com.example.justfly.map;

/** SDK-independent state and scale conversions for the existing map interactions. */
final class MapBehavior {
    static final double MIN_ZOOM = 4;
    static final double MAX_ZOOM = 14;
    static final double INITIAL_ZOOM = 11;
    static final float HEADING_LENGTH_PX = 600;
    static final float HEADING_WIDTH_PX = 5;
    private boolean following = true;
    private boolean openTopo;

    boolean isFollowing() { return following; }
    boolean isOpenTopo() { return openTopo; }
    void follow() { following = true; }
    void onMapTouchDown() { following = false; }
    void switchSource() { openTopo = !openTopo; }

    static double toNativeZoom(double legacyZoom, float density) {
        return legacyZoom + Math.log((int) (256 * density) / (512.0 * density)) / Math.log(2);
    }

    static double toLegacyZoom(double nativeZoom, float density) {
        return nativeZoom - toNativeZoom(0, density);
    }

    static double metersPerPixel(double latitude, double legacyZoom, float density) {
        return Math.cos(Math.toRadians(latitude)) * 2 * Math.PI * 6378137
                / ((int) (256 * density) * Math.pow(2, legacyZoom));
    }

    static double headingX(double startX, double bearing) {
        return startX + HEADING_LENGTH_PX * Math.sin(Math.toRadians(bearing));
    }

    static double headingY(double startY, double bearing) {
        return startY - HEADING_LENGTH_PX * Math.cos(Math.toRadians(bearing));
    }
}

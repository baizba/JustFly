package com.example.justfly.dataformat.openair.model;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Arc {
    private double centerLatitude;
    private double centerLongitude;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private DrawingDirection direction; // Clockwise or Counterclockwise

    public double getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }

    public double getCenterLongitude() {
        return centerLongitude;
    }

    public void setCenterLongitude(double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public DrawingDirection getDirection() {
        return direction;
    }

    public void setDirection(DrawingDirection direction) {
        this.direction = direction;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
                Locale.GERMANY,
                "Arc{centerLatitude=%.6f, centerLongitude=%.6f, startLatitude=%.6f, startLongitude=%.6f, endLatitude=%.6f, endLongitude=%.6f, direction=%s}",
                centerLatitude,
                centerLongitude,
                startLatitude,
                startLongitude,
                endLatitude,
                endLongitude,
                direction
        );
    }

    public static class ArcBuilder {
        private final  Arc arc;
        private ArcBuilder() {
            arc = new Arc();
        }
        public static ArcBuilder builder() {
            return new ArcBuilder();
        }
        public ArcBuilder centerLatitude(double centerLatitude) {
            arc.setCenterLatitude(centerLatitude);
            return this;
        }
        public ArcBuilder centerLongitude(double centerLongitude) {
            arc.setCenterLongitude(centerLongitude);
            return this;
        }
        public ArcBuilder startLatitude(double startLatitude) {
            arc.setStartLatitude(startLatitude);
            return this;
        }
        public ArcBuilder startLongitude(double startLongitude) {
            arc.setStartLongitude(startLongitude);
            return this;
        }
        public ArcBuilder endLatitude(double endLatitude) {
            arc.setEndLatitude(endLatitude);
            return this;
        }
        public ArcBuilder endLongitude(double endLongitude) {
            arc.setEndLongitude(endLongitude);
            return this;
        }
        public ArcBuilder direction(DrawingDirection direction) {
            arc.setDirection(direction);
            return this;
        }
        public Arc build() {
            return arc;
        }
    }
}

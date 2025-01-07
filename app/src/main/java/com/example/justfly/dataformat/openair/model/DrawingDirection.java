package com.example.justfly.dataformat.openair.model;

public enum DrawingDirection {
    CLOCKWISE("+"), COUNTER_CLOCKWISE("-");

    private final String symbol;

    DrawingDirection(String symbol) {
        this.symbol = symbol;
    }

    //symbol can be "+" or "-"
    public static DrawingDirection fromSymbol(String symbol) {
        for (DrawingDirection direction : values()) {
            if (direction.symbol.equals(symbol)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("symbol can be '+' or '-' You used: " + symbol);
    }
}
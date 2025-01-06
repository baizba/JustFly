package com.example.justfly.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UnitConversionUtil {

    private static final BigDecimal MS_TO_KNOTS_MULTIPLIER = BigDecimal.valueOf(1.944);
    private static final BigDecimal METER_TO_FEET_MULTIPLIER = BigDecimal.valueOf(3.281);

    public static long msToKnots(double metersPerSecond) {
        return BigDecimal.valueOf(metersPerSecond)
                .multiply(MS_TO_KNOTS_MULTIPLIER)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    public static long metersToFeet(double meters) {
        return BigDecimal.valueOf(meters)
                .multiply(METER_TO_FEET_MULTIPLIER)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

}

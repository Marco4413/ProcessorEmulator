package io.github.hds.pemu.utils;

public class MathUtils {

    public static int constrain(int val, int min, int max) {
        return Math.max(min, Math.min(val, max));
    }

    public static int makeMultipleOf(int divisor, int val) {
        return Math.round(val / (float) divisor) * divisor;
    }

}

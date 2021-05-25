package io.github.hds.pemu.utils;

public class MathUtils {

    /**
     * Constrains the specified value to the range [min; max]
     * @param val The value to constrain
     * @param min The minimum value of the range
     * @param max The maximum value of the range
     * @return The constrained value
     */
    public static int constrain(int val, int min, int max) {
        return Math.max(min, Math.min(val, max));
    }

    /**
     * Returns the closest multiple of divisor to value
     * @param divisor The divisor to constrain the value to
     * @param val The value from where to search the closest multiple to divisor
     * @return The closest multiple of divisor to value
     */
    public static int makeMultipleOf(int divisor, int val) {
        return Math.round(val / (float) divisor) * divisor;
    }

}

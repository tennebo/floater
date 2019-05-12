package com.tennebo;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IEEE 754 floating-point testing.
 */
class Main {

    private static final Logger logger = Logger.getGlobal();
    private static final String backspaces = "\b\b\b\b\b\b\b";

    // Counter for each number category
    private long zero = 0;
    private long infinite = 0;
    private long nan = 0;
    private long normal = 0;
    private long subnormal = 0;

    /**
     * True if the given number is a subnormal, a.k.a. denormal, IEEE 754 number.
     */
    private static boolean isSubnormal(float f) {
        return -Float.MIN_NORMAL < f && f < Float.MIN_NORMAL && 0f != f;
    }

    /**
     * Convert the given floating point number to string, then back to float.
     */
    private static void roundtrip(float f) {
        var strDecimal = Float.toString(f);
        var parsedFloat = Float.parseFloat(strDecimal);

        assert f == parsedFloat || (Float.isNaN(f) && Float.isNaN(parsedFloat));
    }

    /**
     * Categorize the given floating point number.
     */
    private void categorize(float f) {
        if (Float.isInfinite(f)) {
            infinite++;
        } else if (Float.isNaN(f)) {
            nan++;
        } else if (isSubnormal(f)) {
            subnormal++;
        } else if (0f == f) {
            zero++;
        } else {
            normal++;
        }
    }

    /**
     * Categorize and roundtrip the given floating point number.
     *
     * @param ieeeBits a floating point number expressed as IEEE bits
     */
    private void processNumber(int ieeeBits) {
        float f = Float.intBitsToFloat(ieeeBits);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format(
                    "%6d: Bits %6s: Float %6s\n",
                    ieeeBits, Integer.toBinaryString(ieeeBits), Float.toString(f)));
        }
        categorize(f);
        roundtrip(f);
    }

    /**
     * Process each number between start and end, inclusive.
     *
     * @param start first number to process
     * @param end   last number to process
     */
    private void loop(int start, int end) {
        assert start <= end;

        int nofProgress = 10000;
        int progress = 0;
        long total = (long)end - (long)start + 1;
        long reportAt = total / nofProgress;
        int k = start;
        do {
            processNumber(k);
            if (++progress == reportAt) {
                progress = 0;
                long counter = (long)k - start + 1;
                float percent = (100f * counter) / total;
                System.out.printf(backspaces + "%3.2f%%", percent);
                System.out.flush();
            }
        } while (k++ != end);

        printSummary(total);

        assert total == zero + infinite + nan + normal + subnormal;
        assert total < 0xFFFFFFFFL || 2 == zero;
        assert total < 0xFFFFFFFFL || 2 == infinite;
    }

    /**
     * Print a summary of the processed numbers to the console.
     */
    private void printSummary(long total) {
        System.out.println();
        System.out.println("Summary");
        System.out.println("=======");
        System.out.printf("Zero:      %,13d\n", zero);
        System.out.printf("Infinite:  %,13d\n", infinite);
        System.out.printf("NaN:       %,13d\n", nan);
        System.out.printf("Normal:    %,13d\n", normal);
        System.out.printf("Subnormal: %,13d\n", subnormal);
        System.out.printf("Total:     %,13d\n", total);
    }

    public static void main(String[] args) {
        int first = Integer.MIN_VALUE;
        int last = Integer.MAX_VALUE;

        new Main().loop(first, last);
    }
}

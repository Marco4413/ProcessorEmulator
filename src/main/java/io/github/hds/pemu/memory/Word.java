package io.github.hds.pemu.memory;

import org.jetbrains.annotations.NotNull;

public final class Word {

    public static final Word WordBit8  = new Word( 8, 0x000000ff);
    public static final Word WordBit16 = new Word(16, 0x0000ffff);
    public static final Word WordBit24 = new Word(24, 0x00ffffff);
    private static final Word[] ALL_WORDS = new Word[] { WordBit8, WordBit16, WordBit24 };

    public final int TOTAL_BITS;
    public final int TOTAL_BYTES;
    public final int BIT_MASK;

    private Word(int bits, int bitMask) {
        TOTAL_BITS  = bits;
        TOTAL_BYTES = bits / Byte.SIZE;
        BIT_MASK    = bitMask;
    }

    public static @NotNull Word getClosestWord(int totalBits) {
        int bestIndex = -1;
        int bestDifference = Integer.MAX_VALUE;
        for (int i = 0; i < ALL_WORDS.length; i++) {
            Word currentWord = ALL_WORDS[i];
            int currentDifference = Math.abs(currentWord.TOTAL_BITS - totalBits);
            if (currentDifference == 0) return ALL_WORDS[i];
            else if (currentDifference < bestDifference) {
                bestDifference = currentDifference;
                bestIndex = i;
            }
        }

        if (bestIndex < 0) throw new IllegalStateException("How did this happen? No word close to " + totalBits + " bits found!");
        return ALL_WORDS[bestIndex];
    }

    public static int getClosestSize(int totalBits) {
        return getClosestWord(totalBits).TOTAL_BITS;
    }

    public int combineBytes(int... bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= bytes[i] << (i * Byte.SIZE);
        }
        return result & BIT_MASK;
    }

    public int[] getBytes(int value) {
        int[] bytes = new int[TOTAL_BYTES];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (value >> (i * Byte.SIZE)) & WordBit8.BIT_MASK;
        }
        return bytes;
    }

}

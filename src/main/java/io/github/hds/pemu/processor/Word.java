package io.github.hds.pemu.processor;

public class Word {

    public static final int SizeBit8  =  8;
    public static final int SizeBit16 = 16;
    public static final int SizeBit24 = 24;

    public static final int MaskBit8  = 0x000000ff;
    public static final int MaskBit16 = 0x0000ffff;
    public static final int MaskBit24 = 0x00ffffff;

    public final int SIZE;
    public final int MASK;
    public final int BYTES;

    public Word(int size) {
        switch (size) {
            case 8:
                SIZE = SizeBit8;
                MASK = MaskBit8;
                break;
            case 16:
                SIZE = SizeBit16;
                MASK = MaskBit16;
                break;
            case 24:
                SIZE = SizeBit24;
                MASK = MaskBit24;
                break;
            default:
                throw new IllegalArgumentException("Invalid Word size: " + size);
        }

        BYTES = SIZE / Byte.SIZE;
    }

    public int combineBytes(int... bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= bytes[i] << (i * Byte.SIZE);
        }
        return result & MASK;
    }

    public int[] getBytes(int value) {
        int[] bytes = new int[BYTES];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (value >> (i * Byte.SIZE)) & MaskBit8;
        }
        return bytes;
    }

}

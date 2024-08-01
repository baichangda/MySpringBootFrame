package com.bcd.base.util;

import java.util.Arrays;

public class HexUtil {
    private static final char[] HEXDUMP_TABLE = new char[256 * 4];
    private static final byte[] HEX2B;

    static {
        final char[] DIGITS = "0123456789abcdef".toCharArray();
        for (int i = 0; i < 256; i++) {
            HEXDUMP_TABLE[i << 1] = DIGITS[i >>> 4 & 0x0F];
            HEXDUMP_TABLE[(i << 1) + 1] = DIGITS[i & 0x0F];
        }

        HEX2B = new byte[Character.MAX_VALUE + 1];
        Arrays.fill(HEX2B, (byte) -1);
        HEX2B['0'] = 0;
        HEX2B['1'] = 1;
        HEX2B['2'] = 2;
        HEX2B['3'] = 3;
        HEX2B['4'] = 4;
        HEX2B['5'] = 5;
        HEX2B['6'] = 6;
        HEX2B['7'] = 7;
        HEX2B['8'] = 8;
        HEX2B['9'] = 9;
        HEX2B['A'] = 10;
        HEX2B['B'] = 11;
        HEX2B['C'] = 12;
        HEX2B['D'] = 13;
        HEX2B['E'] = 14;
        HEX2B['F'] = 15;
        HEX2B['a'] = 10;
        HEX2B['b'] = 11;
        HEX2B['c'] = 12;
        HEX2B['d'] = 13;
        HEX2B['e'] = 14;
        HEX2B['f'] = 15;
    }

    public static String hexDump(byte[] array) {
        return hexDump(array, 0, array.length);
    }

    public static String hexDump(byte[] array, int fromIndex, int length) {
        if (length == 0) {
            return "";
        }
        int endIndex = fromIndex + length;
        char[] buf = new char[length << 1];
        int srcIdx = fromIndex;
        int dstIdx = 0;
        for (; srcIdx < endIndex; srcIdx++, dstIdx += 2) {
            System.arraycopy(
                    HEXDUMP_TABLE, (array[srcIdx] & 0xFF) << 1,
                    buf, dstIdx, 2);
        }
        return new String(buf);
    }

    public static byte[] decodeHexDump(CharSequence hexDump) {
        return decodeHexDump(hexDump, 0, hexDump.length());
    }

    public static byte[] decodeHexDump(CharSequence hexDump, int fromIndex, int length) {
        if (length < 0 || (length & 1) != 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        if (length == 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[length >>> 1];
        for (int i = 0; i < length; i += 2) {
            bytes[i >>> 1] = decodeHexByte(hexDump, fromIndex + i);
        }
        return bytes;
    }

    private static byte decodeHexByte(CharSequence s, int pos) {
        int hi = decodeHexNibble(s.charAt(pos));
        int lo = decodeHexNibble(s.charAt(pos + 1));
        if (hi == -1 || lo == -1) {
            throw new IllegalArgumentException(String.format(
                    "invalid hex byte '%s' at index %d of '%s'", s.subSequence(pos, pos + 2), pos, s));
        }
        return (byte) ((hi << 4) + lo);
    }

    private static int decodeHexNibble(final char c) {
        // Character.digit() is not used here, as it addresses a larger
        // set of characters (both ASCII and full-width latin letters).
        return HEX2B[c];
    }

    public static void main(String[] args) {
        String s = hexDump(new byte[]{1, 2, 3, 4, 0x12});
        System.out.println(s);
        byte[] bytes = decodeHexDump(s);
        System.out.println(Arrays.toString(bytes));
    }
}

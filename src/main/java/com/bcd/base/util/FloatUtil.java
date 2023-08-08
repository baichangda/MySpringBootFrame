package com.bcd.base.util;

public class FloatUtil {
    static final double[] pows_positive;
    static final double[] pows_negative;

    static {
        pows_positive = new double[11];
        for (int i = 0; i < pows_positive.length; i++) {
            pows_positive[i] = Math.pow(10, i);
        }

        pows_negative = new double[11];
        for (int i = 0; i < pows_negative.length; i++) {
            pows_negative[i] = Math.pow(10, -i);
        }
    }

    public static boolean eq(double d1, double d2, int num) {
        return Math.abs(d1 - d2) <= pows_negative[num];
    }

    public static boolean gt(double d1, double d2, int num) {
        return d1 - d2 > pows_negative[num];
    }

    public static boolean lt(double d1, double d2, int num) {
        return d1 - d2 < pows_negative[num];
    }

    public static long round(double d) {
        if (d > 0d) {
            return Math.round(d);
        } else if (d == 0d) {
            return 0;
        } else {
            return -Math.round(-d);
        }
    }

    public static int round(float f) {
        if (f > 0d) {
            return Math.round(f);
        } else if (f == 0d) {
            return 0;
        } else {
            return -Math.round(-f);
        }
    }


    public static double format(double d, int i) {
        if (d > 0) {
            if (i == 0) {
                return Math.floor(d);
            } else {
                return Math.floor(d * pows_positive[i]) / pows_positive[i];
            }

        } else if (d < 0) {
            if (i == 0) {
                return Math.ceil(d);
            } else {
                return Math.ceil(d * pows_positive[i]) / pows_positive[i];
            }
        } else {
            return 0;
        }
    }

    public static void main(String[] args) {
        System.out.println(eq(0.1,1-0.9,2));
    }
}

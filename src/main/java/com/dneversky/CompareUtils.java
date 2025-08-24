package com.dneversky;

public class CompareUtils {

    public static boolean isLessThan(Object v1, Object v2) {
        if (v1 == null || v2 == null) {
            throw new IllegalArgumentException("Cannot compare null values");
        }

        if (!v1.getClass().equals(v2.getClass())) {
            throw new IllegalArgumentException("Values must be of the same type");
        }

        if (v1 instanceof String) {
            return ((String) v1).compareTo((String) v2) < 0;
        } else if (v1 instanceof Integer) {
            return (Integer) v1 < (Integer) v2;
        } else if (v1 instanceof Double) {
            return (Double) v1 < (Double) v2;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + v1.getClass());
        }
    }
}

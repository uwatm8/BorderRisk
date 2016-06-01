package com.sonsofhesslow.games.risk.graphics.utils;

import java.lang.reflect.Array;

public class ArrayUtils {

    public static <T> T[] concat(T[]... arrays) {
        int lenAck = 0;
        for (T[] array : arrays) {
            lenAck += array.length;
        }

        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), lenAck);

        int elemenetAck = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, ret, elemenetAck, array.length);
            elemenetAck += array.length;
        }

        return ret;
    }

    public static <T> boolean contains(T[] a, T[] b) {
        for (T elemA : a) {
            for (T elemB : b) {
                if (elemA.equals(elemB)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> boolean contains(T[] a, T b) {
        for (T anA : a) {
            if (anA == b) {
                return true;
            }
        }
        return false;
    }


    public static <T> T[] reverse(T[] arr) {
        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(arr.getClass().getComponentType(), arr.length);
        for (int i = 0; i < arr.length; i++) {
            ret[i] = arr[arr.length - i - 1];
        }
        return ret;
    }
}
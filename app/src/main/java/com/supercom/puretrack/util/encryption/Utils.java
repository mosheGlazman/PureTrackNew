package com.supercom.puretrack.util.encryption;

import java.util.Date;

public class Utils {
    public static boolean equals(String str1, String str2) {
        String s1 = str1 == null ? "" : str1;
        String s2 = str2 == null ? "" : str2;
        return s2.equals(s1);
    }
    public static boolean isNullOrEmpty(Date str) {
        return (str == null || str.equals(""));
    }
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.length() == 0;
    }
    public static String removeFromRight(String text, String s) {
        String res = text;
        while (res.length() >= s.length() &&
                equals(res.substring(res.length() - s.length()), s)) {
            res = res.substring(0, res.length() - s.length());
        }

        return res;
    }

    public static String fromLeft(String text, int length) {
        if (isNullOrEmpty(text)) {
            return "";
        }

        if (text.length() < length) {
            return text;
        }

        return text.substring(0, length);
    }

    public static String fromRight(String text, int length) {
        if (isNullOrEmpty(text)) {
            return "";
        }

        if (text.length() < length) {
            return text;
        }

        return text.substring(text.length() - length);
    }

    public static String removeFromRight(String text, int length) {
        if (isNullOrEmpty(text)) {
            return "";
        }

        if (text.length() <= length) {
            return "";
        }

        return text.substring(0, text.length() - length);
    }

    public static String removeFromLeft(String text, String s) {
        String res = text;
        while (res.length() >= s.length() &&
                equals(res.substring(0, s.length()), s)) {
            res = res.substring(s.length());
        }

        return res;
    }

    public static String removeFromLeft(String text, int length) {
        if (isNullOrEmpty(text)) {
            return "";
        }

        if (text.length() <= length) {
            return "";
        }

        return text.substring(length, text.length());
    }

}

package com.supercom.puretrack.util.general;

public class StringsUtil {

    public static String removeLastCharacter(String str) {
        String strCopy = str;
        if (strCopy != null && strCopy.length() > 0) {
            strCopy = strCopy.substring(0, strCopy.length() - 1);
        }
        return strCopy;
    }
}

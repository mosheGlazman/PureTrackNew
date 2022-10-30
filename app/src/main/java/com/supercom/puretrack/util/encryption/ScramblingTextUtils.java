package com.supercom.puretrack.util.encryption;

public class ScramblingTextUtils {
    /*
    decrement each char in a parity index it.index+1 times
    increment each char in a odd index it.index+1 times
 */
    public static String scramble(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            int numberOfSteps = i % 2 == 0 ? (i + 1) : -(i + 1);
            result.append((char) (input.charAt(i) + numberOfSteps ));
        }
        return result.toString();
    }

    public static String unscramble(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            int numberOfSteps = i % 2 == 0 ? (i + 1) : -(i + 1);
            result.append((char) (input.charAt(i) - numberOfSteps) );
        }
        return result.toString();
    }
}

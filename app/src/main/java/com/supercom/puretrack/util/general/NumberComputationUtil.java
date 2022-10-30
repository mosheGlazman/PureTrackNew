package com.supercom.puretrack.util.general;

import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.date.TimeUtil;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class NumberComputationUtil {

    public static final String TAG = "NumberComputation";
    private final static String listOfLettersAndNumbers = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final static SecureRandom randomSecureNumber = new SecureRandom();
    private static final int NUMBER_OF_DIGITS = 8;
    public static StringBuilder randomPassword;


    public static int compareBetweenVersions(String currentVersion, String versionFromServer) {
        String s1 = normalisedVersion(currentVersion);
        String s2 = normalisedVersion(versionFromServer);
        return s1.compareTo(s2);
    }

    private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

    public static int createRandomInteger(int aStart, int aEnd, Random aRandom) {
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        // get the range, casting to long to avoid overflow problems
        long range = (long) aEnd - (long) aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long) (range * aRandom.nextDouble());
        return (int) (fraction + aStart);
    }

    public static StringBuilder createRandomPassword() {
        randomPassword = new StringBuilder(NUMBER_OF_DIGITS);
        for (int i = 0; i < NUMBER_OF_DIGITS; i++) {
            randomPassword.append(listOfLettersAndNumbers.charAt(randomSecureNumber.nextInt(listOfLettersAndNumbers.length())));
        }
        String createPassInfo = "\nCreate New SmsOp Pass: " + TimeUtil.getCurrentTimeStr() + " -> " + randomPassword;
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), createPassInfo,
                DebugInfoModuleId.Application_Info.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        return randomPassword;
    }

    public static StringBuilder getRandomPassword() {
        if (randomPassword == null) {
            createRandomPassword();
        }
        return randomPassword;
    }
}

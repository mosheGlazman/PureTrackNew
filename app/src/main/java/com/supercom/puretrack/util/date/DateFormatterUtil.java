package com.supercom.puretrack.util.date;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateFormatterUtil {
    public final static String DMY = "dd-MMM-yyyy";
    public final static String HM = "HH:mm";
    public final static String HMS = "HH:mm:ss:SSS";
    public final static String DM = "dd/MM";
    public final static String D = "dd";
    public final static String KM = "k:mm";
    public final static String MD = "MMM dd'th at'";

    public static Date parse(String outFormat, String inputStrDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(outFormat);
            return sdf.parse(inputStrDate);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCurDateStr(String outputFormat) {
        try {
            Date curDate = new Date();
            SimpleDateFormat dateFromatOutput = new SimpleDateFormat(outputFormat);
            return dateFromatOutput.format(curDate);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

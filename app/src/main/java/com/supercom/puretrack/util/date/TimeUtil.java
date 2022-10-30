package com.supercom.puretrack.util.date;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class TimeUtil {
    public final static String SIMPLE = "dd/MM/yyyy '@' HH:mm:ss";
    public final static String SIMPLE2 = "dd/MM/yyyy HH:mm:ss";

    /**
     * Get local time in milliseconds from 1/1/1970
     */
    public static long GetLocalTime() {
        Calendar c = Calendar.getInstance();
        TimeZone z = c.getTimeZone();
        int offset = z.getRawOffset();
        if (z.inDaylightTime(new Date(c.getTimeInMillis() + offset))) {
            offset = offset + z.getDSTSavings();
        }
        // Add TZ + DST
        c.add(Calendar.HOUR_OF_DAY, (offset / 1000 / 60 / 60));
        c.add(Calendar.MINUTE, (offset / 1000 / 60 % 60));
        return c.getTimeInMillis();
    }

    /**
     * Get any time in formatted time string
     */
    public static String GetTimeString(long Time, String TimeFormat) {
        SimpleDateFormat DateFormat = new SimpleDateFormat(TimeFormat,
                Locale.getDefault());

        return DateFormat.format(new Date(Time));
    }

    /**
     * Get UTC time in milliseconds from 1/1/1970
     */
    public static long GetUtcTime() {
        return System.currentTimeMillis();
    }


    /**
     * Get Last Midnight Calendar
     */
    private static Calendar GetLastMidnightCal() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        // Add TZ + DST
        //c.add(Calendar.HOUR_OF_DAY, (offset / 1000 / 60 / 60));
        //	c.add(Calendar.MINUTE, (offset / 1000 / 60 % 60));
        return c;
    }


    /**
     * Get previous midnight
     */
    public static long GetTodayEndTime() {
        Calendar c = GetLastMidnightCal();
        // Get next midnight time
        c.add(Calendar.HOUR_OF_DAY, 24);
        return c.getTimeInMillis();
    }

    public static long GetTimeWithOffset(int offset) {
        Calendar c = GetLastMidnightCal();
        // Get next midnight time
//		c.add(Calendar.HOUR_OF_DAY, 24 + offset);
        c.add(Calendar.DAY_OF_YEAR, 1);
        c.add(Calendar.HOUR_OF_DAY, offset);
        c.set(Calendar.HOUR_OF_DAY, 0);
        return c.getTimeInMillis();
    }

    public static long getCurrentTime() {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();
    }

    public static String formatFromDate(java.util.Date inputDate,
                                        String outputFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(outputFormat);
        return sdf.format(inputDate);
    }

    public static String getCurrentTimeStr() {
        return formatFromDate(new java.util.Date(), TimeUtil.SIMPLE);
    }

    public static String formatFromMiliSecondToString(long timeInMili, String dateFormatter) {
        Date date = new Date(timeInMili);
        // formattter
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormatter);
        // Pass date object
        String startTimeFormatted = formatter.format(date);

        return startTimeFormatted;
    }

    public static long convertDateToUTCTimeInSeconds(java.util.Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(SIMPLE);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateFormaterInUTC = formatter.format(date);

        java.util.Date newDateInUtc;
        try {
            newDateInUtc = formatter.parse(dateFormaterInUTC);
            return TimeUnit.MILLISECONDS.toSeconds(newDateInUtc.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return TimeUnit.MILLISECONDS.toSeconds(date.getTime());
    }

    // only convert and add local time zone
    public static String convertUTCToTimeInSecondsAddTZ(long UTC) {
        SimpleDateFormat formatter = new SimpleDateFormat(SIMPLE2);
        formatter.setTimeZone(TimeZone.getDefault());
        String dateFormaterInUTC = formatter.format(UTC);

        return dateFormaterInUTC;
    }

    public static StringBuilder getCurrentTimeInAmPm() {
        Calendar calendar = new GregorianCalendar();
        return getTimeInAmPm(calendar.getTimeInMillis());
    }

    public static StringBuilder getTimeInAmPm(long Time) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(Time);
        StringBuilder builder = new StringBuilder();
        if (calendar.get(Calendar.MINUTE) < 10) {
            if (calendar.get(Calendar.HOUR) == 0) {
                builder.append("12" + ":0").append(calendar.get(Calendar.MINUTE));
            } else {
                builder.append(calendar.get(Calendar.HOUR)).append(":0").append(calendar.get(Calendar.MINUTE));
            }
        } else {
            if (calendar.get(Calendar.HOUR) == 0) {
                builder.append("12" + ":").append(calendar.get(Calendar.MINUTE));
            } else {
                builder.append(calendar.get(Calendar.HOUR)).append(":").append(calendar.get(Calendar.MINUTE));
            }
        }
        return builder;
    }

    public static String getCurrentAmOrPmString() {
        Calendar calendar = new GregorianCalendar();
        return getAmOrPmString(calendar.getTimeInMillis());
    }

    public static String getAmOrPmString(long Time) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(Time);
        String type;
        if (calendar.get(Calendar.AM_PM) == 1) {
            type = "PM";
        } else {
            type = "AM";
        }
        return type;
    }

    public static String getAmOrPmLowerCaseString(long Time) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(Time);
        String type;
        if (calendar.get(Calendar.AM_PM) == 1) {
            type = "pm";
        } else {
            type = "am";
        }
        return type;
    }

    public static int getCurrentTimeFormatByDeviceSettings(Context context) {
        int time_format = 0;
        try {
            time_format = Settings.System.getInt(context.getContentResolver(), Settings.System.TIME_12_24);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return time_format;
    }

    public static String getDateFormatFromDevice(Context context) {
        Format dateFormat = android.text.format.DateFormat.getDateFormat(context);
        return ((SimpleDateFormat) dateFormat).toLocalizedPattern();
    }
    public static String convertDateToFormat(String date, String outputFormat) {
        Date parsed;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(DateFormatterUtil.DMY);
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat);

        try {
            parsed = df_input.parse(date);
            outputDate = df_output.format(parsed);
        } catch (Exception e) {
            Log.e("formattedDateFromString", "Exception in formateDateFromstring(): " + e.getMessage());
        }
        return outputDate;
    }

}

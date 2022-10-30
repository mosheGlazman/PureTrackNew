package com.supercom.puretrack.util.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TimeSpan {
    public float totalYears;
    public double totalDays;
    public double totalHours;
    public double totalMilliseconds;
    public double totalMinutes;
    public double totalSeconds;

    public TimeSpan(Date early, Date later) {
        if (early == null || later == null) {
            return;
        }
        totalMilliseconds = (later.getTime() - early.getTime());
        totalSeconds = ((double) later.getTime() - (double) early.getTime()) / 1000;
        totalMinutes = ((double) later.getTime() - (double) early.getTime()) / (60000);
        totalHours = ((double) later.getTime() - (double) early.getTime()) / (3600000);
        totalDays = ((double) later.getTime() - (double) early.getTime()) / (86400000);
        totalYears = (float) totalDays / 365;
    }

    public static TimeSpan fromNow(Date early) {
        return new TimeSpan(early, new Date());
    }
    public static TimeSpan toNow(Date later) {
        return new TimeSpan(new Date(), later);
    }

    public static TimeSpan getDiff(Date d) {
        TimeSpan res = new TimeSpan(d, new Date());

        if (res.totalMilliseconds < 0) {
            res.totalMilliseconds = Math.abs(res.totalMilliseconds);
            res.totalSeconds = Math.abs(res.totalSeconds);
            res.totalMinutes = Math.abs(res.totalMinutes);
            res.totalHours = Math.abs(res.totalHours);
            res.totalDays = Math.abs(res.totalDays);
            res.totalYears = Math.abs(res.totalYears);
        }

        return res;
    }

    public static boolean isFuture(Date date) {
        if (date == null) {
            return false;
        }

        return fromNow(date).totalSeconds < 0;
    }

    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }

        return fromNow(date).totalDays == 0;
    }

    public static int equals(Date o1, Date o2) {
        if (o2 == null && o1 == null) {
            return 0;
        }

        if (o1 == null) {
            return -1;
        }

        if (o2 == null) {
            return 1;
        }

        if (o1.getTime() == o2.getTime()) {
            return 0;
        }

        return (int) (o1.getTime() - o2.getTime());
    }


    public static Date getDate(int year, int monthOfYear, int dayOfMonth) {
        return getDate(String.format("%02d", dayOfMonth) + "/" + String.format("%02d", monthOfYear) + "/" + String.format("%04d", year), "dd/MM/yyyy");
    }

    public static Date getDate(String date, String format) {

        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (Exception ex) {

        }


        return null;
    }

    public static Date addSeconds(Date date, int seconds) {
        long t = date.getTime();
        return new Date(t + (seconds * 1000));
    }

    public static Date addMinutes(Date date, int minutes) {
        return addSeconds(date, minutes * 60);
    }

    public static Date addHour(Date date, int houres) {
        return addMinutes(date, houres * 60);
    }

    public static Date addDay(Date date, int days) {
        return addHour(date, days * 24);
    }

    public static String getString(Date date, String format) {
        if (date == null) {
            return "";
        }

        SimpleDateFormat format_Date = new SimpleDateFormat(format, Locale.ENGLISH);

        return format_Date.format(date);
    }
}

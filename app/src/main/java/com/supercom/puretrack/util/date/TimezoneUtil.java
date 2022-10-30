package com.supercom.puretrack.util.date;

import java.util.Hashtable;

public class TimezoneUtil {

    private final Hashtable<Integer, String> timeZoneTable = new Hashtable<Integer, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(4, "US/Hawaii");
            put(5, "US/Alaska");
            put(7, "US/Pacific");
            put(8, "US/Mountain");
            put(10, "US/Mountain");
            put(14, "Canada/Central");
            put(16, "Canada/Eastern");
            put(17, "US/Eastern");
            put(39, "Europe/Prague");
            put(41, "Europe/Prague");
            put(51, "Asia/Jerusalem");
            put(52, "Europe/Bucharest");
        }
    };

    public String getTimeZone(int id) {
        return timeZoneTable.get(id);
    }

}

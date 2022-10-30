/**
 *
 */
package com.supercom.puretrack.model.database.objects;


public class BatteryThreshold {
    public interface BATTERY_CONST {
        int BATTERY_HIGH_CHARGE = 80;
        int BATTERY_MEDIUM_CONSUMPTION = 70;
        int BATTERY_MEDIUM_CHARGE = 40;
        int BATTERY_LOW_CONSUMPTION = 30;
        int BATTERY_LOW_CHARGE = 20;
        int BATTERY_CRITICAL_CONSUMPTION = 10;
    }


    public int Charger_Low;
    public int Charger_Medium;
    public int Charger_High;
    public int No_Charger_Critical;
    public int No_Charger_Low;
    public int No_Charger_Medium;

}

package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import com.supercom.puretrack.model.business_logic_models.shielding.CellularStrengthModel;

import java.util.List;

public class HardwareUtilsManager {


    public interface DeviceShieldingEventListener {

        void onOpenEventConditionsFailed();

        void onCloseEventConditionsSuccess();
    }

    public enum ReceptionType {
        UNKNOWN(""),
        WCDMA_3G("3G"),
        LTE_4G("4G");

        public final String label;

        ReceptionType(String label) {
            this.label = label;
        }
    }

    public CellularStrengthModel calculateCellularStrength(final TelephonyManager telephonyManager) {
        List<CellInfo> cellInfo = telephonyManager.getAllCellInfo();
        if (cellInfo == null) return null;
        for (int i = 0; i < cellInfo.size(); i++) {
            if (!cellInfo.get(i).isRegistered()) continue;
            if (cellInfo.get(i) instanceof CellInfoWcdma) { //3G
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo.get(i);
                CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
//                DeviceShieldingManager.cellularReceptionString = "Network Type: " +  ReceptionType.WCDMA_3G.name() + ", Cellular Level DBM: " + cellSignalStrengthWcdma.getDbm();
                return new CellularStrengthModel(cellSignalStrengthWcdma.getDbm(), ReceptionType.WCDMA_3G);
            } else if (cellInfo.get(i) instanceof CellInfoLte) { //4G
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo.get(i);
                CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
//                DeviceShieldingManager.cellularReceptionString = "Network Type: " +  ReceptionType.LTE_4G.name() + ", Cellular Level DBM: " + cellSignalStrengthLte.getDbm();
                return new CellularStrengthModel(cellSignalStrengthLte.getDbm(), ReceptionType.LTE_4G);
            }
        }
        return null;
    }

    /**
     * @return - int - representing a percentage of the amount of high receptions to the total tested time.
     */
    public int getCellularValueCounterPercentage(int totalTime, int sampleInterval, int goodCellularReceptionsCounter) {
        totalTime = totalTime / 1000;
        int counterTimeForDivision;
        if (sampleInterval > 9999)
            counterTimeForDivision = sampleInterval / 10000;
        else
            counterTimeForDivision = sampleInterval / 1000;
        float actualSampleTime = (float) (totalTime / counterTimeForDivision);
        int result = Math.round(((float) goodCellularReceptionsCounter / actualSampleTime) * 100);
        return Math.min(result, 100);
    }
}

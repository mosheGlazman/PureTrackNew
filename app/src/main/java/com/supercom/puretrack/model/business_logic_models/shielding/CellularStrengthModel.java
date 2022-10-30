package com.supercom.puretrack.model.business_logic_models.shielding;

import com.supercom.puretrack.data.source.local.local_managers.hardware.HardwareUtilsManager;

public class CellularStrengthModel {

    private int dBmStrength;
    private HardwareUtilsManager.ReceptionType receptionType;

    public CellularStrengthModel(int dBmStrength, HardwareUtilsManager.ReceptionType receptionType) {
        this.dBmStrength = dBmStrength;
        this.receptionType = receptionType;
    }

    public int getDbmStrength() {
        return dBmStrength;
    }

    public int getSignalStrength() {
        switch (receptionType) {
            case WCDMA_3G:
                if (dBmStrength <= -110) return 0; // Dead Zone
                else if (dBmStrength <= -101) return 1; // Poor
                else if (dBmStrength <= -86) return 2; // Fair
                else if (dBmStrength >= -71) return 3; // Good
                else return 4; // Excellent >= -70
            case LTE_4G:
                if (dBmStrength <= -120) return 0; // Dead Zone
                else if (dBmStrength <= -111) return 1; // Poor
                else if (dBmStrength <= -106) return 2; // Fair
                else if (dBmStrength >= -91) return 3; // Good
                else return 4; // Excellent >= -90
            default:
                throw new IllegalStateException("Unexpected value: " + receptionType);
        }
    }

    public void setDbmStrength(int dBmStrength) {
        this.dBmStrength = dBmStrength;
    }

    public HardwareUtilsManager.ReceptionType getReceptionType() {
        return receptionType;
    }

    public void setReceptionType(HardwareUtilsManager.ReceptionType receptionType) {
        this.receptionType = receptionType;
    }
}

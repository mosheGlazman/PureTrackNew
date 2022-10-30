package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import com.supercom.puretrack.data.source.local.local_managers.hardware.MagneticManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityCaseTamper;

public class MagnetCaseManager {

    public void handleMagnetRecalibration(Context context) {
        EntityCaseTamper caseTamperEntity = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity();
        if (caseTamperEntity == null) return;
        boolean caseTamperEnabled = caseTamperEntity.enabled > 0;
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;
        if (!caseTamperEnabled || !isOffenderAllocated) return;


        Log.i("CaseTamperTest","handleMagnetRecalibration");

        final SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        final Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        MagneticManager.getInstance().setShouldRecalibrate(true);
        MagneticManager.getInstance().setMagnetRecalibrationListener(new MagneticManager.MagnetRecalibrationListener() {
            @Override
            public void onRecalibration() {
                MagneticManager.getInstance().setShouldRecalibrate(false);
                sensorManager.unregisterListener(MagneticManager.getInstance(), magneticSensor);
            }
        });
        sensorManager.registerListener(MagneticManager.getInstance(), magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

}

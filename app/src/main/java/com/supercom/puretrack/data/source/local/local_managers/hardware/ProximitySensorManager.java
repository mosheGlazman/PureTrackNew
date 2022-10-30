package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableSelfDiagnosticEvents;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableSelfDiagnosticManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.enums.CaseTamperOperator;
import com.supercom.puretrack.model.database.entities.EntityCaseTamper;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.objects.AccelerometerConfig;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.custom_implementations.OnOnlyFinishedCountdownTimer;
import com.supercom.puretrack.util.custom_implementations.OnOnlySensorChangedEventListener;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProximitySensorManager extends OnOnlySensorChangedEventListener {
    private static ProximitySensorManager instance;

    public static ProximitySensorManager getInstance() {
        if (instance == null) {
            instance = new ProximitySensorManager();
        }

        return instance;
    }

    SensorManager mSensorManager;
    Sensor mProximity;
    SensorListener listener;
    Context context;
    SensorData lastData = new SensorData(SensorData.E_SensorType.Proximity,0);
    Date startUp = new Date();

    private int SENSOR_SENSITIVITY = 4;

    private ProximitySensorManager() {
        register();
    }

    public void register() {
        context = App.getContext();

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                //near
                MagneticManager.getInstance().logToServer("ProximitySensor Near");
                lastData = new SensorData(SensorData.E_SensorType.Proximity,event.values[0]);
            } else {
                if(lastData!= null) {
                    if (lastData.date.getTime() - startUp.getTime() < 3000) {
                        MagneticManager.getInstance().logToServer("ProximitySensor Far at startup");
                        lastData=null;
                        return;
                    }
                }

                MagneticManager.getInstance().logToServer("ProximitySensor Far");
                lastData = new SensorData(SensorData.E_SensorType.Proximity,event.values[0]);
            }

            SensorDataSource.getInstance().add(lastData);
            if (listener != null) {
                listener.onReceivedSensorData(lastData);
            }
        }
    }
}

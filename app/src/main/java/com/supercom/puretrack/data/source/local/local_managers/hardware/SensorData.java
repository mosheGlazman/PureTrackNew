package com.supercom.puretrack.data.source.local.local_managers.hardware;

import java.util.Date;

public class SensorData {
    public SensorData(E_SensorType type){
        this(type,0);
    }
    public SensorData(E_SensorType type,double value){
        this.type = type;
        this.value = value;
    }
    public SensorData(E_SensorType type,String message){
        this.type = type;
        this.message = message;
    }
    public boolean hasChange(SensorData last) {

        if (type ==E_SensorType.Accelerometer){
            return Math.abs(last.value - value) > 150;
        }
        if (type ==E_SensorType.Light){
              return Math.abs(last.value - value) > 80;
        }
        if (type ==E_SensorType.Magnetics){
            return Math.abs(last.value - value) > 500;
        }

        return last.value!=value;
    }

    public enum E_SensorType {
        None,
        NFC,
        Magnetics,
        Proximity,
        Message,
        ErrorMessage,
        Screen,
        Accelerometer,
        Light
    }

    int counter=0;
    E_SensorType type;
    double value=0;
    String message="";
    Date date=new Date();
}

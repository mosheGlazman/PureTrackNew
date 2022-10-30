package com.supercom.puretrack.data.source.local.local_managers.hardware;

import static com.supercom.puretrack.data.source.local.local_managers.hardware.SensorData.E_SensorType.*;

import com.supercom.puretrack.data.source.local.local_managers.hardware.SensorData.E_SensorType;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

public class SensorDataSource {
    private static SensorDataSource instance;

    public static SensorDataSource getInstance() {
        if (instance == null) {
            instance = new SensorDataSource();
        }

        return instance;
    }

    ArrayList<SensorData> sensorsData;
    Hashtable<E_SensorType,SensorData> sensorsLastData;

    private SensorDataSource() {
        sensorsData=new ArrayList<>();
        sensorsLastData=new Hashtable<>();
        _showSensors= PureTrackSharedPreferences.getShowSensors();
    }
    boolean _showSensors;
    public boolean showSensors() {
        return  _showSensors;
    }

    public void setShowSensors(boolean b) {
        _showSensors=b;
        PureTrackSharedPreferences.setShowSensors(b);
    }

    public boolean add(SensorData data) {
        boolean isChange = true;

         if (sensorsLastData.containsKey(data.type)) {
            SensorData last = sensorsLastData.get(data.type);
            isChange = last.hasChange(data);
             if (isChange) {
                 sensorsLastData.remove(last.type);
                 sensorsLastData.put(data.type, data);
             }
         }else {
             sensorsLastData.put(data.type, data);
         }

        if (isChange) {
            sensorsData.add(0,data);
            return true;
        }

        return false;
    }

    SimpleDateFormat format_HH_mm_ss =new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

    int instanceId=1;
    public int getInstanceId() {
        return instanceId++;
    }

    public String toHtmlLog(){
        String res="";

        for(SensorData data:sensorsData){
            res+=toHtmlLog(data,false);
        }

        return res;
    }

    public String toHtmlLog(SensorData item,boolean isLast){
        String res="";

            String counterText = (item.counter <2) ? "" : "["+item.counter+"]";
            String value =  item.type + counterText+" ";

            switch (item.type){
                case NFC :{
                    value = toHtmlBlueSky(value);
                    break;
                }
                case Screen :{
                    value = toHtmlBlueSky(value)  +toHtmlWhite(item.value == 1 ?"ON" :"OFF");
                    break;
                }
                case Magnetics :{
                    value = toHtmlGreen(value)  +toHtmlWhite(item.value> 0 ?(item.value+"") : item.message);
                    break;
                }
                case Accelerometer :{
                    value = toHtmlPink(value)  +toHtmlWhite(item.value+"");
                    break;
                }
                case Message :{
                    value =toHtmlGray( item.value+"");
                    break;
                }
                case ErrorMessage :{
                    value = toHtmlBold(toHtmlRed(item.value+""));
                    break;
                }
                case Proximity:{
                    value = toHtmlRed(value) +toHtmlWhite(item.value == 0 ? "Near":"Far");
                    break;
                }
                case Light:{
                    value = toHtmlYellow(value) + toHtmlWhite(item.value+"");
                    break;
                }
            }

            res=toHtmlGray(format_HH_mm_ss.format(item.date))+" "+ value+"<BR>"+res;


        return res;
    }

    public static  String toHtmlGray(String text) { return "<font color=\"#cccccc\">" +  text+ "</font>";}
    public static  String toHtmlYellow(String text) { return "<font color=\"#ffd169\">" +  text+ "</font>";}
    public static  String toHtmlGreen(String text) { return "<font color=\"#60DC74\">" +  text+ "</font>";}
    public static  String toHtmlViolet(String text) { return "<font color=\"#8e7bce\">" +  text+ "</font>";}
    public static  String toHtmlBlueSky(String text) { return "<font color=\"#93B4B1\">" +  text+ "</font>";}
    public static  String toHtmlPink(String text) { return "<font color=\"#ff9ed0\">" +  text+ "</font>";}
    public static  String toHtmlRed(String text) { return "<font color=\"#be3246\">" +  text+ "</font>";}
    public static  String toHtmlBlack(String text) { return "<font color=\"black\">" +  text+ "</font>";}
    public static  String toHtmlWhite(String text) { return "<font color=\"#ffffff\">" +  text+ "</font>";}
    public static  String toHtmlBold(String text) { return "<B>" +  text+ "</B>";}
    public static  String toHtmlUnderline(String text) { return "<U>" +  text+ "</U>";}
    public static  String htmlEnter() { return "<BR>";}
    public static  String toHtmlH(String text,int level) { return "<H"+level+">" +  text+ "</H"+level+">";}

    public void clear() {
        sensorsData.clear();
    }
}

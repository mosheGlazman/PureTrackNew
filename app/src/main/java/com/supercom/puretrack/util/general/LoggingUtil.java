package com.supercom.puretrack.util.general;


import com.google.gson.Gson;
import com.supercom.puretrack.data.service.heart_beat.HeartBeatTaskJava;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.logs_file.WhiteListFileObject;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.date.DateFormatterUtil;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.hardware.FilesManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LoggingUtil {
    public static boolean isNetworkLogEnabled;
    public static boolean isZonesLogEnabled;
    public static boolean isBleLogEnabled;

    private static final int MAX_NETWORK_LOG_SIZE = (1024 * 1024 * 5);
    private static final String LOG_FILE_DATETIME_FORM = "yyyy-MM-dd_HH-mm-ss";

    private static String CurrNetworkLogFilePath = null;

    public interface OperationType {
        String ADV = "ADV";
        String PROXIMITY = "PROXIMITY";
    }

    public interface HardwareTypeString {
        String New_Tag = "Tag";
        String New_Beacon = "Beacon";
    }

    public static void createWhiteListConfigFile() {
        boolean whiteListEnabled = TableOffenderDetailsManager.sharedInstance()
                .getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONES_ACTIVE) == 1;
        ArrayList<String> incomingCallsWhiteListArrayList = DatabaseAccess.getInstance().tableOffenderDetails.getAllowedIncomingList();
        // create Logs directory is not exist
        File dir = new File(FilesManager.getInstance().CONFIG_FILES_DIRECTORY);
        if ((!dir.exists()) || (!dir.isDirectory())) {
            dir.mkdir();
        }
        File whitelistFile = new File(FilesManager.getInstance().WHITE_LIST_FILE);
        try {
            whitelistFile.createNewFile();
            Gson gson = new Gson();
            FileOutputStream fileOutputStream = new FileOutputStream(whitelistFile, false);
            fileOutputStream.write(gson.toJson(new WhiteListFileObject(whiteListEnabled, incomingCallsWhiteListArrayList)).getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String CreateNetworkLogFileName() {
        CurrNetworkLogFilePath = "";
        try {
            String fileName = String.format(Locale.US, "PT_Log_Network_%s.txt", TimeUtil.formatFromDate(new java.util.Date(), LOG_FILE_DATETIME_FORM));
            CurrNetworkLogFilePath = String.format(Locale.US, "%s%s", FilesManager.getInstance().LOG_FILES_DIRECTORY, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CurrNetworkLogFilePath;
    }

    public static void createNetworkLog() {
        // create Logs directory is not exist
        File dir = new File(FilesManager.getInstance().LOG_FILES_DIRECTORY);
        if ((!dir.exists()) || (!dir.isDirectory())) {
            dir.mkdir();
        }
        String NewFilename = CreateNetworkLogFileName();
        File logFile = new File(NewFilename);
        String startup = "\n---------  startup: " + TimeUtil.getCurrentTimeStr() + "  ---------" +
                App.getDeviceInfo() + "\n" + NewFilename;
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                FileOutputStream myOutWriter = new FileOutputStream(logFile, false);
                myOutWriter.write(startup.getBytes());
                myOutWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            updateNetworkLog("\n" + startup, false);
        }
    }

    private static void checkNetworkLogSize() {
        try {
            File myFile = new File(CurrNetworkLogFilePath);
            if (myFile.length() > (MAX_NETWORK_LOG_SIZE)) {
                CurrNetworkLogFilePath = CreateNetworkLogFileName();
                // create the new log file
                File logFile = new File(CurrNetworkLogFilePath);
                logFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void updateNetworkLog(String str, boolean forceWrite) {
        if ((isNetworkLogEnabled) || (forceWrite)) {
            try {
                // check if file exceeded size
                checkNetworkLogSize();
                File myFile = new File(CurrNetworkLogFilePath);
                FileOutputStream myOutWriter = new FileOutputStream(myFile, true);
                myOutWriter.write(str.getBytes());
                myOutWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createActivityLogsToFile() {
        File logFile = new File(FilesManager.getInstance().ACTIVITY_LOG_FILE);
        String startup = "---------  startup: " + TimeUtil.formatFromDate(new java.util.Date(), TimeUtil.SIMPLE) + "  ---------";
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                FileOutputStream myOutWriter = new FileOutputStream(logFile, false);
                myOutWriter.write(startup.getBytes());
                myOutWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            fileLogActivityUpdate("\n" + startup);
            //fileLogZonesUpdate("\n"+startup);
        }
    }


    public static void createLogZonesFiles() {
        File logFile = new File(FilesManager.getInstance().ZONES_LOG_FILE);
        String startup = "---------  startup: " + TimeUtil.formatFromDate(new java.util.Date(), TimeUtil.SIMPLE) + "  ---------";
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                FileOutputStream myOutWriter = new FileOutputStream(logFile, false);
                myOutWriter.write(startup.getBytes());
                myOutWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            fileLogZonesUpdate("\n" + startup);
        }
    }

    public static void fileLogActivityUpdate(String str) {
        if (isZonesLogEnabled) {
            try {
                File myFile = new File(FilesManager.getInstance().ACTIVITY_LOG_FILE);
                FileOutputStream myOutWriter = new FileOutputStream(myFile, true);
                myOutWriter.write(str.getBytes());
                myOutWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void fileLogZonesUpdate(String str) {
        if (isZonesLogEnabled) {
            try {
                File myFile = new File(FilesManager.getInstance().ZONES_LOG_FILE);
                FileOutputStream myOutWriter = new FileOutputStream(myFile, true);
                myOutWriter.write(str.getBytes());
                myOutWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createBleLogsToFile() {
        File logFile = new File(FilesManager.getInstance().BLE_LOG_FILE_CSV);
        try {
            logFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(logFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append("\n\n\n");
            myOutWriter.append("Date,TimeStamp,Type,Tag/Beacon,ID,RSSI,AdvCounter/HBCounter,Address,TagAdvIndex,Battery,MotionSticky, " +
                    "Strap/ProxTamperIndex,CaseTamperIndex,MotionTamperIndex,LastIntervalSeconds,Text\n");
            myOutWriter.append("\n");
            myOutWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeBleLogsToFile(String csvVile) {
        if (isBleLogEnabled) {
            try {
                File logFile = new File(FilesManager.getInstance().BLE_LOG_FILE_CSV);
                FileOutputStream fOut = new FileOutputStream(logFile, true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(csvVile);
                myOutWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String createStringForCSVFile(String operationType, String hardwareType, String tempTagId,
                                                int lastRssiTagFix, long ADVOrHBCounter, String tagAddress, int rollingCode, float floatBattery, boolean bleMotionTamperSticky,
                                                int strapOrProximityTamperIndexNew, int caseTamperIndex, int motionTamperIndex, long lastInterval, String text) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", TimeUtil.getCurrentTimeStr(),
                TimeUtil.formatFromMiliSecondToString(System.currentTimeMillis(), DateFormatterUtil.HMS), operationType, hardwareType, tempTagId, lastRssiTagFix, ADVOrHBCounter, tagAddress,
                rollingCode, floatBattery, bleMotionTamperSticky, strapOrProximityTamperIndexNew, caseTamperIndex, motionTamperIndex,
                lastInterval, text);
    }


    public static Boolean isHeartBeatLogEnabled;
   static SimpleDateFormat time_format=new SimpleDateFormat("HH:mm:ss.SSS");
   static SimpleDateFormat dateTime_format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static void updateHeartBeatLog(HeartBeatTaskJava task) {
        updateHeartBeatLog("result:"+task.result +" start at "+time_format.format(task.startTime));
    }

    public static void updateHeartBeatLog(String str) {
        if (isHeartBeatLogEnabled == null) {
            initHeartBeatLog();
        }

        if (!isHeartBeatLogEnabled) {
            return;
        }

         str=dateTime_format.format(new Date())+"\t"+str;
        str+="\n\r";

        try {
            File myFile = new File(FilesManager.getInstance().HEARTBEAT_LOG_FILE);
            FileOutputStream myOutWriter = new FileOutputStream(myFile, true);
            myOutWriter.write(str.getBytes());
            myOutWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initHeartBeatLog() {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date timeoutDate=format.parse("2022-05-12");
            isHeartBeatLogEnabled=timeoutDate.getTime()>new Date().getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            isHeartBeatLogEnabled=false;
         }

        if (!isHeartBeatLogEnabled) {
            return;
        }

        File logFile = new File(FilesManager.getInstance().HEARTBEAT_LOG_FILE);
        String startup = "---------  startup: " + TimeUtil.formatFromDate(new java.util.Date(), TimeUtil.SIMPLE) + "  ---------";

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                FileOutputStream myOutWriter = new FileOutputStream(logFile, false);
                myOutWriter.write(startup.getBytes());
                myOutWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            fileLogZonesUpdate("\n" + startup);
        }
    }
}

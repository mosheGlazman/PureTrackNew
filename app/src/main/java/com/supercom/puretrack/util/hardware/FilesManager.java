package com.supercom.puretrack.util.hardware;

import android.os.Build;
import android.util.Log;

import com.supercom.puretrack.util.application.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FilesManager {
    public static FilesManager instance;
    public static String TAG = "FilesManager";

    public static FilesManager getInstance() {
        if (instance == null) {
            instance = new FilesManager();
        }
        return instance;
    }

    private FilesManager() {
        externalPath = "/storage/emulated/0/download/";
        path = externalPath;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File file = App.getContext().getDataDir();
            path = file.getAbsolutePath();
            path += "/files";
        }

        CONFIG_FILES_DIRECTORY = path + "/PureTrack_Configs/";
        SHIELDING_FILE = CONFIG_FILES_DIRECTORY + "DeviceShieldingFile.txt";
        WHITE_LIST_FILE = CONFIG_FILES_DIRECTORY + "white_list.txt";

        LOG_FILES_DIRECTORY = path + "/PureTrack_Logs/";
        HEARTBEAT_LOG_FILE = path + "/Logs/PureTrackLog_HeartBeat.txt";
        ZONES_LOG_FILE = path + "/Logs/PureTrackLog_Zones.txt";
        BLE_LOG_FILE_CSV = path + "/Logs/PureTrackLog_Ble_Tag.csv";
        ACTIVITY_LOG_FILE = path + "/Logs/PureTrackLog_Activity.txt";
        DATABASE_NAME = path + "/PureTrackDatabase.db";
        DATABASE_NAME_JOURNAL = path + "/PureTrackDatabase.db-journal";
        APK_LOCATION = path + "/apk";
        KNOX_CONFIG_FILE_START = "knox_config";
        KNOX_CONFIG_FILE_EXT = "set";

        if (Build.VERSION.SDK_INT >= 31) {
            KNOX_CONFIG_FOLDER = "/storage/emulated/0/download/Contents/";
        }else{
            KNOX_CONFIG_FOLDER = "/storage/emulated/0/Contents/";
        }
        String[] directories = new String[]{
                "PureTrack_Configs","Logs","PureTrack_Logs","apk",
        };

        for (String s : directories) {
            String p=path + "/"+s;
            if (!new File(p).exists()) {
                try {
                    new File(p).mkdirs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String KNOX_CONFIG_FILE_START,KNOX_CONFIG_FILE_EXT;

    public String APK_LOCATION;
    public String LOG_FILES_DIRECTORY;
    public String CONFIG_FILES_DIRECTORY;
    public String ZONES_LOG_FILE;
    public String HEARTBEAT_LOG_FILE;
    public String BLE_LOG_FILE_CSV;
    public String ACTIVITY_LOG_FILE;
    public String SHIELDING_FILE;
    public String WHITE_LIST_FILE;
    public String DATABASE_NAME;
    public String DATABASE_NAME_JOURNAL;
    public String KNOX_CONFIG_FOLDER;

    private String path;
    private String externalPath;

    public void copyOldFilesFromExternal() {
        if (externalPath.equals(path)) {
            return;
        }

        if (!new File(externalPath).exists()) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!new File(DATABASE_NAME).exists()) {
                        Log.i(TAG, "db not exist");
                        Thread.sleep(2000);
                    }
                    Log.i(TAG, "db exist");
                    Thread.sleep(1000);
                    copyDirectory(new File(externalPath), new File(path));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            Log.i(TAG, "copy dir " + sourceLocation.getName());
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
            return;
        }

        Log.i(TAG, "copy file " + sourceLocation.getName());
        // make sure the directory we plan to store the recording in exists
        File directory = targetLocation.getParentFile();
        if (directory != null && !directory.exists() && !directory.mkdirs()) {
            throw new IOException("Cannot create dir " + directory.getAbsolutePath());
        }

        InputStream in = new FileInputStream(sourceLocation);
        OutputStream out = new FileOutputStream(targetLocation);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        Log.i(TAG, "copy success: " + sourceLocation.exists());
    }

    public void deleteOldFilesFromExternal() {
        deleteFileOrDirectory(externalPath);
    }

    private void deleteFileOrDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        if (!file.isDirectory()) {
            try {
                Log.i(TAG, "deleteFile(" + path + ") -> " + file.delete());
            } catch (Exception e) {
                Log.e(TAG, "error", e);
                e.printStackTrace();
            }
            return;
        }

        Log.i(TAG, "deleteDirectoryFiles(" + path + ")");
        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                deleteFileOrDirectory(child.getAbsolutePath());
            }
        }

        try {
            Log.i(TAG, "deleteDirectory(" + path + ") -> " + file.delete());
        } catch (Exception e) {
            Log.e(TAG, "error", e);
            e.printStackTrace();
        }
    }


    public void deleteDatabaseFiles() {
        File logFileName = new File(DATABASE_NAME);
        logFileName.delete();

        File logFileNameJournal = new File(DATABASE_NAME_JOURNAL);
        logFileNameJournal.delete();
    }

    public File getMainPath() {
        return new File(path);
    }
}

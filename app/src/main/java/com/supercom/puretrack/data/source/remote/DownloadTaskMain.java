package com.supercom.puretrack.data.source.remote;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.remote.DownloadTaskNetwork.DownloadTaskNetworkListener;
import com.supercom.puretrack.util.hardware.FilesManager;

import java.io.File;
import java.lang.ref.WeakReference;

public class DownloadTaskMain implements DownloadTaskNetworkListener {

    public static final String TAG = "DownloadTask";

    //	private Context context;
    private final WeakReference<Context> context;
    private final String apkTargetShortFileName;
    private final File apkTargetFile;
    private final DownloadTaskListener downloadTaskListener;
    private final String versionFromServer;
    private ProgressDialog downloadProgressDialog;
    private AlertDialog errorDownloadDialog;
    private final Download_Task_Type downloadTaskType;


    public enum Download_Task_Type {
        PT_Version_Upgrade,
        All_Apk_Upgrade,
        Google_Play_Version
    }

    public DownloadTaskMain(Context context, String apkTargetFileName, DownloadTaskListener downloadTaskListener, String versionFromServer, Download_Task_Type downloadTaskType) {
        this.context = new WeakReference<>(context);
        this.apkTargetShortFileName = apkTargetFileName.substring(0, apkTargetFileName.indexOf("_"));
        this.apkTargetFile = new File(FilesManager.getInstance().APK_LOCATION + apkTargetFileName);
        this.downloadTaskListener = downloadTaskListener;
        this.versionFromServer = versionFromServer;
        this.downloadTaskType = downloadTaskType;
    }

    public interface DownloadTaskListener {
        void onSucceededToDownloadFileFromServer(File apkTargetFile, String apkTargetShortFileName, String versionFromServer, Download_Task_Type downloadTaskType);

        void onFailedToDownloadFileFromServer(String result, String apkTargetShortFileName, String versionFromServer, Download_Task_Type downloadTaskType);
    }

    public void onPreExecute() {
        CreateDialog();
    }

    public void onProgressUpdate(Integer... progress) {
        // if we get here, length is known, now set indeterminate to false
        downloadProgressDialog.setIndeterminate(false);
        downloadProgressDialog.setMax(100);
        downloadProgressDialog.setProgress(progress[0]);
    }

    public void onPostExecute(String result) {
        downloadProgressDialog.dismiss();

        // download failed
        if (result != null) {
            downloadTaskListener.onFailedToDownloadFileFromServer(result, apkTargetShortFileName, versionFromServer, downloadTaskType);
        }
        // download succeeded
        else {
            if (errorDownloadDialog != null && errorDownloadDialog.isShowing()) {
                errorDownloadDialog.dismiss();
            }
            downloadTaskListener.onSucceededToDownloadFileFromServer(apkTargetFile, apkTargetShortFileName, versionFromServer, downloadTaskType);
        }
    }

    private void CreateDialog() {
        downloadProgressDialog = new ProgressDialog(context.get());
        if (downloadTaskType == Download_Task_Type.PT_Version_Upgrade) {
            downloadProgressDialog.setMessage(context.get().getString(R.string.dialog_text_puretrack_upgrade));
        } else if (downloadTaskType == Download_Task_Type.Google_Play_Version) {
            downloadProgressDialog.setMessage(context.get().getString(R.string.dialog_text_google_play_services_upgrade));
        } else if (downloadTaskType == Download_Task_Type.All_Apk_Upgrade) {
            String msg = String.format(context.get().getString(R.string.dialog_text_all_apk_update), this.apkTargetShortFileName);
            downloadProgressDialog.setMessage(msg);
        }
        downloadProgressDialog.setIndeterminate(true);
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.setCancelable(false);
        downloadProgressDialog.show();
    }

    public void downloadFromURL(String url) {
        new DownloadTaskNetwork(this, apkTargetFile).execute(url);
    }

}

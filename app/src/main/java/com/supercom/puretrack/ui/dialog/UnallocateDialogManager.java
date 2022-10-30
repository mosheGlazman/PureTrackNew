package com.supercom.puretrack.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager.BluetoothManagerListener;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.BeaconModel;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.TagModel;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.dialog.UnallocateDialog.ScreenType;
import com.supercom.puretrack.ui.dialog.UnallocateDialog.UnallocateDialogListener;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.runnable.BaseFutureRunnable;

import java.util.concurrent.TimeUnit;

public class UnallocateDialogManager implements UnallocateDialogListener, BluetoothManagerListener {
    private UnallocateDialog unallocateDialog;
    private final Context context;
    private final BluetoothManager packetManager;
    private boolean lastStrapOpenStatus;
    private final Handler futureTasksHandler = new Handler();
    private final ProcessScreenAllowedTimeOut processScreenAllowedTimeOut = new ProcessScreenAllowedTimeOut();
    private final long processScreenTimeOut = TimeUnit.SECONDS.toMillis(30);


    public UnallocateDialogManager(Context context) {
        this.context = context;

        packetManager = new BluetoothManager(this, false);

        createUnallocateDialog();
    }

    public void createUnallocateDialog() {
        this.unallocateDialog = new UnallocateDialog(context, this);
    }

    public void showNoticeScreen() {
        unallocateDialog.show();
        unallocateDialog.setNoticeScreen();
        lastStrapOpenStatus = false;
        scanLeDeviceNew();
    }

    @Override
    public void onPressedContinueToCutStrap() {
        unallocateDialog.setConfirmationScreen();
    }

    @Override
    public void onPressedCancelToCutStrap() {
        stopBleScan();
    }

    @SuppressLint("NewApi")
    private void scanLeDeviceNew() {
        packetManager.startScan();
    }

    @SuppressLint("NewApi")
    public void stopBleScan() {
        packetManager.stopScan();
    }

    //@Override
    public void onBluetoothManagerModelsHandled(BeaconModel beaconModel, TagModel tagModel) {
        if (tagModel != null) {
            switch (unallocateDialog.getScreenType()) {
                case NoticeScreen:

                    App.writeToNetworkLogsAndDebugInfo("TagOff",
                            "lastStrapOpenStatus: " + lastStrapOpenStatus + ", is open: " + tagModel.isTagStrapOpen()
                            , DebugInfoModuleId.Ble);

                    if (tagModel.isTagStrapOpen() && !lastStrapOpenStatus) {

                        lastStrapOpenStatus = true;

                        unallocateDialog.updateNoticeScreen(R.drawable.ico_tag_open_center, R.string.dialog_unallocate_notice_stap_open_body, true);
                    } else if (!tagModel.isTagStrapOpen() && lastStrapOpenStatus) {
                        lastStrapOpenStatus = false;
                        unallocateDialog.updateNoticeScreen(R.drawable.ico_tag_center, R.string.dialog_unallocate_notice_stap_close_body, false);
                    }
                    break;
                case ConfirmationScreen:
                case TurnedOffErrorScreen:
                    if (!tagModel.isTagStrapOpen()) {
                        unallocateDialog.setNoticeScreen();
                        scanLeDeviceNew();
                    }
                default:
                    break;
            }
        }
    }

    @Override
    public void onStartTagTurnOffProcess() {
        unallocateDialog.setProcessingScreen();
        processScreenAllowedTimeOut.scheduleFutureRun(futureTasksHandler, processScreenTimeOut);

//        GattOpFlowSendTagOff gattOpFlowSendTagOff = new GattOpFlowSendTagOff();
//        gattOpFlowSendTagOff.setCallbackListener(new IGattOperationFlowCallbackListener() {
//
//            @Override
//            public void onOperationSuccess(String opCode) {
//                unallocateDialog.setTurnedOffSuccessScreen();
//            }
//
//            @Override
//            public void onOperationError(String opCode, String action, int status) {
//                unallocateDialog.setTurnedOffErrorScreen();
//            }
//
//        });
//        BluetoothGattActionsManager.getInstance().runGattOperationFlow(gattOpFlowSendTagOff);

    }

    class ProcessScreenAllowedTimeOut extends BaseFutureRunnable {
        @Override
        public void run() {
            if (unallocateDialog.getScreenType() == ScreenType.ProcessingScreen) {
                unallocateDialog.setTurnedOffErrorScreen();
            }
        }
    }

    @Override
    public void onOpenBeaconEventStatusChanged() {
    }
}

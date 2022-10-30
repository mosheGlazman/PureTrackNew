package com.supercom.puretrack.ui.enrollment.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager.BluetoothManagerListener;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.BeaconModel;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.TagModel;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.enrollment.activity.EnrollmentActivity;
import com.supercom.puretrack.util.constants.Enrollment;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.runnable.BaseFutureRunnable;

import java.util.concurrent.TimeUnit;

public class DevicesVerificationFragment extends Fragment implements OnClickListener, BluetoothManagerListener {

    private static final int TIME_TO_SCAN = 30;

    public static final String TAG = "DevicesVerificationFragment";

    private final TestTag testTag = new TestTag();
    private final TestBeacon testBeacon = new TestBeacon();
    private final Handler futureTasksHandler = new Handler();
    private View view;
    private BeaconModel beaconPacketData;
    private TagModel tagPacketData;
    private DevicesVerificationEnrollmentListener devicesVerificationListener;
    private AnimationDrawable beaconAnimation;
    private AnimationDrawable tagAnimation;
    private boolean shouldHandleBeaconResult;
    private boolean shouldHandleTagResult;

    private BluetoothManager bluetoothManager;

    public static DevicesVerificationFragment newInstance() {
        return new DevicesVerificationFragment();
    }

    public interface DevicesVerificationEnrollmentListener {
        void onFinishedToTestDevices(boolean didSkipScreen);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothManager = new BluetoothManager(this, false);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DevicesVerificationEnrollmentListener) {
            devicesVerificationListener = (DevicesVerificationEnrollmentListener) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement DevicesVerificationEnrolmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.enrollment_devices_verification_main, container, false);

        String firstName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_FIRST_NAME);
        String lastName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_LAST_NAME);

        TextView secondTitleTextView = view.findViewById(R.id.second_title);
        secondTitleTextView.setText(String.format("%s %s", firstName, lastName));

        String tagId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ID);
        String tagRfId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
        if (!tagRfId.equals(BluetoothManager.NO_TAG)) {

            shouldHandleTagResult = true;
            testTag.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(TIME_TO_SCAN));

            TextView tagTitleTextView = view.findViewById(R.id.tag_title_text);
            tagTitleTextView.setText(getResources().getString(R.string.enrolment_text_pure_tag, tagId));

            ImageButton tagRetryImageButton = view.findViewById(R.id.tag_retry_button);
            tagAnimation = (AnimationDrawable) tagRetryImageButton.getDrawable();
            tagAnimation.start();
        } else {
            view.findViewById(R.id.tag_test_layout).setVisibility(View.GONE);
        }

        int BeaconZoneId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID);
        boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(BeaconZoneId) != null;
        if (isBeaconExistsInDBZone) {

            shouldHandleBeaconResult = true;
            testBeacon.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(TIME_TO_SCAN));

            int beaconId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ID);
            TextView beaconTitleTextView = view.findViewById(R.id.beacon_title_text);
            beaconTitleTextView.setText(getResources().getString(R.string.enrolment_text_pure_beacon, beaconId));

            ImageButton beaconRetryImageButton = view.findViewById(R.id.beacon_retry_button);
            beaconAnimation = (AnimationDrawable) beaconRetryImageButton.getDrawable();
            beaconAnimation.start();

        } else {
            view.findViewById(R.id.beacon_test_layout).setVisibility(View.GONE);
        }

        scanLeDeviceNew();

        Button leftButton = view.findViewById(R.id.left_button);
        leftButton.setOnClickListener(this);

        Button rightButton = view.findViewById(R.id.right_button);
        rightButton.setOnClickListener(this);
        rightButton.setTag(EnrollmentActivity.SKIPPED);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.left_button:
                getActivity().onBackPressed();
                break;

            case R.id.right_button:

                stopBleScan();

                String messageToUpload;
                int status = (int) v.getTag();
                switch (status) {

                    case EnrollmentActivity.FINISHED:
                        messageToUpload = "Finished device enrollment";
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                        devicesVerificationListener.onFinishedToTestDevices(false);

                        break;

                    case EnrollmentActivity.SKIPPED:
                        messageToUpload = "Skipped device enrollment";
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                        devicesVerificationListener.onFinishedToTestDevices(true);

                        break;

                    default:
                        break;

                }

                break;

            case R.id.tag_retry_button:

                startTagSearching();

                break;

            case R.id.beacon_retry_button:

                startBeaconSearching();

                break;

        }
    }

    private void startTagSearching() {

        LoggingUtil.updateNetworkLog("\nCalling 'start new cycle' - startTagSearching\n", false);
        NetworkRepository.getInstance().startNewCycle();

        tagPacketData = null;

        String tagId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ID);
        TextView tagTitleTextView = view.findViewById(R.id.tag_title_text);
        tagTitleTextView.setText(getResources().getString(R.string.enrolment_text_pure_tag, tagId));

        TextView tagStatusTextView = view.findViewById(R.id.tag_status_text);
        tagStatusTextView.setTextColor(getResources().getColor(R.color.grey_darker));
        tagStatusTextView.setText(R.string.enrolment_text_test_tag);

        TextView tagMessageTextView = view.findViewById(R.id.tag_message_text);
        tagMessageTextView.setText("");

        ImageButton tagRetryImageButton = view.findViewById(R.id.tag_retry_button);
        tagRetryImageButton.setImageResource(R.drawable.enrollment_tag_device_testing_animation);
        tagRetryImageButton.setOnClickListener(null);

        tagAnimation = (AnimationDrawable) tagRetryImageButton.getDrawable();
        tagAnimation.start();

        testTag.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(TIME_TO_SCAN));

        scanLeDeviceNew();
    }

    private void startBeaconSearching() {

        //init beacon fields
        int BeaconId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID);
        boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(BeaconId) != null;
        if (isBeaconExistsInDBZone) {

            beaconPacketData = null;

            TextView beaconStatusTextView = view.findViewById(R.id.beacon_status_text);
            beaconStatusTextView.setTextColor(getResources().getColor(R.color.grey_darker));
            beaconStatusTextView.setText(R.string.enrolment_text_test_beacon);

            TextView beaconMessageTextView = view.findViewById(R.id.beacon_message_text);
            beaconMessageTextView.setText("");

            testBeacon.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(TIME_TO_SCAN));

            ImageButton beaconRetryImageButton = view.findViewById(R.id.beacon_retry_button);
            beaconRetryImageButton.setImageResource(R.drawable.enrollment_beacon_device_testing_animation);
            beaconRetryImageButton.setOnClickListener(null);

            beaconAnimation = (AnimationDrawable) beaconRetryImageButton.getDrawable();
            beaconAnimation.start();

            testBeacon.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(TIME_TO_SCAN));

            scanLeDeviceNew();
        }

    }

    class TestTag extends BaseFutureRunnable {

        @Override
        public void run() {
            if (isAdded()) {
                showTagErrorMessage();
                stopBleScan();
            }
        }
    }

    class TestBeacon extends BaseFutureRunnable {

        @Override
        public void run() {
            if (isAdded()) {
                showBeaconErrorMessage();
                stopBleScan();
            }
        }
    }

    private void showBeaconErrorMessage() {

        StringBuilder beaconError = new StringBuilder();

        //beacon timeout
        if (beaconPacketData == null) {
            beaconError.append(getString(R.string.enrolment_text_device_beacon_timeout_error));
        } else {

            //beacon case
            boolean isBeaconCaseOpen = beaconPacketData.isBeaconTamperCaseOpen();

            if (isBeaconCaseOpen) {
                beaconError.append(getString(R.string.enrolment_text_device_beacon_case_error));
            }

            //beacon proximity
            boolean isBeaconInProximity = beaconPacketData.isBeaconTamperProximityOpen();

            if (isBeaconInProximity) {
                beaconError.append(getString(R.string.enrolment_text_device_proximity_error));
            }

            //beacon battery
            boolean isBeaconBatteryLow = beaconPacketData.isBatteryTamperCurrent();
            if (isBeaconBatteryLow) {
                beaconError.append(getString(R.string.enrolment_text_device_battery_error));
            }
        }

        TextView beaconStatusTextView = view.findViewById(R.id.beacon_status_text);
        beaconStatusTextView.setText(R.string.enrolment_text_Verification_failed);
        beaconStatusTextView.setTextColor(getResources().getColor(R.color.enrollment_device_error_color));

        TextView beaconMessageTextView = view.findViewById(R.id.beacon_message_text);
        beaconMessageTextView.setText(beaconError);

        ImageButton beaconRetryImageButton = view.findViewById(R.id.beacon_retry_button);
        beaconRetryImageButton.setImageResource(R.drawable.ico_enrollment_beacon_device_error_retry);
        beaconRetryImageButton.setOnClickListener(this);

        beaconAnimation.stop();

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                beaconError.toString(), DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

    }

    private void showTagErrorMessage() {
        StringBuilder tagError = new StringBuilder();
        if (tagPacketData == null) {
            tagError.append(getString(R.string.enrolment_text_device_tag_timeout_error));
        } else {

            //tag strap
            boolean isStrapOpen = tagPacketData.isTagStrapOpen();

            if (isStrapOpen) {
                tagError.append(getString(R.string.enrolment_text_device_stap_error));
            }

            //tag case
            boolean isTagCaseOpen = tagPacketData.isTagTamperCaseOpen();

            if (isTagCaseOpen) {
                tagError.append(getString(R.string.enrolment_text_device_tag_case_error));
            }

            //tag battery
            boolean isTagBatteryLow = tagPacketData.isBatteryTamperCurrent();

            if (isTagBatteryLow) {
                tagError.append(getString(R.string.enrolment_text_device_battery_error));
            }
        }

        TextView tagStatusTextView = view.findViewById(R.id.tag_status_text);
        tagStatusTextView.setText(R.string.enrolment_text_Verification_failed);
        tagStatusTextView.setTextColor(getResources().getColor(R.color.enrollment_device_error_color));

        TextView tagMessageTextView = view.findViewById(R.id.tag_message_text);
        tagMessageTextView.setText(tagError.toString());

        ImageButton tagRetryImageButton = view.findViewById(R.id.tag_retry_button);
        tagRetryImageButton.setImageResource(R.drawable.ico_enrollment_tag_device_error_retry);
        tagRetryImageButton.setOnClickListener(this);

        tagAnimation.stop();

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                tagError.toString(), DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }


    @SuppressLint("NewApi")
    private void scanLeDeviceNew() {
        bluetoothManager.startScan();
    }


    public void stopBleScan() {
        bluetoothManager.stopScan();
    }

    @Override
    public void onBluetoothManagerModelsHandled(BeaconModel beaconModel, TagModel tagModel) {
        if (beaconModel != null && shouldHandleBeaconResult) {
            this.beaconPacketData = beaconModel;
            handleFoundBeaconResult();
        }

        if (tagModel != null && shouldHandleTagResult) {
            this.tagPacketData = tagModel;
            handleFoundTagResult();
        }

    }

    private void handleFoundBeaconResult() {
        if (isAdded()) {

            int BeaconId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID);
            boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(BeaconId) != null;

            if (isBeaconExistsInDBZone) {

                // beacon case
                boolean isBeaconCaseOpen = beaconPacketData.isBeaconTamperCaseOpen();

                // beacon proximity
                boolean isBeaconInProximity = beaconPacketData.isBeaconTamperProximityOpen();

                //beacon battery
                boolean isBeaconBatteryLow = beaconPacketData.isBatteryTamperCurrent();

                if (!isBeaconCaseOpen && !isBeaconInProximity && !isBeaconBatteryLow) {

                    TextView beaconStatusTextView = view.findViewById(R.id.beacon_status_text);
                    beaconStatusTextView.setTextColor(getResources().getColor(R.color.green));
                    beaconStatusTextView.setText(R.string.enrolment_text_verified);

                    TextView beaconMessageTextView = view.findViewById(R.id.beacon_message_text);
                    beaconMessageTextView.setText("");

                    futureTasksHandler.removeCallbacks(testBeacon);

                    ImageButton beaconRetryImageButton = view.findViewById(R.id.beacon_retry_button);
                    beaconRetryImageButton.setOnClickListener(null);
                    beaconRetryImageButton.setImageResource(R.drawable.ico_enrollment_beacon_device_ok);

                    beaconAnimation.stop();

                    //we will stop scan, if already got "good" tag result in addition to beacon
                    if (!shouldHandleTagResult) {
                        stopBleScan();
                    }

                    Button rightButton = view.findViewById(R.id.right_button);
                    if (((EnrollmentActivity) getActivity()).getNextScreen(Enrollment.TAG_SETUP_STEP) == null) {
                        rightButton.setText(R.string.enrolment_button_finish);
                    } else {
                        rightButton.setText(R.string.enrolment_button_next);
                    }
                    rightButton.setTag(EnrollmentActivity.FINISHED);

                    shouldHandleBeaconResult = false;
                }
            }
        }
    }

    private void handleFoundTagResult() {
        if (isAdded()) {

            // tag strap
            boolean isStrapOpen = tagPacketData.isTagStrapOpen();

            // tag case
            boolean isTagCaseOpen = tagPacketData.isTagTamperCaseOpen();

            //tag battery
            boolean isTagBatteryLow = tagPacketData.isBatteryTamperCurrent();

            if (!isStrapOpen && !isTagCaseOpen && !isTagBatteryLow) {
                futureTasksHandler.removeCallbacks(testTag);

                TextView tagStatusTextView = view.findViewById(R.id.tag_status_text);
                tagStatusTextView.setTextColor(getResources().getColor(R.color.green));
                tagStatusTextView.setText(R.string.enrolment_text_verified);

                TextView tagMessageTextView = view.findViewById(R.id.tag_message_text);
                tagMessageTextView.setText("");

                ImageButton tagRetryImageButton = view.findViewById(R.id.tag_retry_button);
                tagRetryImageButton.setOnClickListener(null);
                tagRetryImageButton.setImageResource(R.drawable.ico_enrollment_tag_device_ok);

                tagAnimation.stop();

                //we will stop scan, if already got "good" beacon result in addition to tag
                if (!shouldHandleBeaconResult) {
                    stopBleScan();
                }

                Button rightButton = view.findViewById(R.id.right_button);
                if (((EnrollmentActivity) getActivity()).getNextScreen(Enrollment.TAG_SETUP_STEP) == null) {
                    rightButton.setText(R.string.enrolment_button_finish);
                } else {
                    rightButton.setText(R.string.enrolment_button_next);
                }
                rightButton.setTag(EnrollmentActivity.FINISHED);

                shouldHandleTagResult = false;
            }
        }
    }

    @Override
    public void onOpenBeaconEventStatusChanged() {
    }

}

package com.supercom.puretrack.ui.enrollment.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.MagnetCaseManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.MagneticManager;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.EventTypes;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.ui.activity.BaseActivity;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.ui.enrollment.fragments.DevicesVerificationFragment;
import com.supercom.puretrack.ui.enrollment.fragments.DevicesVerificationFragment.DevicesVerificationEnrollmentListener;
import com.supercom.puretrack.ui.enrollment.fragments.KnoxInstallFragment;
import com.supercom.puretrack.ui.enrollment.fragments.KnoxInstallFragment.KnoxInstallEnrollmentListener;
import com.supercom.puretrack.ui.enrollment.fragments.LocationVerificationFragment;
import com.supercom.puretrack.ui.enrollment.fragments.LocationVerificationFragment.LocationVerificationEnrollmentListener;
import com.supercom.puretrack.ui.enrollment.fragments.OffenderDetailsFragment;
import com.supercom.puretrack.ui.enrollment.fragments.OffenderDetailsFragment.OffenderDetailsEnrollmentListener;
import com.supercom.puretrack.ui.enrollment.fragments.OffenderFingerPrintFragment;
import com.supercom.puretrack.ui.enrollment.fragments.OffenderFingerPrintFragment.OffenderFingerPrintEnrollmentListener;
import com.supercom.puretrack.util.constants.Enrollment;

import java.util.ArrayList;

public class EnrollmentActivity extends BaseActivity implements
        DevicesVerificationEnrollmentListener,
        OffenderFingerPrintEnrollmentListener,
        LocationVerificationEnrollmentListener,
        OffenderDetailsEnrollmentListener,
        KnoxInstallEnrollmentListener {

    public static final String ENROLMENT_DEVICES = "ENROLMENT_DEVICES";
    public static final int FINISHED = 1;
    public static final int SKIPPED = 2;

    //Class Variables - booleans
    private boolean didUserSkipOffenderDetailsScreen;
    private boolean didUserSkipDevicesScreen;
    private boolean didUserSkipFingerprintScreen;
    private boolean didUserSkipLocationScreen;
    private boolean didUserSkipKnoxScreen;

    //Class Variables - Dependencies
    MagnetCaseManager magnetCaseManager = new MagnetCaseManager();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreen();

        setContentView(R.layout.enrollment_screen_main);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //getActionBar().hide();

        boolean didFindScreenToShow = false;

        ArrayList<String> enrolmentScreensToShowArray = TableOffenderDetailsManager.sharedInstance().getEnrollmentScreensToShow();
        for (String screenToShowItem : enrolmentScreensToShowArray) {
            Fragment screenToShowFragment = getScreenToShow(screenToShowItem);

            if (screenToShowFragment != null) {
                didFindScreenToShow = true;
                openNewScreen(screenToShowFragment);
                break;
            }
        }

        if (!didFindScreenToShow) {
            finish();
        }


        magnetCaseManager.handleMagnetRecalibration(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isCameFromRegularActivityCode = false;
    }

    private void openNewScreen(Fragment screenToShowFragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction().add(R.id.main_layout,
                screenToShowFragment, screenToShowFragment.getClass().getName());
        fragmentTransaction.addToBackStack(screenToShowFragment.getClass().getName());
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {

        FragmentManager fm = getFragmentManager();

        stopFragmentActivities(fm);

        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
            return;
        }
        finish();
    }

    private void stopFragmentActivities(FragmentManager fm) {
        int index = fm.getBackStackEntryCount() - 1;
        if (index >= 0) {
            BackStackEntry backStackEntryAt = fm.getBackStackEntryAt(index);
            String tagId = backStackEntryAt.getName();
            if (tagId.equals(DevicesVerificationFragment.class.getName())) {
                DevicesVerificationFragment devicesVerificationFragment = (DevicesVerificationFragment) fm.findFragmentByTag(tagId);
                devicesVerificationFragment.stopBleScan();
            } else if (tagId.equals(LocationVerificationFragment.class.getName())) {
                LocationVerificationFragment locationVerificationFragment = (LocationVerificationFragment) fm.findFragmentByTag(tagId);
                locationVerificationFragment.stopEnrollmentLocationUpdate();
            }
        }
    }

    public Fragment getNextScreen(String screenName) {
        ArrayList<String> enrollmentScreensToShowList = TableOffenderDetailsManager.sharedInstance().getEnrollmentScreensToShow();
        int nextScreenNumber = enrollmentScreensToShowList.indexOf(screenName) + 1;
        for (int i = nextScreenNumber; i < enrollmentScreensToShowList.size(); i++) {
            String screenNumber = enrollmentScreensToShowList.get(i);
            Fragment screenToShowFragment = getScreenToShow(screenNumber);
            if (screenToShowFragment != null) {
                return screenToShowFragment;
            }
        }

        return null;
    }


    @Override
    public void onFinishedOffenderDetailsPreview(boolean didSkipScreen) {
        didUserSkipOffenderDetailsScreen = didSkipScreen;
        handleScreenFinished(Enrollment.OFFENDER_DETAILS_STEP);
    }

    @Override
    public void onFinishedToTestDevices(boolean didSkipScreen) {
        didUserSkipDevicesScreen = didSkipScreen;

        handleScreenFinished(Enrollment.TAG_SETUP_STEP);
    }

    @Override
    public void onFinishedToRegisterFingerprint(boolean didSkipScreen) {
        didUserSkipFingerprintScreen = didSkipScreen;

        handleScreenFinished(Enrollment.OFFENDER_FINGER_ENROLLMENT_STEP);
    }

    @Override
    public void onFinishedToTestLocationVerification(boolean didSkipScreen) {
        didUserSkipLocationScreen = didSkipScreen;

        handleScreenFinished(Enrollment.LOCATION_VALIDATION_STEP);

    }

    @Override
    public void onFinishedToInstallKnoxVerification(boolean didSkipScreen) {
        didUserSkipKnoxScreen = didSkipScreen;

        handleScreenFinished(Enrollment.KNOX_SETUP_STEP);

    }

    private void handleScreenFinished(String screenName) {

        Fragment nextScreenFragment = getNextScreen(screenName);
        if (nextScreenFragment == null) {
            handleFinishedToDoEnrolment();
        } else {
            openNewScreen(nextScreenFragment);
        }

    }

    private void handleFinishedToDoEnrolment() {
        MagneticManager.getInstance().saveMagneticValue();

        openRelevantEvents();

        Intent mainActivityIntent = new Intent(MainActivity.MAIN_RECEIVER_EXTRA);
        mainActivityIntent.putExtra(MainActivity.MAIN_RECEIVER_EXTRA, ENROLMENT_DEVICES);
        LocalBroadcastManager.getInstance(this).sendBroadcast(mainActivityIntent);

        finish();
    }

    private void openRelevantEvents() {

        ArrayList<String> enrolmentScreensToShowArray = TableOffenderDetailsManager.sharedInstance().getEnrollmentScreensToShow();

        if (!didUserSkipDevicesScreen && enrolmentScreensToShowArray.contains(Enrollment.TAG_SETUP_STEP)) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.tag_beaconVerified, -1, -1);
        }

        if (!didUserSkipFingerprintScreen && enrolmentScreensToShowArray.contains(Enrollment.OFFENDER_FINGER_ENROLLMENT_STEP)) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.offenderFingerprintScanned, -1, -1);
        }

        if (!didUserSkipLocationScreen && enrolmentScreensToShowArray.contains(Enrollment.LOCATION_VALIDATION_STEP)) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceLocationVerified, -1, -1);
        }

        if (!didUserSkipKnoxScreen && enrolmentScreensToShowArray.contains(Enrollment.KNOX_SETUP_STEP)) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.knoxActivatedOnDevice, -1, -1);
        }

        if (!didUserSkipOffenderDetailsScreen && !didUserSkipDevicesScreen && !didUserSkipFingerprintScreen && !didUserSkipLocationScreen
                && !didUserSkipKnoxScreen) {

            if (!DatabaseAccess.getInstance().tableEventLog.isEventExistsInDB(EventTypes.offenderEnrolmentPerformed)) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.offenderEnrolmentPerformed, -1, -1);
            }
        }
    }

    public static Fragment getScreenToShow(String screen) {

        switch (screen) {

            case Enrollment.LOGIN_PASS_STEP:

            case Enrollment.OFFICER_FINGER_ENROLLMENT_STEP:

            case Enrollment.WI_FI_STEP:
                return null;

            case Enrollment.KNOX_SETUP_STEP:
                return KnoxInstallFragment.newInstance();

            case Enrollment.OFFENDER_DETAILS_STEP:
                return OffenderDetailsFragment.newInstance();

            case Enrollment.TAG_SETUP_STEP:
                return DevicesVerificationFragment.newInstance();

            case Enrollment.OFFENDER_FINGER_ENROLLMENT_STEP:
                return OffenderFingerPrintFragment.newInstance();

            case Enrollment.LOCATION_VALIDATION_STEP:
                return LocationVerificationFragment.newInstance();

        }

        return null;
    }

}

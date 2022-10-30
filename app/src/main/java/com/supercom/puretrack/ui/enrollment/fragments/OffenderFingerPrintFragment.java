package com.supercom.puretrack.ui.enrollment.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.enrollment.activity.EnrollmentActivity;
import com.supercom.puretrack.util.constants.Enrollment;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.local_managers.hardware.FingerprintManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.FingerprintManager.FingerPrintManagerListener;
import com.supercom.puretrack.ui.activity.BaseActivity;

import java.util.concurrent.TimeUnit;

public class OffenderFingerPrintFragment extends Fragment implements OnClickListener, FingerPrintManagerListener {

    private OffenderFingerPrintEnrollmentListener fingerPrintListener;
    private FingerprintManager fingerPrintManager;
    private View view;
    private boolean isInKioskModeWhileScreenFirstCreated;

    public static OffenderFingerPrintFragment newInstance() {
        return new OffenderFingerPrintFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fingerPrintManager = new FingerprintManager(getActivity(), this);

        //a patch in M to fix a problem while finger print screen blocked by kiosk mode, so in this screen we will always be in officer mode until user prext next/finish
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && KnoxUtil.getInstance().getKnoxSDKImplementation().isInKioskMode()) {
            isInKioskModeWhileScreenFirstCreated = true;
            KnoxUtil.getInstance().enterOfficerMode(false);
        }
    }

    public interface OffenderFingerPrintEnrollmentListener {
        void onFinishedToRegisterFingerprint(boolean didSkipScreen);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OffenderFingerPrintEnrollmentListener) {
            fingerPrintListener = (OffenderFingerPrintEnrollmentListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement IEnrolmentScreensListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.enrollment_finger_print_main, container, false);

        View fingerPrintTitleTxt = view.findViewById(R.id.fingerprin_title_text);

        String firstName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_FIRST_NAME);
        String lastName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_LAST_NAME);
        String offenderName = String.format("%s %s", firstName, lastName);

        TextView firstTitleText = fingerPrintTitleTxt.findViewById(R.id.first_title);
        firstTitleText.setText(R.string.enrolment_text_fingerprint_title);

        TextView secondTitleTextView = view.findViewById(R.id.second_title);
        secondTitleTextView.setText(offenderName);

        TextView startEnrolmentTextInstruction = view.findViewById(R.id.fingerprint_start_enrolment_text);
        startEnrolmentTextInstruction.setText(getString(R.string.enrolment_text_fingerprint_register, offenderName));

        Button registerFingerprintButton = view.findViewById(R.id.register_fingerprint_button);
        registerFingerprintButton.setOnClickListener(this);

        Button leftButton = view.findViewById(R.id.left_button);
        leftButton.setOnClickListener(this);

        Button rightButton = view.findViewById(R.id.right_button);
        rightButton.setOnClickListener(this);

        handleRegisteredFingerprint();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.register_fingerprint_button:

                BaseActivity.isCameFromRegularActivityCode = true;
                //fingerPrintManager.registerFingerPrint();

                break;

            case R.id.left_button:

                enterOffenderModeIfNeeded();

                getActivity().onBackPressed();
                break;

            case R.id.right_button:

                enterOffenderModeIfNeeded();

                String messageToUpload = "";
                int status = (int) v.getTag();
                switch (status) {

                    case EnrollmentActivity.FINISHED:
                        messageToUpload = "Finished fingerprint enrollment";
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                        fingerPrintListener.onFinishedToRegisterFingerprint(false);

                        break;

                    case EnrollmentActivity.SKIPPED:
                        messageToUpload = "Skipped fingerprint enrollment";
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                        fingerPrintListener.onFinishedToRegisterFingerprint(true);

                        break;

                    default:
                        break;

                }

                break;

        }

    }

    public static void enterOffenderModeIfNeededX() {
        KnoxUtil.getInstance().enterOffenderMode(true);
    }

    private void enterOffenderModeIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isInKioskModeWhileScreenFirstCreated) {
            KnoxUtil.getInstance().enterOffenderMode(false);
        }
    }

    @Override
    public void onFinshedToConductFingerPrint(int notificationId) { }

    @Override
    public void onFinshedToRegisterFingerPrint() {
        handleRegisteredFingerprint();
    }

    private void handleRegisteredFingerprint() {
        Button rightButton = view.findViewById(R.id.right_button);
       // if (fingerPrintManager.hasRegisterFingerPrint()) {
       //     rightButton.setTag(EnrollmentActivity.FINISHED);
       //     if (((EnrollmentActivity) getActivity()).getNextScreen(Enrollment.OFFENDER_FINGER_ENROLLMENT_STEP) == null) {
       //         rightButton.setText(R.string.enrolment_button_finish);
       //     } else {
       //         rightButton.setText(R.string.enrolment_button_next);
       //     }
       // } else {
       //     rightButton.setTag(EnrollmentActivity.SKIPPED);
       //     rightButton.setText(R.string.enrolment_button_skip);
       // }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //fingerPrintManager.unregisterBroadcastReceiver();
    }

}

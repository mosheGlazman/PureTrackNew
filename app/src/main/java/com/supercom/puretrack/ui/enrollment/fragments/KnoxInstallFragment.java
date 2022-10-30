package com.supercom.puretrack.ui.enrollment.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.enrollment.activity.EnrollmentActivity;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.broadcast_receiver.KnoxAdminReceiver;
import com.supercom.puretrack.util.constants.Enrollment;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.general.KnoxUtil.KnoxUtilityListener;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.ui.activity.BaseActivity;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.util.concurrent.TimeUnit;

public class KnoxInstallFragment extends Fragment implements OnClickListener, KnoxUtilityListener {

    public static final String TAG = "LocationVerificationFragment";

    private View view;
    private KnoxInstallEnrollmentListener knoxInstallEnrollmentListener;
    private AnimationDrawable knoxStatusAnimation;

    public static KnoxInstallFragment newInstance() {
        return new KnoxInstallFragment();
    }

    public interface KnoxInstallEnrollmentListener {
        void onFinishedToInstallKnoxVerification(boolean didSkipScreen);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof KnoxInstallEnrollmentListener) {
            knoxInstallEnrollmentListener = (KnoxInstallEnrollmentListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement KnoxInstallListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.enrollment_knox_install_verification_main, container, false);

        String firstName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_FIRST_NAME);
        String lastName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_LAST_NAME);

        View topLayout = view.findViewById(R.id.top_layout);

        TextView firstTitleTextView = (TextView) topLayout.findViewById(R.id.first_title);
        firstTitleTextView.setText(R.string.enrolment_text_knox_install_is_active_title_type);

        TextView secondTitleTextView = (TextView) topLayout.findViewById(R.id.second_title);
        secondTitleTextView.setText(String.format("%s %s", firstName, lastName));

        Button leftButton = (Button) view.findViewById(R.id.left_button);
        leftButton.setOnClickListener(this);

        handleKnoxStatus();

        return view;
    }

    private void handleKnoxStatus() {
        boolean isKnoxLicenceActivated = PureTrackSharedPreferences.isKnoxLicenceActivated();
        if (isKnoxLicenceActivated) {
            handleKnoxActivated();
        } else {
            handleKnoxNotActivated();
        }
    }

    private void handleKnoxActivated() {
        TextView knoxInstallTitleTextView = (TextView) view.findViewById(R.id.knox_install_title_text);
        knoxInstallTitleTextView.setTextColor(getResources().getColor(R.color.green));
        knoxInstallTitleTextView.setText(R.string.enrolment_text_knox_title_detected);

        TextView knoxInstallStatusTextView = (TextView) view.findViewById(R.id.knox_install_status_text);
        knoxInstallStatusTextView.setTextColor(getResources().getColor(R.color.grey_light));
        knoxInstallStatusTextView.setText(R.string.enrolment_text_knox_install_status_detected_active);

        Button rightButton = (Button) view.findViewById(R.id.right_button);
        rightButton.setOnClickListener(this);
        rightButton.setTag(EnrollmentActivity.FINISHED);

        if (((EnrollmentActivity) getActivity()).getNextScreen(Enrollment.KNOX_SETUP_STEP) == null) {
            rightButton.setText(R.string.enrolment_button_finish);
        } else {
            rightButton.setText(R.string.enrolment_button_next);
        }

        Button activateButtonButton = (Button) view.findViewById(R.id.knox_install_activate_button);
        activateButtonButton.setVisibility(View.GONE);

        ImageView statusImage = (ImageView) view.findViewById(R.id.knox_install_status_image);
        statusImage.setImageResource(R.drawable.ico_enrillment_knox_ok);
    }

    private void handleKnoxNotActivated() {
        TextView knoxInstallTitleTextView = (TextView) view.findViewById(R.id.knox_install_title_text);
        knoxInstallTitleTextView.setTextColor(getResources().getColor(R.color.enrollment_device_error_color));
        knoxInstallTitleTextView.setText(R.string.enrolment_text_knox_title_inactive);

        TextView knoxInstallStatusTextView = (TextView) view.findViewById(R.id.knox_install_status_text);
        knoxInstallStatusTextView.setTextColor(getResources().getColor(R.color.grey_light));
        knoxInstallStatusTextView.setText(R.string.enrolment_text_knox_install_status_detected_not_active);

        Button rightButton = (Button) view.findViewById(R.id.right_button);
        rightButton.setOnClickListener(this);
        rightButton.setTag(EnrollmentActivity.SKIPPED);

        ImageView statusImage = (ImageView) view.findViewById(R.id.knox_install_status_image);
        statusImage.setImageResource(R.drawable.ico_enrillment_knox_error);

        Button activateButtonButton = (Button) view.findViewById(R.id.knox_install_activate_button);
        activateButtonButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // back button
            case R.id.left_button:
                getActivity().onBackPressed();
                break;

            // skip/finished button
            case R.id.right_button:

                String messageToUpload = "";
                int status = (int) v.getTag();
                switch (status) {

                    case EnrollmentActivity.FINISHED:
                        messageToUpload = "Finished location enrollment";
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                        knoxInstallEnrollmentListener.onFinishedToInstallKnoxVerification(false);

                        break;

                    case EnrollmentActivity.SKIPPED:
                        messageToUpload = "Skipped location enrollment";
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                        knoxInstallEnrollmentListener.onFinishedToInstallKnoxVerification(true);

                        break;

                    default:
                        break;

                }

                break;

            case R.id.knox_install_activate_button:
                KnoxUtil.getInstance().setknoxUtilityListener(this);
                KnoxUtil.getInstance().runKnoxIfNeeded(getActivity());
                break;

        }
    }

    @Override
    public void onDeviceAdminShouldInstalled() {
        if (KnoxUtil.getInstance().mDeviceAdmin == null)
            KnoxUtil.getInstance().mDeviceAdmin = new ComponentName(getActivity(), KnoxAdminReceiver.class);

        // This activity asks the user to grant device administrator rights to the app.
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, KnoxUtil.getInstance().mDeviceAdmin);
        startActivityForResult(intent, KnoxUtil.DEVICE_ADMIN_ADD_RESULT_ENABLE);
        BaseActivity.isCameFromRegularActivityCode = true;

    }

    @Override
    public void onStartedActivateKnox() {

        TextView knoxInstallTitleTextView = (TextView) view.findViewById(R.id.knox_install_title_text);
        knoxInstallTitleTextView.setTextColor(getResources().getColor(R.color.grey_darker));
        knoxInstallTitleTextView.setText(R.string.enrolment_text_knox_title_activating);

        TextView knoxInstallStatusTextView = (TextView) view.findViewById(R.id.knox_install_status_text);
        knoxInstallStatusTextView.setTextColor(getResources().getColor(R.color.grey_darker));
        knoxInstallStatusTextView.setText(R.string.enrolment_button_knox_install_proccess_time);

        ImageView statusImage = (ImageView) view.findViewById(R.id.knox_install_status_image);
        statusImage.setImageResource(R.drawable.enrollment_knox_install_animation);
        knoxStatusAnimation = (AnimationDrawable) statusImage.getDrawable();
        knoxStatusAnimation.start();

        Button activateButtonButton = (Button) view.findViewById(R.id.knox_install_activate_button);
        activateButtonButton.setEnabled(false);
    }

    @Override
    public void onSucceededToActivateKnox() {
        TextView knoxInstallTitleTextView = (TextView) view.findViewById(R.id.knox_install_title_text);
        knoxInstallTitleTextView.setTextColor(getResources().getColor(R.color.green));
        knoxInstallTitleTextView.setText(R.string.enrolment_text_knox_title_activated);

        TextView knoxInstallStatusTextView = (TextView) view.findViewById(R.id.knox_install_status_text);
        knoxInstallStatusTextView.setTextColor(getResources().getColor(R.color.grey_light));
        knoxInstallStatusTextView.setText(R.string.enrolment_text_knox_install_status_succeeded);

        ImageView statusImage = (ImageView) view.findViewById(R.id.knox_install_status_image);
        statusImage.setImageResource(R.drawable.enrollment_knox_install_animation);
        knoxStatusAnimation = (AnimationDrawable) statusImage.getDrawable();
        knoxStatusAnimation.stop();

        statusImage.setImageResource(R.drawable.ico_enrillment_knox_ok);

        Button rightButton = (Button) view.findViewById(R.id.right_button);
        rightButton.setOnClickListener(this);
        rightButton.setTag(EnrollmentActivity.FINISHED);

        if (((EnrollmentActivity) getActivity()).getNextScreen(Enrollment.KNOX_SETUP_STEP) == null) {
            rightButton.setText(R.string.enrolment_button_finish);
        } else {
            rightButton.setText(R.string.enrolment_button_next);
        }

        Button activateButtonButton = (Button) view.findViewById(R.id.knox_install_activate_button);
        activateButtonButton.setVisibility(View.GONE);

    }

    @Override
    public void onFailedToActivateKnox() {
        if (isAdded()) {
            TextView knoxInstallTitleTextView = (TextView) view.findViewById(R.id.knox_install_title_text);
            knoxInstallTitleTextView.setTextColor(getResources().getColor(R.color.enrollment_device_error_color));
            knoxInstallTitleTextView.setText(R.string.enrolment_text_knox_title_failed);

            TextView knoxInstallStatusTextView = (TextView) view.findViewById(R.id.knox_install_status_text);
            knoxInstallStatusTextView.setTextColor(getResources().getColor(R.color.grey_light));
            knoxInstallStatusTextView.setText(R.string.enrolment_text_knox_install_status_failed);

            ImageView statusImage = (ImageView) view.findViewById(R.id.knox_install_status_image);
            statusImage.setImageResource(R.drawable.enrollment_knox_install_animation);
            knoxStatusAnimation = (AnimationDrawable) statusImage.getDrawable();
            knoxStatusAnimation.stop();

            statusImage.setImageResource(R.drawable.ico_enrillment_knox_error);

            Button activateButtonButton = (Button) view.findViewById(R.id.knox_install_activate_button);
            activateButtonButton.setEnabled(true);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case KnoxUtil.DEVICE_ADMIN_ADD_RESULT_ENABLE:

                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        break;
                    case Activity.RESULT_OK:
                        App.writeToNetworkLogsAndDebugInfo(KnoxUtil.class.getSimpleName(), "Device administrator activated", DebugInfoModuleId.Knox);
                        KnoxUtil.getInstance().activateKnoxLicence();
                        break;
                }

                break;


            default:
                break;
        }
    }

}

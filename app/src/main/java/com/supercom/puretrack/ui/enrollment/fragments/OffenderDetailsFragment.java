package com.supercom.puretrack.ui.enrollment.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;

import java.util.concurrent.TimeUnit;

public class OffenderDetailsFragment extends Fragment implements OnClickListener {

    private OffenderDetailsEnrollmentListener offenderDetailsEnrollmentListener;

    TextView first_title;
    TextView second_title;
    TextView tvOffenderSN;
    ImageView ivOffenderPicture;
    TextView tvOffenderName;
    TextView tvPrimaryPhoneNumber;
    TextView tvSecondaryPhoneNumber;
    TextView tvHomeAddress;
    TextView tvAgency;
    TextView tvOfficerName;
    Button leftButtonBack;
    Button rightButtonNext;


    public static OffenderDetailsFragment newInstance() {
        return new OffenderDetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface OffenderDetailsEnrollmentListener {
        void onFinishedOffenderDetailsPreview(boolean didSkipScreen);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OffenderDetailsEnrollmentListener) {
            offenderDetailsEnrollmentListener = (OffenderDetailsEnrollmentListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement IOffenderDetailsEnrolmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.enrollment_offender_details_fragment, container, false);
        bindViews(view);
        return view;
    }

    public void bindViews(View view) {
        first_title = view.findViewById(R.id.first_title);
        second_title = view.findViewById(R.id.second_title);

        tvOffenderSN = view.findViewById(R.id.tvOffenderSN);
        ivOffenderPicture = view.findViewById(R.id.ivOffenderPicture);
        tvOffenderName = view.findViewById(R.id.tvOffenderName);
        tvPrimaryPhoneNumber = view.findViewById(R.id.tvPrimaryPhoneNumber);
        tvSecondaryPhoneNumber = view.findViewById(R.id.tvAlternativePhoneNumber);
        tvHomeAddress = view.findViewById(R.id.tvHomeAddress);
        tvAgency = view.findViewById(R.id.tvAgency);
        tvOfficerName = view.findViewById(R.id.tvOfficer);

        leftButtonBack = view.findViewById(R.id.left_button);
        leftButtonBack.setOnClickListener(this);

        rightButtonNext = view.findViewById(R.id.right_button);
        rightButtonNext.setOnClickListener(this);

        bindUiState();
    }

    public void bindUiState() {
        first_title.setText(R.string.enrollment_text_title_offenderDetails);
        tvOffenderSN.setText(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_SN));

        String picPath = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_PICTURE_PATH);
        Glide.with(getActivity())
                .load(picPath)
                .placeholder(R.drawable.img_no_photo)
                .error(R.drawable.img_no_photo)
                .into(ivOffenderPicture);

        String firstName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_FIRST_NAME);
        String lastName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_LAST_NAME);
        second_title.setText(firstName + " " + lastName);
        tvOffenderName.setText(firstName + " " + lastName);
        tvPrimaryPhoneNumber.setText(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_PRIMARY_PHONE));
        tvSecondaryPhoneNumber.setText(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_SECONDARY_PHONE));
        tvHomeAddress.setText(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_ADDRESS));

        tvAgency.setText(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_AGENCY_NAME));
        tvOfficerName.setText(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_OFFICER_NAME));
        rightButtonNext.setText(R.string.enrolment_button_next);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.left_button:
                getActivity().onBackPressed();
                break;

            case R.id.right_button:
                String messageToUpload = "Finished Offender Details enrollment";
                TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                        DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
                offenderDetailsEnrollmentListener.onFinishedOffenderDetailsPreview(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

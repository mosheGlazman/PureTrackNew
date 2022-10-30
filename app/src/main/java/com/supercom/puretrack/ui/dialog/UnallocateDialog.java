package com.supercom.puretrack.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.R;

public class UnallocateDialog extends BaseDialog {
    private final UnallocateDialogListener unallocateDialogListener;

    public interface UnallocateDialogListener {
        void onPressedContinueToCutStrap();

        void onPressedCancelToCutStrap();

        void onStartTagTurnOffProcess();
    }

    enum ScreenType {
        TurnedOffSuccessfulScreen,
        ProcessingScreen,
        ConfirmationScreen,
        NoticeScreen,
        TurnedOffErrorScreen,
    }

    private ScreenType screenType = ScreenType.NoticeScreen;

    public UnallocateDialog(Context context, UnallocateDialogListener unallocateDialogListener) {
        super(context);
        this.unallocateDialogListener = unallocateDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View bodyRlLayout = findViewById(R.id.body_rl);
        bodyRlLayout.setAlpha((float) 0.8);

        TextView bodyTitleText = findViewById(R.id.body_title_text);
        bodyTitleText.setVisibility(View.INVISIBLE);

        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (screenType) {
                    case NoticeScreen:
                        unallocateDialogListener.onPressedContinueToCutStrap();
                        break;
                    case ConfirmationScreen:
                    case TurnedOffErrorScreen:
                        unallocateDialogListener.onStartTagTurnOffProcess();
                        break;
                    default:
                        dismiss();
                }
            }
        });
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (screenType) {
                    case NoticeScreen:
                    case ConfirmationScreen:
                        unallocateDialogListener.onPressedCancelToCutStrap();
                    default:
                        dismiss();
                }
            }
        });
    }

    public ScreenType getScreenType() {
        return screenType;
    }

    @Override
    protected boolean isShowingSwipeToAcknowledge() {
        return false;
    }

    public void setTurnedOffSuccessScreen() {
        screenType = ScreenType.TurnedOffSuccessfulScreen;
        setUnallocatedDialogUI(R.drawable.ico_tag_open_center,
                context.getString(R.string.dialog_unallocate_conformation_title),
                context.getString(R.string.dialog_unallocate_tag_turned_off_success_body),
                context.getString(R.string.btn_ok),
                View.VISIBLE,
                true,
                Color.parseColor("#FFFFFF"),
                context.getString(R.string.dialog_unallocate_btn_cancel),
                View.GONE,
                true,
                Color.parseColor("#FFFFFF"));
    }

    public void setProcessingScreen() {
        screenType = ScreenType.ProcessingScreen;
        setUnallocatedDialogUI(R.drawable.ico_tag_open_center,
                context.getString(R.string.dialog_unallocate_processing_title),
                context.getString(R.string.dialog_unallocate_processing_body),
                context.getString(R.string.dialog_unallocate_btn_turn_tag_off),
                View.VISIBLE,
                false,
                context.getResources().getColor(R.color.WhiteSmoke),
                context.getString(R.string.dialog_unallocate_btn_cancel),
                View.VISIBLE,
                false,
                context.getResources().getColor(R.color.WhiteSmoke));
    }

    public void setConfirmationScreen() {
        screenType = ScreenType.ConfirmationScreen;
        String tagId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ID);
        setUnallocatedDialogUI(R.drawable.ico_tag_open_center,
                context.getString(R.string.dialog_unallocate_conformation_title),
                context.getString(R.string.dialog_unallocate_confirmation_body, tagId),
                context.getString(R.string.dialog_unallocate_btn_turn_tag_off),
                View.VISIBLE,
                true,
                Color.parseColor("#FFFFFF"),
                context.getString(R.string.dialog_unallocate_btn_cancel),
                View.VISIBLE,
                true,
                Color.parseColor("#FFFFFF"));
    }

    public void setNoticeScreen() {
        screenType = ScreenType.NoticeScreen;
        setUnallocatedDialogUI(R.drawable.ico_tag_center,
                context.getString(R.string.dialog_unallocate_notice_title),
                context.getString(R.string.dialog_unallocate_notice_stap_close_body),
                context.getString(R.string.dialog_unallocate_btn_continue),
                View.VISIBLE,
                false,
                Color.parseColor("#FFFFFF"),
                context.getString(R.string.dialog_unallocate_btn_cancel),
                View.VISIBLE,
                true,
                Color.parseColor("#FFFFFF"));
    }

    public void setTurnedOffErrorScreen() {
        screenType = ScreenType.TurnedOffErrorScreen;
        setUnallocatedDialogUI(R.drawable.ico_tag_open_center,
                context.getString(R.string.dialog_unallocate_conformation_title),
                context.getString(R.string.dialog_unallocate_tag_turned_off_error_body),
                context.getString(R.string.dialog_unallocate_btn_retry),
                View.VISIBLE,
                true,
                Color.parseColor("#FFFFFF"),
                context.getString(R.string.dialog_unallocate_btn_cancel),
                View.VISIBLE,
                true,
                Color.parseColor("#FFFFFF"));
    }

    private void setUnallocatedDialogUI(int bodyImageRes, String titleTextRes, String bodyTextRes, String bOkTextRes,
                                        int bOkVisibility, boolean bOkEnabled, int bOkTextColor, String bCancelTextRes, int bCancelVisibility, boolean bCancelEnabled, int bCancelTextColor) {
        TextView titleText = findViewById(R.id.title_text);
        ImageView bodyImage = findViewById(R.id.body_image);
        TextView bodyText = findViewById(R.id.body_text);
        bodyImage.setImageResource(bodyImageRes);
        titleText.setText(titleTextRes);
        bodyText.setText(bodyTextRes);
        bOk.setText(bOkTextRes);
        bOk.setVisibility(bOkVisibility);
        bOk.setEnabled(bOkEnabled);
        bOk.setTextColor(bOkTextColor);
        bCancel.setText(bCancelTextRes);
        bCancel.setVisibility(bCancelVisibility);
        bCancel.setEnabled(bCancelEnabled);
        bCancel.setTextColor(bCancelTextColor);
    }

    public void updateNoticeScreen(int bodyImageResource, int bodyTextResource, boolean isOkButtonEnable) {
        ImageView bodyImage = findViewById(R.id.body_image);
        TextView bodyText = findViewById(R.id.body_text);
        bodyImage.setImageResource(bodyImageResource);
        bodyText.setText(context.getString(bodyTextResource));
        bOk.setEnabled(isOkButtonEnable);
    }
}

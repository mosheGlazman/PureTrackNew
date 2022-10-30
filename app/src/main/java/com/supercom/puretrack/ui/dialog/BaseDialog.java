package com.supercom.puretrack.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.util.date.DateFormatterUtil;
import com.supercom.puretrack.util.date.TimeUtil;

public class BaseDialog extends Dialog {

    protected Context context;
    protected Button bOk;
    protected Button bCancel;

    public BaseDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_message_received);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        RelativeLayout body_rl = findViewById(R.id.body_rl);
        if (getBodyBgColor() != -1) body_rl.setBackgroundResource(getBodyBgColor());

        RelativeLayout swipeToAck_rl = findViewById(R.id.swipeToAck_rl);
        RelativeLayout rlOk = findViewById(R.id.rlOk);
        bOk = findViewById(R.id.bOk);
        bCancel = findViewById(R.id.bCancel);
        bOk.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOkButtonPressed();
            }

        });
        bCancel.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView bodyTitleText = findViewById(R.id.body_title_text);
        if (isShowingSwipeToAcknowledge()) {
            final TextView seekBarText = findViewById(R.id.seekbar_text);
            seekBarText.setText(getSeekBarText());

            SeekBar seekBarDialog = findViewById(R.id.seekBar);
            seekBarDialog.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekBar.setProgress(0);
                    seekBarText.setText(getSeekBarText());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    seekBarText.setText("");
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        handleOnDialogProgressFinished();
                    }
                }
            });

            swipeToAck_rl.setVisibility(View.VISIBLE);
            rlOk.setVisibility(View.GONE);
        } else {
            swipeToAck_rl.setVisibility(View.GONE);
            rlOk.setVisibility(View.VISIBLE);
            if (isBtnCancelVisible()) {
                bCancel.setVisibility(View.VISIBLE);
                bodyTitleText.setVisibility(View.GONE);
            } else {
                bCancel.setVisibility(View.GONE);
                bodyTitleText.setVisibility(View.VISIBLE);
                int currentTimeFormatByDeviceSettings = TimeUtil.getCurrentTimeFormatByDeviceSettings(context);
                if (currentTimeFormatByDeviceSettings == 12) { // am pm
                    bodyTitleText.setText(TimeUtil.getCurrentTimeInAmPm() + " " + TimeUtil.getCurrentAmOrPmString());
                } else { //24
                    bodyTitleText.setText(TimeUtil.formatFromDate(new java.util.Date(), DateFormatterUtil.HM));
                }
            }
            bOk.setEnabled(isBtnOkEnabled());
            if (isBtnOkEnabled()) {
                bOk.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                bOk.setTextColor(context.getResources().getColor(R.color.WhiteSmoke));
            }
            bCancel.setEnabled(isBtnCancelEnabled());
            if (isBtnOkEnabled()) {
                bCancel.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                bCancel.setTextColor(context.getResources().getColor(R.color.WhiteSmoke));
            }
            if (isBtnOkVisible())
                bOk.setVisibility(View.VISIBLE);
            else {
                bOk.setVisibility(View.GONE);
            }
            if (getBtnOkResId() != -1) bOk.setText(getBtnOkResId());
            if (getBtnCancelResId() != -1) bCancel.setText(getBtnCancelResId());
        }

        TextView titleText = findViewById(R.id.title_text);
        if (getTitleText() != -1) titleText.setText(getTitleText());

        ImageView bodyImage = findViewById(R.id.body_image);
        if (getBodyImage() != -1) {
            bodyImage.setImageResource(getBodyImage());
        } else {
            bodyImage.setVisibility(View.GONE);
        }

        TextView bodyText = findViewById(R.id.body_text);
        bodyText.setText(getBodyText());
        if (getBodyTextColor() != -1) bodyText.setTextColor(getBodyTextColor());
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    protected void handleOnDialogProgressFinished() { }

    protected void handleOkButtonPressed() {
        dismiss();
    }

    protected int getTitleText() {
        return -1;
    }

    protected String getBodyText() {
        return "";
    }

    protected int getBodyTextColor() {
        return -1;
    }

    protected String getSeekBarText() {
        return "";
    }

    protected int getBodyImage() {
        return -1;
    }

    protected int getBodyBgColor() {
        return -1;
    }

    protected boolean isShowingSwipeToAcknowledge() {
        return true;
    }

    protected boolean isBtnCancelVisible() {
        return false;
    }

    protected boolean isBtnOkVisible() {
        return true;
    }

    protected boolean isBtnOkEnabled() {
        return true;
    }

    protected boolean isBtnCancelEnabled() {
        return true;
    }

    protected int getBtnOkResId() {
        return -1;
    }

    protected int getBtnCancelResId() {
        return -1;
    }
}

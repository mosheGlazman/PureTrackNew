package com.supercom.puretrack.ui.dialog;

import android.content.Context;

import com.supercom.puretrack.data.R;


public class FingerPrintDialog extends BaseDialog {

    private final FingerPrintDialogListener fingerPrintDialogListener;

    public interface FingerPrintDialogListener {
        void onFingerPrintDialogSwipe();
    }

    public FingerPrintDialog(Context context, FingerPrintDialogListener fingerPrintDialogListener) {
        super(context);
        this.fingerPrintDialogListener = fingerPrintDialogListener;
    }

    @Override
    protected void handleOnDialogProgressFinished() {
        fingerPrintDialogListener.onFingerPrintDialogSwipe();
    }

    @Override
    protected int getTitleText() {
        return R.string.dialog_text_biometric_test;
    }

    @Override
    protected String getBodyText() {
        return context.getString(R.string.dialog_text_please_conduct_biometric_test);
    }

    @Override
    protected String getSeekBarText() {
        return context.getString(R.string.dialog_text_swipe_to_start_text);
    }

    @Override
    protected int getBodyImage() {
        return R.drawable.ico_bio;
    }

}

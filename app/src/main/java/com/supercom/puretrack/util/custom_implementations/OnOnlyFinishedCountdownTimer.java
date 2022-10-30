package com.supercom.puretrack.util.custom_implementations;

import android.os.CountDownTimer;

public abstract class OnOnlyFinishedCountdownTimer extends CountDownTimer {

    public OnOnlyFinishedCountdownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }
}
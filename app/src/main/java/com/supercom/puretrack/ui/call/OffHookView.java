package com.supercom.puretrack.ui.call;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.R;

import java.lang.reflect.Method;
import java.util.Locale;

public class OffHookView extends BaseCallView implements OnClickListener {

    private View view;
    private int time;
    private Thread thread;
    protected boolean isRunning = true;

    public interface ITelephony {

        boolean endCall();
    }

    protected void initView() {
        view = LayoutInflater.from(App.getContext()).inflate(R.layout.outgoing_callscreen, null);

        Button videoButton = view.findViewById(R.id.videoButton);
        videoButton.setVisibility(View.GONE);

        Button hangupButton = view.findViewById(R.id.hangupButton);
        hangupButton.setOnClickListener(this);

        Button keyboardButton = view.findViewById(R.id.keyboardButton);
        keyboardButton.setVisibility(View.GONE);

        TextView remoteUserTextView = view.findViewById(R.id.remoteUser);
        remoteUserTextView.setVisibility(View.GONE);

        final TextView callDuration = view.findViewById(R.id.callDuration);
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(1000);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            @Override
                            public void run() {
                                callDuration.setText(formatTimeSpan(time));
                                time += 1;
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();

    }

    private String formatTimeSpan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    @Override
    protected View getView() {
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.hangupButton) {
            endCall();
        }

    }

    private void endCall() {
        ITelephony telephonyService;
        TelephonyManager telephony = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            telephonyService = (ITelephony) m.invoke(telephony);
            telephonyService.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopDurationTimeTimer() {
        isRunning = false;
    }

}

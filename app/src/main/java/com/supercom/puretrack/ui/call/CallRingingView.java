package com.supercom.puretrack.ui.call;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.R;

import java.io.IOException;

public class CallRingingView extends BaseCallView {


    private View createRingingView;
    private final String incomingNumber;

    public CallRingingView(String incomingNumber) {
        this.incomingNumber = incomingNumber;
    }

    @Override
    protected void initView() {
        createRingingView = LayoutInflater.from(App.getContext()).inflate(R.layout.incoming_callscreen, null);
        SeekBar seekBar = (SeekBar) createRingingView.findViewById(R.id.seekBar);
        final TextView seekBarText = (TextView) createRingingView.findViewById(R.id.seekbar_text);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(0);
                seekBarText.setText(App.getContext().getString(R.string.incoming_call_slide_to_answer));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarText.setText("");
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 100) {
                    answerClicked();
                }
            }

        });

        createRingingView.findViewById(R.id.container1).setVisibility(View.GONE);

        ((TextView) createRingingView.findViewById(R.id.remoteUser)).setText(incomingNumber);
    }

    @Override
    protected View getView() {
        return createRingingView;
    }

    private void answerClicked() {
        try {
            Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK);
        } catch (IOException e) {
            // Runtime.exec(String) had an I/O problem, try to fall back
            String enforcedPerm = "android.permission.CALL_PRIVILEGED";
            Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(Intent.EXTRA_KEY_EVENT,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));


            App.getContext().sendOrderedBroadcast(btnDown, enforcedPerm);
        }

    }


}

package com.supercom.puretrack.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;


import com.supercom.puretrack.data.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("AppCompatCustomView")
public class ClockView extends FontFitTextView {
    SimpleDateFormat format = new SimpleDateFormat("mm:HH", Locale.getDefault());
    private int refreshMilli=59000;

   private Context context;

    public ClockView(@NonNull Context context) {
        super(context);
        this.context = context;
        init(null);
    }

    public ClockView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public ClockView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        try {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ClockView, 0, 0);
            try {
               String _format = a.getString(R.styleable.ClockView_Format);
              int _refreshMilli = a.getInt(R.styleable.ClockView_RefreshMilli,0);

              if(_format!=null && _format.length()>0){
                  format= new SimpleDateFormat(_format, Locale.ENGLISH);
              }
                if(_refreshMilli>0){
                    refreshMilli=_refreshMilli;
                }
            } finally {
                a.recycle();
            }
        } catch (Exception ex) {
            Log.e("NetworkView", "init error", ex);
        }

        setText();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setText();
            }
        },0,refreshMilli);//Update text every second
    }

    private void setText() {
        setText(format.format(new Date()));
    }
}

package com.supercom.puretrack.ui.views.toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.ui.views.FontFitTextView;
import com.supercom.puretrack.ui.views.ToolbarViewsDataManager;
import com.supercom.puretrack.util.application.App;

import java.util.UUID;


@SuppressLint("AppCompatCustomView")
public class BatteryView extends LinearLayout  {
    private static final String TAG = "BatteryView";
    private final Context context;
    ImageView imageView;
    FontFitTextView textView;
    private int percent;
    private boolean charging;
    private boolean showNumbers;
    private int[] drawables;

    public BatteryView(@NonNull Context context) {
        super(context);
        this.context = context;
        init(null);
    }

    public BatteryView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public BatteryView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        drawables = new int[]{
                R.drawable.ic_baseline_battery_0_bar_24,
                R.drawable.ic_baseline_battery_1_bar_24,
                R.drawable.ic_baseline_battery_2_bar_24,
                R.drawable.ic_baseline_battery_3_bar_24,
                R.drawable.ic_baseline_battery_4_bar_24,
                R.drawable.ic_baseline_battery_5_bar_24,
                R.drawable.ic_baseline_battery_6_bar_24,
                R.drawable.ic_baseline_battery_7_bar_24,
                R.drawable.ic_baseline_battery_8_bar_24,
                R.drawable.ic_baseline_battery_9_bar_24,
                R.drawable.ic_baseline_battery_10_bar_24,
                R.drawable.ic_baseline_battery_11_bar_24,
                R.drawable.ic_baseline_battery_12_bar_24,
                R.drawable.ic_baseline_battery_13_bar_24,
                R.drawable.ic_baseline_battery_full_24
        };

        try {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BatteryView, 0, 0);
            try {
               percent = ToolbarViewsDataManager.getInstance(context).getLastBatteryPercentage();
               charging = ToolbarViewsDataManager.getInstance(context).isLastBatteryCharging();
                //percent = a.getInt(R.styleable.BatteryView_Percent, percent);
                //charging = a.getBoolean(R.styleable.BatteryView_Charging, charging);
                 showNumbers = a.getBoolean(R.styleable.BatteryView_ShowNumbers, false);
            } finally {
                a.recycle();
            }
        } catch (Exception ex) {
            Log.e(TAG, "init error", ex);
        }

        View layout = inflate(context, R.layout.battery_view, null);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(layout);

        imageView = layout.findViewById(R.id.imageView);
        textView = layout.findViewById(R.id.textView);
        textView.setVisibility(showNumbers ? VISIBLE : GONE);

        setData(percent,charging);
     }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

     }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        percent = ToolbarViewsDataManager.getInstance(context).getLastBatteryPercentage();
        charging = ToolbarViewsDataManager.getInstance(context).isLastBatteryCharging();
        setData(percent,charging);

        Log.i(TAG, "onAttachedToWindow");
     }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "onDetachedFromWindow");
    }

    public void setPercent(int percent) {
        Log.d(TAG, "Charging percentage: " + percent);
        this.percent = percent;
        setData(percent,charging);
    }

    public void setCharging(boolean charging) {
        Log.d(TAG, "is Charging: " + charging);
        this.charging = charging;
        setData(percent,charging);
    }

    private void setData(int percent,boolean charging) {
        float part = 100 / (float) (drawables.length-1);
        float imageLevel = (float) percent / part;
        int imageLevelVal = (int) (imageLevel == 0 ? 0 : imageLevel);

        Log.i(TAG, "percent: " + percent + " part:" + part +" Level:"+imageLevelVal);

        if (showNumbers) {
            textView.setText(percent + "%");

            if(charging){
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_battery_charging_full_24));
            }else{
                imageView.setImageDrawable(context.getDrawable(drawables[imageLevelVal]));
            }
        }else{
            imageView.setImageDrawable(context.getDrawable(drawables[imageLevelVal]));
            if (!charging) {
                imageView.setColorFilter(null);
            } else {
                imageView.setColorFilter(Color.rgb(255,255,140));
            }
        }
    }
}

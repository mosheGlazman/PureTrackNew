package com.supercom.puretrack.ui.views.toolbar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.ui.views.ToolbarViewsDataManager;


@SuppressLint("AppCompatCustomView")
public class CellularSignalView extends ImageView implements ToolbarViewsDataManager.OnCellularInfoListener {
    private final Context context;
    private int level;
    private boolean available;
    private int[] drawables;
    private BroadcastReceiver cellSignalReceiver;

    public CellularSignalView(@NonNull Context context) {
        super(context);
        this.context = context;
        init(null);
    }

    public CellularSignalView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public CellularSignalView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        drawables = new int[]{
                R.drawable.ic_baseline_signal_cellular_unavailable,
                R.drawable.ic_baseline_signal_cellular_1,
                R.drawable.ic_baseline_signal_cellular_2,
                R.drawable.ic_baseline_signal_cellular_3,
                R.drawable.ic_baseline_signal_cellular_4
        };

        try {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NetworkView, 0, 0);
            try {
                //level = a.getInt(R.styleable.NetworkView_Level, 0);
                level = ToolbarViewsDataManager.getInstance(context).getLastCellularSignalLevel();
                available = a.getBoolean(R.styleable.NetworkView_Available, false);
            } finally {
                a.recycle();
            }
        } catch (Exception ex) {
            Log.e("NetworkView", "init error", ex);
        }

        setData(level, available);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setData(level, available);
            }
        });

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i("CellularSignalView", "onAttachedToWindow");
        ToolbarViewsDataManager.getInstance(context).setCellularInfoListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i("CellularSignalView", "onDetachedFromWindow");
        ToolbarViewsDataManager.getInstance(context).removeCellularInfoListener(this);
    }

    public void setData(int level, boolean available) {
        this.level = level;
        setImageDrawable(context.getDrawable(drawables[level]));
    }

    @Override
    public void onCellularSignalStrengthSampled(int signalLevel) {
        setData(signalLevel, true);
    }
}

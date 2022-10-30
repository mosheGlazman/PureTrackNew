package com.supercom.puretrack.ui.views.toolbar;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.ui.views.FontFitTextView;
import com.supercom.puretrack.ui.views.ToolbarViewsDataManager;


@SuppressLint("AppCompatCustomView")
public class MobileDataView extends LinearLayout implements ToolbarViewsDataManager.OnMobileDataInfoListener {
    private final Context context;
    ImageView imageView;
    FontFitTextView textView;
    private boolean isAvailable;
    private String typeData;

    public MobileDataView(@NonNull Context context) {
        super(context);
        this.context = context;
        init(null);
    }

    public MobileDataView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public MobileDataView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        try {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MobileDataView, 0, 0);
            try {
/*              available = a.getBoolean(R.styleable.MobileDataView_AvailableData, false);*/
                isAvailable = ToolbarViewsDataManager.getInstance(context).isLastMobileDataOn();
                typeData = ToolbarViewsDataManager.getInstance(context).getLastMobileDataType();

            } finally {
                a.recycle();
            }
        } catch (Exception ex) {
            Log.e("MobileDataView", "init error", ex);
        }

        View layout = inflate(context, R.layout.mobile_data_view, null);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(layout);

        imageView = layout.findViewById(R.id.imageView);
        textView = layout.findViewById(R.id.textView);

        setData(isAvailable, typeData);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i("MobileDataView", "onAttachedToWindow");
        ToolbarViewsDataManager.getInstance(context).setMobileDataInfoListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i("MobileDataView", "onDetachedFromWindow");
        ToolbarViewsDataManager.getInstance(context).removeMobileDataInfoListener(this);
    }

    public void setData(boolean available, String typeData) {
        textView.setText(typeData);
        imageView.setImageDrawable(context.getDrawable(available ? R.drawable.ic_baseline_signal_data_enabled : R.drawable.ic_baseline_signal_data_disabled));
    }

    @Override
    public void onMobileDataConnectivityStatusSampled(String receptionType, boolean isOn) {
        setData(isOn, receptionType);
    }
}

package com.supercom.puretrack.ui.dialog;

import android.content.Context;
import android.graphics.Color;

import com.supercom.puretrack.data.R;

public class AcknowledgeWIthButtonDialog extends BaseDialog {
    private int imgResId = -1;
    private int titleResId = -1;
    private int infoMsgResId = -1;

    public AcknowledgeWIthButtonDialog(Context context, int imgResourceId, int titleResourseId, int infoMsgResorseId) {
        super(context);
        imgResId = imgResourceId;
        titleResId = titleResourseId;
        infoMsgResId = infoMsgResorseId;
    }

    @Override
    protected boolean isShowingSwipeToAcknowledge() {
        return false;
    }

    @Override
    protected int getBodyImage() {
        return imgResId;
    }

    @Override
    protected int getTitleText() {
        return titleResId;
    }

    @Override
    protected String getBodyText() {
        if (infoMsgResId != -1) {
            return context.getString(infoMsgResId);
        } else {
            return "";
        }
    }

    @Override
    protected int getBodyBgColor() {
        return R.color.white;
    }

    @Override
    protected int getBodyTextColor() {
        return Color.parseColor("#000000");
    }
}

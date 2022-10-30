package com.supercom.puretrack.ui.dialog;

import android.content.Context;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.model.business_logic_models.enums.ServerMessageType;

public class MessageDialog extends BaseDialog {

    private final MessageDialogListener messageDialogListener;
    private final String bodyText;
    private final ServerMessageType serverMessageType;
    private final int requestId;

    public interface MessageDialogListener {
        void onMessageDialogSwipeComplete(ServerMessageType serverMessageType, int requestId);
    }

    public MessageDialog(Context context, MessageDialogListener messageDialogListener, String titleText, ServerMessageType serverMessageType, int requestId) {
        super(context);
        this.messageDialogListener = messageDialogListener;
        this.bodyText = titleText;
        this.serverMessageType = serverMessageType;
        this.requestId = requestId;
    }


    @Override
    protected void handleOnDialogProgressFinished() {
        messageDialogListener.onMessageDialogSwipeComplete(serverMessageType, requestId);
        dismiss();
    }

    @Override
    protected int getTitleText() {
        if (serverMessageType == ServerMessageType.MESSAGE)
            return R.string.dialog_text_new_message;
        else
            return R.string.message_dialog_on_demand_photo_message;
    }

    @Override
    protected String getBodyText() {
        if (serverMessageType == ServerMessageType.MESSAGE)
            return bodyText;
        else
            return "";
    }

    @Override
    protected String getSeekBarText() {
        if (serverMessageType == ServerMessageType.MESSAGE)
            return context.getString(R.string.dialog_text_swipe_to_acknowledge_message);
        else
            return context.getString(R.string.message_dialog_swipe_to_take_a_photo);
    }

    @Override
    protected int getBodyImage() {
        return R.drawable.ico_msg;
    }

}

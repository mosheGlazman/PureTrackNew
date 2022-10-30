package com.supercom.puretrack.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.model.ui_models.Message;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.ArrayList;

public class MessagesAdapter extends ArrayAdapter<Message> {
    public MessagesAdapter(Context context, ArrayList<Message> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Message msg = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.msg, parent, false);
        }
        // Lookup view for data population
        TextView tvTextMsg = convertView.findViewById(R.id.tvMsg);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        ImageView ivAck = convertView.findViewById(R.id.ivAck);
        LinearLayout llMsg = convertView.findViewById(R.id.llMsg);
        if (msg.getMsgRead() == 1) {
            ivAck.setImageResource(R.drawable.btn_msg_acknowledge);
            ivAck.setVisibility(View.VISIBLE);
        }
        if (msg.getMsgType() == 1) {  // 0 - MSG from officer, 1- MSG from offender
            llMsg.setBackgroundColor(Color.WHITE);
        }

        // Populate the data into the template view using the data object
        tvTextMsg.setText(msg.getMsgText());
        tvDate.setText(TimeUtil.convertUTCToTimeInSecondsAddTZ(msg.getMsgTime()));
        // Return the completed view to render on screen
        return convertView;
    }

}

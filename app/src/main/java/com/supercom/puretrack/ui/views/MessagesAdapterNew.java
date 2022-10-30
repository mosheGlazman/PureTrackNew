package com.supercom.puretrack.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessagesAdapterNew extends ArrayAdapter<Message> {
    public MessagesAdapterNew(Context context, ArrayList<Message> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = getItem(position);
        boolean fromOfficer = msg.getMsgType() == 0;
        int layoutResId = fromOfficer ? R.layout.message_item_in_day : R.layout.message_item_out_day;
        boolean showDay = true;
        boolean showPoint = true;

        if (position > 0) {
            Message lastMsg = getItem(position - 1);
            if (msg.getDay() == lastMsg.getDay()) {
                showDay = false;
                layoutResId = fromOfficer ? R.layout.message_item_in : R.layout.message_item_out;
            }

            if(!showDay && msg.getMsgType() == lastMsg.getMsgType()) {
                if (!fromOfficer || msg.getMsgSender().equals(lastMsg.getMsgSender())) {
                    showPoint = false;
                }
            }
        }

        convertView = LayoutInflater.from(getContext()).inflate(layoutResId, parent, false);

        TextView tv_time = convertView.findViewById(R.id.tv_time);
        TextView tv_text = convertView.findViewById(R.id.tv_text);

        convertView.findViewById(R.id.v_point).setVisibility(showPoint ?View.VISIBLE:View.INVISIBLE);

        if(!showDay){
            convertView.setPadding(0, showPoint ? 22 : 0, 0, 0);
        }

        tv_text.setText(msg.getMsgText());
        tv_time.setText(getTime(new Date(msg.getMsgTime())));

        if (fromOfficer) {
            TextView tv_name = convertView.findViewById(R.id.tv_name);
            tv_name.setText(msg.getMsgSender());
        }
        if (showDay) {
            TextView tv_day = convertView.findViewById(R.id.tv_day);
            tv_day.setText(getDate(new Date(msg.getMsgTime())));
        }

        return convertView;
    }

    public static SimpleDateFormat format_dd_MM_yyyy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    public static SimpleDateFormat format_dd_MM_HH_mm = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
    public static SimpleDateFormat format_HH_mm_ss = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static String getTimeOrDateTime(Date date) {
        if (date == null) {
            return "";
        }

        String currentDate = getDate(getCurrentDate());
        String dDate = getDate(date);

        if (TextUtils.equals(currentDate, dDate)) {
            return format_HH_mm_ss.format(date);
        }

        return format_dd_MM_HH_mm.format(date);
    }

    public static String getTime(Date date) {
        if (date == null)
            return "";

        return format_HH_mm_ss.format(date);
    }

    public static String getDate(Date date) {
        if (date == null)
            return "";

        return format_dd_MM_yyyy.format(date);
    }

    public static Date getCurrentDate() {
        return new Date();
    }
}

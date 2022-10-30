package com.supercom.puretrack.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.supercom.puretrack.model.ui_models.LauncherApplicationModel;
import com.supercom.puretrack.data.R;

import java.util.List;

public class AppsListAdapter extends ArrayAdapter<LauncherApplicationModel> {

    private final Context context;
    private final int resource;
    private List<LauncherApplicationModel> appsList;

    public AppsListAdapter(Context context, int resource, List<LauncherApplicationModel> apps) {
        super(context, resource, apps);
        this.context = context;
        this.resource = resource;
        this.appsList = apps;
    }

    public int getCount() {
        return appsList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, null);
        }

        ImageView appIcon = convertView.findViewById(R.id.item_app_icon);
        appIcon.setImageDrawable(appsList.get(position).getIcon());

        TextView appLabel = convertView.findViewById(R.id.item_app_label);
        appLabel.setText(appsList.get(position).getLabel());

        TextView appName = convertView.findViewById(R.id.item_app_name);
        appName.setText(appsList.get(position).getName());

        return convertView;
    }

    public void setApps(List<LauncherApplicationModel> apps) {
        this.appsList = apps;
    }

}

package com.supercom.puretrack.model.ui_models;

import android.graphics.drawable.Drawable;

public class LauncherApplicationModel {

    private CharSequence label;
    private CharSequence name;
    private Drawable icon;

    public CharSequence getLabel() {
        return label;
    }

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

}
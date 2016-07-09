package com.dmplayer.models;


import android.graphics.drawable.Drawable;

public class DrawerItem {
    String title;
    Drawable icon;

    public DrawerItem(String title, Drawable icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getIcon() {
        return icon;
    }
}

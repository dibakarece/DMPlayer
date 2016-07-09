/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
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

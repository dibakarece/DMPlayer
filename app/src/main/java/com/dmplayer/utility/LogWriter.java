/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.utility;

import android.util.Log;


public class LogWriter {

    public static void debug(String TAG, String writeText) {
        Log.d(TAG, writeText);
    }

    public static void info(String TAG, String writeText) {
        Log.i(TAG, writeText);
    }
}

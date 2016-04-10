/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.phonemidea;

import java.util.Formatter;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.dmplayer.ApplicationDMPlayer;
import com.dmplayer.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class DMPlayerUtility {

    private static final String TAG = "MusicUtils";


    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static String makeAlbumsLabel(Context context, int numalbums, int numsongs, boolean isUnknown) {
        // There are two formats for the albums/songs information:
        // "N Song(s)" - used for unknown artist/album
        // "N Album(s)" - used for known albums

        StringBuilder songs_albums = new StringBuilder();

        Resources r = context.getResources();
        if (isUnknown) {
            if (numsongs == 1) {
                songs_albums.append(context.getString(R.string.onesong));
            } else {
                String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
                sFormatBuilder.setLength(0);
                sFormatter.format(f, Integer.valueOf(numsongs));
                songs_albums.append(sFormatBuilder);
            }
        } else {
            String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
            sFormatBuilder.setLength(0);
            sFormatter.format(f, Integer.valueOf(numalbums));
            songs_albums.append(sFormatBuilder);
            songs_albums.append(context.getString(R.string.albumsongseparator));
        }
        return songs_albums.toString();
    }

    private static String mLastSdStatus;

    public static void displayDatabaseError(Activity a) {
        if (a.isFinishing()) {
            // When switching tabs really fast, we can end up with a null
            // cursor (not sure why), which will bring us here.
            // Don't bother showing an error message in that case.
            return;
        }

        String status = Environment.getExternalStorageState();
        int title, message;

        if (Environment.isExternalStorageRemovable()) {
            title = R.string.sdcard_error_title;
            message = R.string.sdcard_error_message;
        } else {
            title = R.string.sdcard_error_title_nosdcard;
            message = R.string.sdcard_error_message_nosdcard;
        }

        if (status.equals(Environment.MEDIA_SHARED) || status.equals(Environment.MEDIA_UNMOUNTED)) {
            if (Environment.isExternalStorageRemovable()) {
                title = R.string.sdcard_busy_title;
                message = R.string.sdcard_busy_message;
            } else {
                title = R.string.sdcard_busy_title_nosdcard;
                message = R.string.sdcard_busy_message_nosdcard;
            }
        } else if (status.equals(Environment.MEDIA_REMOVED)) {
            if (Environment.isExternalStorageRemovable()) {
                title = R.string.sdcard_missing_title;
                message = R.string.sdcard_missing_message;
            } else {
                title = R.string.sdcard_missing_title_nosdcard;
                message = R.string.sdcard_missing_message_nosdcard;
            }
        } else if (status.equals(Environment.MEDIA_MOUNTED)) {
            // The card is mounted, but we didn't get a valid cursor.
            // This probably means the mediascanner hasn't started scanning the
            // card yet (there is a small window of time during boot where this
            // will happen).
            a.setTitle("");
            // Intent intent = new Intent();
            // intent.setClass(a, ScanningProgress.class);
            // a.startActivityForResult(intent, Defs.SCAN_DONE);
        } else if (!TextUtils.equals(mLastSdStatus, status)) {
            mLastSdStatus = status;
            Log.d(TAG, "sd card: " + status);
        }
    }


    public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
    }

    public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, int limit) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            if (limit > 0) {
                uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }

    }

    public static long[] getSongListForArtist(Context context, long id) {
        final String[] ccols = new String[]{MediaStore.Audio.Media._ID};
        String where = MediaStore.Audio.Media.ARTIST_ID + "=" + id + " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null, MediaStore.Audio.Media.ALBUM_KEY + ","
                + MediaStore.Audio.Media.TRACK);

        if (cursor != null) {
            long[] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }

    public static long[] getSongListForAlbum(Context context, long id) {
        final String[] ccols = new String[]{MediaStore.Audio.Media._ID};
        String where = MediaStore.Audio.Media.ALBUM_ID + "=" + id + " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null, MediaStore.Audio.Media.TRACK);

        if (cursor != null) {
            long[] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }

    private final static long[] sEmptyList = new long[0];

    public static long[] getSongListForCursor(Cursor cursor) {
        if (cursor == null) {
            return sEmptyList;
        }
        int len = cursor.getCount();
        long[] list = new long[len];
        cursor.moveToFirst();
        int colidx = -1;
        try {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch (IllegalArgumentException ex) {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getLong(colidx);
            cursor.moveToNext();
        }
        return list;
    }

    public static String getAudioDuration(long durationLong) {
        // long totalSecs = durationLong / 1000;
        long totalSecs = durationLong;
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        String duration = "";
        if (hours != 0) {
            duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            duration = String.format("%02d:%02d", minutes, seconds);
        }

        return duration;
    }


    //Theme set
    public static void settingTheme(Context context, int theme) {
        switch (theme) {
            case 1:
                context.setTheme(R.style.AppTheme);
                break;
            case 2:
                context.setTheme(R.style.AppTheme2);
                break;
            case 3:
                context.setTheme(R.style.AppTheme3);
                break;
            case 4:
                context.setTheme(R.style.AppTheme4);
                break;
            case 5:
                context.setTheme(R.style.AppTheme5);
                break;
            case 6:
                context.setTheme(R.style.AppTheme6);
                break;
            case 7:
                context.setTheme(R.style.AppTheme7);
                break;
            case 8:
                context.setTheme(R.style.AppTheme8);
                break;
            case 9:
                context.setTheme(R.style.AppTheme9);
                break;
            case 10:
                context.setTheme(R.style.AppTheme10);
                break;
            default:
                context.setTheme(R.style.AppTheme);
                break;
        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            ApplicationDMPlayer.applicationHandler.post(runnable);
        } else {
            ApplicationDMPlayer.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(ApplicationDMPlayer.density * value);
    }


    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    public static void animateHeartButton(final View v) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(v, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(v, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
        animatorSet.start();
    }


    public static void animatePhotoLike(final View vBgLike, final View ivLike) {
        vBgLike.setVisibility(View.VISIBLE);
        ivLike.setVisibility(View.VISIBLE);

        vBgLike.setScaleY(0.1f);
        vBgLike.setScaleX(0.1f);
        vBgLike.setAlpha(1f);
        ivLike.setScaleY(0.1f);
        ivLike.setScaleX(0.1f);

        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();

        android.animation.ObjectAnimator bgScaleYAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(200);
        bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        android.animation.ObjectAnimator bgScaleXAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(200);
        bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        android.animation.ObjectAnimator bgAlphaAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(200);
        bgAlphaAnim.setStartDelay(150);
        bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        android.animation.ObjectAnimator imgScaleUpYAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleY", 0.1f, 1f);
        imgScaleUpYAnim.setDuration(300);
        imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        android.animation.ObjectAnimator imgScaleUpXAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleX", 0.1f, 1f);
        imgScaleUpXAnim.setDuration(300);
        imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        android.animation.ObjectAnimator imgScaleDownYAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleY", 1f, 0f);
        imgScaleDownYAnim.setDuration(300);
        imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
        android.animation.ObjectAnimator imgScaleDownXAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleX", 1f, 0f);
        imgScaleDownXAnim.setDuration(300);
        imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                vBgLike.setVisibility(View.INVISIBLE);
                ivLike.setVisibility(View.INVISIBLE);
            }
        });
        animatorSet.start();
    }

}

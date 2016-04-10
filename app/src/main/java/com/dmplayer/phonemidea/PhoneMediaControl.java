/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.phonemidea;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.dmplayer.ApplicationDMPlayer;
import com.dmplayer.dbhandler.FavoritePlayTableHelper;
import com.dmplayer.dbhandler.MostAndRecentPlayTableHelper;
import com.dmplayer.manager.MediaController;
import com.dmplayer.models.SongDetail;

public class PhoneMediaControl {

    private Context context;
    private Cursor cursor = null;
    private static volatile PhoneMediaControl Instance = null;

    public static enum SonLoadFor {
        All, Gener, Artis, Album, Musicintent, MostPlay, Favorite, ResecntPlay
    }

    public static PhoneMediaControl getInstance() {
        PhoneMediaControl localInstance = Instance;
        if (localInstance == null) {
            synchronized (MediaController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new PhoneMediaControl();
                }
            }
        }
        return localInstance;
    }

    public void loadMusicList(final Context context, final long id, final SonLoadFor sonloadfor, final String path) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            ArrayList<SongDetail> songsList = null;

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    songsList = getList(context, id, sonloadfor, path);
                } catch (Exception e) {
                    closeCrs();
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (phonemediacontrolinterface != null) {
                    phonemediacontrolinterface.loadSongsComplete(songsList);
                }
            }
        };

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }


    public ArrayList<SongDetail> getList(final Context context, final long id, final SonLoadFor sonloadfor, final String path) {
        ArrayList<SongDetail> songsList = new ArrayList<>();
        String sortOrder = "";
        switch (sonloadfor) {
            case All:
                String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
//                sortOrder = MediaStore.Audio.Media.DATE_ADDED + " desc";
                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
                cursor = ((Activity) context).getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projectionSongs, selection, null, sortOrder);
                songsList = getSongsFromCursor(cursor);
                break;

            case Gener:
                Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", id);
                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
                cursor = ((Activity) context).getContentResolver().query(uri, projectionSongs, null, null, null);
                songsList = getSongsFromCursor(cursor);
                break;

            case Artis:
                String where = MediaStore.Audio.Media.ARTIST_ID + "=" + id + " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";
                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
                cursor = ((Activity) context).getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projectionSongs, where, null, sortOrder);
                songsList = getSongsFromCursor(cursor);
                break;

            case Album:
                String wherecls = MediaStore.Audio.Media.ALBUM_ID + "=" + id + " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";
                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
                cursor = ((Activity) context).getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projectionSongs, wherecls, null, sortOrder);
                songsList = getSongsFromCursor(cursor);
                break;

            case Musicintent:
                String condition = MediaStore.Audio.Media.DATA + "='" + path + "' AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";
                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
                cursor = ((Activity) context).getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projectionSongs, condition, null, sortOrder);
                songsList = getSongsFromCursor(cursor);
                break;

            case MostPlay:
                cursor = MostAndRecentPlayTableHelper.getInstance(context).getMostPlay();
                songsList = getSongsFromSQLDBCursor(cursor);
                break;

            case Favorite:
                cursor = FavoritePlayTableHelper.getInstance(context).getFavoriteSongList();
                songsList = getSongsFromSQLDBCursor(cursor);
                break;
        }
        return songsList;
    }

    private ArrayList<SongDetail> getSongsFromCursor(Cursor cursor) {
        ArrayList<SongDetail> generassongsList = new ArrayList<SongDetail>();
        try {
            if (cursor != null && cursor.getCount() >= 1) {
                int _id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int album_id = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int display_name = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {

                    int ID = cursor.getInt(_id);
                    String ARTIST = cursor.getString(artist);
                    String TITLE = cursor.getString(title);
                    String DISPLAY_NAME = cursor.getString(display_name);
                    String DURATION = cursor.getString(duration);
                    String Path = cursor.getString(data);

                    SongDetail mSongDetail = new SongDetail(ID, album_id, ARTIST, TITLE, Path, DISPLAY_NAME, DURATION);
                    generassongsList.add(mSongDetail);
                }
            }
            closeCrs();
        } catch (Exception e) {
            closeCrs();
            e.printStackTrace();
        }
        return generassongsList;
    }

    private ArrayList<SongDetail> getSongsFromSQLDBCursor(Cursor cursor) {
        ArrayList<SongDetail> generassongsList = new ArrayList<SongDetail>();
        try {
            if (cursor != null && cursor.getCount() >= 1) {

                while (cursor.moveToNext()) {
                    long ID = cursor.getLong(cursor.getColumnIndex(FavoritePlayTableHelper.ID));
                    long album_id = cursor.getLong(cursor.getColumnIndex(FavoritePlayTableHelper.ALBUM_ID));
                    String ARTIST = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.ARTIST));
                    String TITLE = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.TITLE));
                    String DISPLAY_NAME = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.DISPLAY_NAME));
                    String DURATION = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.DURATION));
                    String Path = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.PATH));

                    SongDetail mSongDetail = new SongDetail((int) ID, (int) album_id, ARTIST, TITLE, Path, DISPLAY_NAME, "" + (Long.parseLong(DURATION) * 1000));
                    generassongsList.add(mSongDetail);
                }
            }
            closeCrs();
        } catch (Exception e) {
            closeCrs();
            e.printStackTrace();
        }
        return generassongsList;
    }

    private void closeCrs() {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception e) {
                Log.e("tmessages", e.toString());
            }
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

    private final String[] projectionSongs = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION};

    public static PhoneMediaControlINterface phonemediacontrolinterface;

    public static PhoneMediaControlINterface getPhonemediacontrolinterface() {
        return phonemediacontrolinterface;
    }

    public static void setPhonemediacontrolinterface(PhoneMediaControlINterface phonemediacontrolinterface) {
        PhoneMediaControl.phonemediacontrolinterface = phonemediacontrolinterface;
    }

    public interface PhoneMediaControlINterface {
        public void loadSongsComplete(ArrayList<SongDetail> songsList);
    }

}

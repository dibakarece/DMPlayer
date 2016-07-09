package com.dmplayer.dbhandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.dmplayer.ApplicationDMPlayer;
import com.dmplayer.models.SongDetail;

import java.util.ArrayList;

public class MostAndRecentPlayTableHelper {

    public static final String TABLENAME = "MostPlay";

    public static final String ID = "_id";
    public static final String ALBUM_ID = "album_id";
    public static final String ARTIST = "artist";
    public static final String TITLE = "title";
    public static final String DISPLAY_NAME = "display_name";
    public static final String DURATION = "duration";
    public static final String PATH = "path";
    public static final String AUDIOPROGRESS = "audioProgress";
    public static final String AUDIOPROGRESSSEC = "audioProgressSec";
    public static final String LastPlayTime = "lastplaytime";
    public static final String PLAYCOUNT = "playcount";


    private static DMPLayerDBHelper dbHelper = null;
    private static MostAndRecentPlayTableHelper mInstance;
    private SQLiteDatabase sampleDB;


    public static synchronized MostAndRecentPlayTableHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MostAndRecentPlayTableHelper(context);
        }
        return mInstance;
    }

    public Context context;

    public MostAndRecentPlayTableHelper(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((ApplicationDMPlayer) context.getApplicationContext()).DB_HELPER;
        }
    }

    public void inserSong(SongDetail songDetail) {
        try {

            if (isSongExist(songDetail.getId())) {
                return;
            }

            sampleDB = dbHelper.getDB();
            sampleDB.beginTransaction();

            String sql = "Insert or Replace into " + TABLENAME + " values(?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement insert = sampleDB.compileStatement(sql);

            try {
                if (songDetail != null) {
                    insert.clearBindings();
                    insert.bindLong(1, songDetail.getId());
                    insert.bindLong(2, songDetail.getAlbum_id());
                    insert.bindString(3, songDetail.getArtist());
                    insert.bindString(4, songDetail.getTitle());
                    insert.bindString(5, songDetail.getDisplay_name());
                    insert.bindString(6, songDetail.getDuration());
                    insert.bindString(7, songDetail.getPath());
                    insert.bindString(8, songDetail.audioProgress + "");
                    insert.bindString(9, songDetail.audioProgressSec + "");
                    insert.bindString(10, System.currentTimeMillis() + "");
                    insert.bindLong(11, 1);

                    insert.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sampleDB.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e("XML:", e.toString());
        } finally {
            sampleDB.endTransaction();
        }
    }


    private boolean isSongExist(int id_) {
        Cursor mCursor = null;
        boolean isExist = false;
        try {
            String sqlQuery = "select * from " + TABLENAME + " where " + ID + "=" + id_;
            sampleDB = dbHelper.getDB();
            mCursor = sampleDB.rawQuery(sqlQuery, null);
            if (mCursor != null && mCursor.getCount() >= 1) {
                mCursor.moveToNext();
                long count = mCursor.getLong(mCursor.getColumnIndex(PLAYCOUNT));
                count++;
                updateStatus_(count, id_);
                isExist = true;
            }
            closeCurcor(mCursor);
        } catch (Exception e) {
            closeCurcor(mCursor);
            e.printStackTrace();
        }
        return isExist;
    }

    public void updateStatus_(long count, int musicid) {
        try {
            ContentValues values = new ContentValues();
            values.put(PLAYCOUNT, count);
            long success = sampleDB.update(TABLENAME, values, ID + "=?", new String[]{String.valueOf(musicid)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeCurcor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

    public Cursor getMostPlay() {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + TABLENAME + " where " + PLAYCOUNT + ">=2 order by " + LastPlayTime + " ASC limit 20";
            sampleDB = dbHelper.getDB();
            mCursor = sampleDB.rawQuery(sqlQuery, null);
        } catch (Exception e) {
            closeCurcor(mCursor);
            e.printStackTrace();
        }
        return mCursor;
    }
}

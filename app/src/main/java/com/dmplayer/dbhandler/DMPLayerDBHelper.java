package com.dmplayer.dbhandler;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dmplayer.R;


public class DMPLayerDBHelper extends SQLiteOpenHelper {
    private Context context_;
    private static String DATABASE_NAME = "";
    private static int DATABASE_VERSION = 0;
    private String DB_PATH = "";
    private SQLiteDatabase db;

    public DMPLayerDBHelper(Context context) {
        super(context, context.getResources().getString(R.string.DataBaseName), null, Integer.parseInt(context.getResources().getString(R.string.DataBaseName_Version)));
        this.context_ = context;
        DATABASE_NAME = context.getResources().getString(R.string.DataBaseName);
        DATABASE_VERSION = Integer.parseInt(context.getResources().getString(R.string.DataBaseName_Version));
        DB_PATH = context.getDatabasePath(DATABASE_NAME).getPath();
        context.openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE, null);
    }

    public SQLiteDatabase getDB() {
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(sqlForCreateMostPlay());
            db.execSQL(sqlForCreateFavoritePlay());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + MostAndRecentPlayTableHelper.TABLENAME);
            db.execSQL("DROP TABLE IF EXISTS " + FavoritePlayTableHelper.TABLENAME);
            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        if (getDB() != null)
            getDB().close();

        super.close();
    }

    public void openDataBase() throws SQLException {
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String sqlForCreateMostPlay() {
        String sql = "CREATE TABLE " + MostAndRecentPlayTableHelper.TABLENAME + " (" + MostAndRecentPlayTableHelper.ID + " INTEGER NOT NULL PRIMARY KEY,"
                + MostAndRecentPlayTableHelper.ALBUM_ID + " INTEGER NOT NULL,"

                + MostAndRecentPlayTableHelper.ARTIST + " TEXT NOT NULL,"
                + MostAndRecentPlayTableHelper.TITLE + " TEXT NOT NULL,"
                + MostAndRecentPlayTableHelper.DISPLAY_NAME + " TEXT NOT NULL,"
                + MostAndRecentPlayTableHelper.DURATION + " TEXT NOT NULL,"
                + MostAndRecentPlayTableHelper.PATH + " TEXT NOT NULL,"
                + MostAndRecentPlayTableHelper.AUDIOPROGRESS + " TEXT NOT NULL,"
                + MostAndRecentPlayTableHelper.AUDIOPROGRESSSEC + " INTEGER NOT NULL,"
                + MostAndRecentPlayTableHelper.LastPlayTime + " TEXT NOT NULL,"
                + MostAndRecentPlayTableHelper.PLAYCOUNT + " INTEGER NOT NULL);";
        return sql;
    }

    public static String sqlForCreateFavoritePlay() {
        String sql = "CREATE TABLE " + FavoritePlayTableHelper.TABLENAME + " (" + FavoritePlayTableHelper.ID + " INTEGER NOT NULL PRIMARY KEY,"
                + FavoritePlayTableHelper.ALBUM_ID + " INTEGER NOT NULL,"

                + FavoritePlayTableHelper.ARTIST + " TEXT NOT NULL,"
                + FavoritePlayTableHelper.TITLE + " TEXT NOT NULL,"
                + FavoritePlayTableHelper.DISPLAY_NAME + " TEXT NOT NULL,"
                + FavoritePlayTableHelper.DURATION + " TEXT NOT NULL,"
                + FavoritePlayTableHelper.PATH + " TEXT NOT NULL,"
                + FavoritePlayTableHelper.AUDIOPROGRESS + " TEXT NOT NULL,"
                + FavoritePlayTableHelper.AUDIOPROGRESSSEC + " INTEGER NOT NULL,"
                + FavoritePlayTableHelper.LastPlayTime + " TEXT NOT NULL,"
                + FavoritePlayTableHelper.ISFAVORITE + " INTEGER NOT NULL);";
        return sql;
    }

}

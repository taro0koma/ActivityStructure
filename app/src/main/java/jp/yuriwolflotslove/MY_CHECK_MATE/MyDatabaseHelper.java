package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "saveitems";
    public static final String COL_ID = "_id";
    public static final String COL_PLACE = "place";
    public static final String COL_URI = "uri";
    public static final String COL_MEMO = "memo";
    public static final String COL_LASTUPDATE = "lastupdate";
    static final String DATABASE_NAME = "mycheckmate.db";
    static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COL_PLACE + " TEXT NOT NULL,"
                        + COL_URI + " TEXT,"
                        + COL_MEMO + " TEXT,"
                        + COL_LASTUPDATE + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}

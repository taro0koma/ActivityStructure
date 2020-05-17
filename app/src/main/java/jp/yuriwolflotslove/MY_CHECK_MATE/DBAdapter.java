package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;

public class DBAdapter {
    public static final String TABLE_NAME = "saveitems";
    public static final String COL_ID = "_id";
    public static final String COL_PLACE = "place";
    public static final String COL_URI = "uri";
    public static final String COL_MEMO = "memo";
    public static final String COL_LASTUPDATE = "lastupdate";
    static final String DATABASE_NAME = "checklist.db";
    static final int DATABASE_VERSION = 1;
    protected final Context context;
    protected DatabaseHelper dbHelper;
    protected SQLiteDatabase db;

    public DBAdapter(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }


    public DBAdapter open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public boolean deleteAllNotes() throws IOException {
        db.beginTransaction();
        boolean result =true;
        int deleteValue = 0;
        Cursor cursor =this.getAllNotes();
        if(cursor.moveToFirst()) {
            deleteValue = db.delete(TABLE_NAME, null, null);
            result = db.delete(TABLE_NAME, null, null) > 0;
        }
         db.setTransactionSuccessful();
         db.endTransaction();
//        String message = "DELETE FROM";
//        db.execSQL(message + TABLE_NAME);
        return true;

    }


    public boolean deleteNote() {
        return db.delete(TABLE_NAME, COL_ID + "=" + 0, null) > 0;
    }

    public Cursor getAllNotes() {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        return cursor;
    }

    public void saveItem(String place, String uri, String memo, String dateTime)
            throws IOException {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_PLACE, place);
            values.put(COL_URI, uri);
            values.put(COL_MEMO, memo);
            values.put(COL_LASTUPDATE, dateTime);
            db.insert(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE " + TABLE_NAME + " ("
                            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + COL_PLACE + " TEXT,"
                            + COL_URI + " TEXT,"
                            + COL_MEMO + " TEXT,"
                            + COL_LASTUPDATE + " TEXT);");
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
}

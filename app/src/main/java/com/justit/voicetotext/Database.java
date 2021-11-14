package com.justit.voicetotext;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class Database  extends SQLiteOpenHelper {

    public static final int dbversion = 1;
    public static final String dbname = "Data.db";
    public static final String ID = "id";
    public static final String TABLE_NAME = "info";
    public static final String DATA = "data";
    public static final String DATE = "date";
    public static final String TRANSLATING_DATA = "date";

    public static final String Create_Table = ("create table " + TABLE_NAME + " ( " + ID + " INTEGER PRIMARY KEY ," + DATA + ", " + DATE + ", "+ TRANSLATING_DATA +")");
    private Context context;

    public Database(Context context) {
        super(context, dbname, null, dbversion);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(Create_Table);
        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }
    }

}

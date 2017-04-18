package com.mclovesmy.birthdaygift.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BirthdayDatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME = "BIRTHDAYS";

    // Table columns
    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String DATE = "date";
    public static final String IMAGE = "image";
    public static final String PRESENTS_GIVEN = "presents_given";
    public static final String PRESENTS_IDEAS = "presents_ideas";
    public static final String GENDER = "gender";

    // Database Information
    static final String DB_NAME = "BIRTHDAYS.DB";

    // database version
    static final int DB_VERSION = 3;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT NOT NULL, " + DATE + " TEXT, " + IMAGE + " TEXT, " + PRESENTS_GIVEN + " TEXT, " + PRESENTS_IDEAS + " TEXT, " + GENDER + " TEXT);";

    public BirthdayDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + PRESENTS_GIVEN + " TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + PRESENTS_IDEAS + " TEXT;");
        } else if (oldVersion == 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + GENDER + " TEXT;");
        }
    }
}

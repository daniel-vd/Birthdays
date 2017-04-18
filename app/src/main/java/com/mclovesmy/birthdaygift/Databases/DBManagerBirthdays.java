package com.mclovesmy.birthdaygift.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManagerBirthdays {
    private BirthdayDatabaseHelper dbHelper;

    private Context context;

    private static SQLiteDatabase database;

    public DBManagerBirthdays(Context c) {
        context = c;
    }

    public DBManagerBirthdays open() throws SQLException {
        dbHelper = new BirthdayDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public static void insert(String name, String date, String image, String presents_given, String presents_ideas, String gender) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(BirthdayDatabaseHelper.NAME, name);
        contentValue.put(BirthdayDatabaseHelper.DATE, date);
        contentValue.put(BirthdayDatabaseHelper.IMAGE, image);
        contentValue.put(BirthdayDatabaseHelper.PRESENTS_GIVEN, presents_given);
        contentValue.put(BirthdayDatabaseHelper.PRESENTS_IDEAS, presents_ideas);
        contentValue.put(BirthdayDatabaseHelper.GENDER, gender);
        database.insert(BirthdayDatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { BirthdayDatabaseHelper._ID, BirthdayDatabaseHelper.NAME, BirthdayDatabaseHelper.DATE, BirthdayDatabaseHelper.IMAGE, BirthdayDatabaseHelper.PRESENTS_GIVEN, BirthdayDatabaseHelper.PRESENTS_IDEAS, BirthdayDatabaseHelper.GENDER };
        Cursor cursor = database.query(BirthdayDatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetch2(String id) {
        String[] columns = new String[] { BirthdayDatabaseHelper._ID, BirthdayDatabaseHelper.NAME, BirthdayDatabaseHelper.DATE, BirthdayDatabaseHelper.IMAGE, BirthdayDatabaseHelper.PRESENTS_GIVEN, BirthdayDatabaseHelper.PRESENTS_IDEAS, BirthdayDatabaseHelper.GENDER };
        Cursor cursor = database.query(BirthdayDatabaseHelper.TABLE_NAME, columns, "_id=?", new String[] {id}, null, null, null);
        if (cursor != null) {
            cursor.moveToNext();
        }
        return cursor;
    }

    public Cursor fetch3() {
        String[] columns = new String[] { BirthdayDatabaseHelper._ID, BirthdayDatabaseHelper.NAME, BirthdayDatabaseHelper.DATE, BirthdayDatabaseHelper.IMAGE, BirthdayDatabaseHelper.PRESENTS_GIVEN, BirthdayDatabaseHelper.PRESENTS_IDEAS, BirthdayDatabaseHelper.GENDER };
        Cursor cursor = database.query(BirthdayDatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToPosition(0);
        }

        return cursor;
    }


    public int update(long _id, String name, String date, String image, String presents_given, String presents_ideas, String gender) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BirthdayDatabaseHelper.NAME, name);
        contentValues.put(BirthdayDatabaseHelper.DATE, date);
        contentValues.put(BirthdayDatabaseHelper.IMAGE, image);
        contentValues.put(BirthdayDatabaseHelper.PRESENTS_GIVEN, presents_given);
        contentValues.put(BirthdayDatabaseHelper.PRESENTS_IDEAS, presents_ideas);
        contentValues.put(BirthdayDatabaseHelper.GENDER, gender);
        return database.update(BirthdayDatabaseHelper.TABLE_NAME, contentValues, BirthdayDatabaseHelper._ID + " = " + _id, null);
    }

    public int update2(long _id, String presents_ideas) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BirthdayDatabaseHelper.PRESENTS_IDEAS, presents_ideas);
        return database.update(BirthdayDatabaseHelper.TABLE_NAME, contentValues, BirthdayDatabaseHelper._ID + " = " + _id, null);
    }

    public int update3(long _id, String presents_given) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BirthdayDatabaseHelper.PRESENTS_GIVEN, presents_given);
        return database.update(BirthdayDatabaseHelper.TABLE_NAME, contentValues, BirthdayDatabaseHelper._ID + " = " + _id, null);
    }

    public static int update4(long _id, String name, String date, String image, String gender) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BirthdayDatabaseHelper.NAME, name);
        contentValues.put(BirthdayDatabaseHelper.DATE, date);
        contentValues.put(BirthdayDatabaseHelper.IMAGE, image);
        contentValues.put(BirthdayDatabaseHelper.GENDER, gender);
        return database.update(BirthdayDatabaseHelper.TABLE_NAME, contentValues, BirthdayDatabaseHelper._ID + " = " + _id, null);
    }

    public static int update5(long _id, String name, String date, String gender) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BirthdayDatabaseHelper.NAME, name);
        contentValues.put(BirthdayDatabaseHelper.DATE, date);
        contentValues.put(BirthdayDatabaseHelper.GENDER, gender);
        return database.update(BirthdayDatabaseHelper.TABLE_NAME, contentValues, BirthdayDatabaseHelper._ID + " = " + _id, null);
    }

    public void delete(long _id) {
        database.delete(BirthdayDatabaseHelper.TABLE_NAME, BirthdayDatabaseHelper._ID + "=" + _id, null);
    }

    public void deleteAll() {
        database.delete(BirthdayDatabaseHelper.TABLE_NAME, null, null);
    }
}

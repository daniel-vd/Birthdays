package com.mclovesmy.birthdaygift.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.mclovesmy.birthdaygift.BirthdayActivity;
import com.mclovesmy.birthdaygift.Databases.DBManagerBirthdays;
import com.mclovesmy.birthdaygift.R;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Random;

public class AlarmService extends Service {
    Random random = new Random();

    private static int NOTIFICATION_ID;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @SuppressWarnings("static-access")
    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);

        checkBirthdays();
    }

    String id;
    String date;
    int years;

    Intent mIntent;

    Bundle bundle;

    private DBManagerBirthdays dbManager;

    public void checkBirthdays () {
        dbManager = new DBManagerBirthdays(getApplicationContext());
        dbManager.open();
        Cursor cursor = dbManager.fetch3();


        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                NOTIFICATION_ID = random.nextInt(9999 - 1000) + 1000;

                id = cursor.getString(cursor.getColumnIndex("_id"));

                Cursor cursor2 = dbManager.fetch2(id);

                date = cursor2.getString(cursor2.getColumnIndex("date"));

                String[] date2 = date.split("/");

                DateTime endDate = new DateTime();
                Calendar calendar = Calendar.getInstance();
                DateTime startDate = new DateTime(calendar.get(Calendar.YEAR), Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);

                String days = String.valueOf(Days.daysBetween(new LocalDate(endDate), new LocalDate(startDate)).getDays());

                if (Integer.parseInt(days) < 0) {
                    startDate = new DateTime((calendar.get(Calendar.YEAR) + 1), Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);
                    days = String.valueOf(Days.daysBetween(endDate, startDate).getDays());
                }

                if (Integer.parseInt(days) == 0) {
                    DateTime startDate2 = new DateTime(Integer.parseInt(date2[2]), Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);
                    DateTime endDate2 = new DateTime();

                    years = Years.yearsBetween(startDate2, endDate2).getYears() + 1;

                } else if (Integer.parseInt(days) == 7) {
                    DateTime startDate2 = new DateTime(Integer.parseInt(date2[2]), Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);
                    DateTime endDate2 = new DateTime();

                    years = Years.yearsBetween(startDate2, endDate2).getYears() + 1;
                }

                if (Integer.parseInt(days) == 0 || Integer.parseInt(days) == 7) {

                    String image = cursor2.getString(cursor2.getColumnIndex("image"));

                    Context context = this.getApplicationContext();
                    notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                    mIntent = new Intent(this, BirthdayActivity.class);

                    bundle = new Bundle();
                    mIntent.putExtra("id", id);
                    pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                    builder.setContentTitle("" + cursor2.getString(cursor2.getColumnIndex("name")));

                    builder.setAutoCancel(true);

                    if (Integer.parseInt(days) == 0) {

                        builder.setContentText("Turns " + years + " today");
                    }
                    if (Integer.parseInt(days) == 7) {

                        builder.setContentText("Turns " + years + " in one week");
                    }


                    Bitmap bitmap = null;
                    File f = new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE), "birthdayPics" + image + ".jpg");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    try {
                        bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    builder.setSmallIcon(R.drawable.ic_stat_name);

                    if (!image.equals("noImage")) {
                        builder.setLargeIcon(bitmap);
                    } else {
                        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person_black_24dp));
                    }

                    builder.setContentIntent(pendingIntent);

                    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }

                cursor.moveToNext();
            }
        }

    }

}

package com.mclovesmy.birthdaygift.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    Random random = new Random();

    private static int NOTIFICATION_ID;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;

    String id;
    String date;
    int years;

    Intent mIntent;

    Bundle bundle;

    private DBManagerBirthdays dbManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        //This notification gets removed at the end of the file.
        //This 'fake' notification seems to be necessary for the real notifications to work
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_person_black_24dp)
                        .setContentTitle("Birthdays")
                        .setContentText("There are birthdays!");
// Sets an ID for the notification
        int mNotificationId = 1;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        dbManager = new DBManagerBirthdays(context);
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

                    notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                    mIntent = new Intent(context, BirthdayActivity.class);

                    bundle = new Bundle();
                    mIntent.putExtra("id", id);
                    pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                    builder.setContentTitle("" + cursor2.getString(cursor2.getColumnIndex("name")));

                    builder.setAutoCancel(true);

                    if (Integer.parseInt(days) == 0) {

                        builder.setContentText("Turns " + years + " today");
                    }
                    if (Integer.parseInt(days) == 7) {

                        builder.setContentText("Turns " + years + " in one week");
                    }

                    Bitmap bitmap = null;
                    File f = new File(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE), "birthdayPics" + image + ".jpg");
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

                    notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }

                cursor.moveToNext();
            }
        }
        mNotifyMgr.cancel(mNotificationId);
    }
}

package com.mclovesmy.birthdaygift;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.mclovesmy.birthdaygift.Databases.BirthdayDatabaseHelper;
import com.mclovesmy.birthdaygift.Databases.DBManagerBirthdays;
import com.mclovesmy.birthdaygift.helpActivities.NewGiftActivity;
import com.mclovesmy.birthdaygift.utils.AlarmReceiver;
import com.mclovesmy.birthdaygift.utils.CheckInternet;
import com.mclovesmy.birthdaygift.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, SpringListener {

    private Spring spring;

    FloatingActionButton fab;

    private DBManagerBirthdays dbManager;

    final String[] from = new String[] { BirthdayDatabaseHelper._ID,
                BirthdayDatabaseHelper.NAME, BirthdayDatabaseHelper.DATE, BirthdayDatabaseHelper.IMAGE,
                BirthdayDatabaseHelper.NAME, BirthdayDatabaseHelper.NAME};

        final int[] to = new int[] { R.id.Id, R.id.Name, R.id.Date, R.id.Image, R.id.tillText, R.id.turnsText};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAlarm(this);

        new downloadGiftList().execute();

        //Initialize floating action button
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(this);

        fab.setOnTouchListener(this);

        fab.setVisibility(View.INVISIBLE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1500);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        fab.setAnimation(animation);
        fab.startAnimation(animation);
        fab.setVisibility(View.VISIBLE);

        SpringSystem springSystem = SpringSystem.create();

        // Add a spring to the system.
        spring = springSystem.createSpring();
        spring.addListener(this);

        double DAMPER = 20;
        double TENSION = 800;
        SpringConfig config = new SpringConfig(TENSION, DAMPER);
        spring.setSpringConfig(config);
    }

    public static void setAlarm(Context context){

        PendingIntent pendingIntent;

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                alarmIntent,
                PendingIntent.FLAG_NO_CREATE) != null);

        //TODO i'm pretty sure this should be removed soon, but works fine for now.
        if (alarmUp) {
            Toast.makeText(context, "1", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "2", Toast.LENGTH_SHORT).show();

        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 25);

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        if (calendar.before(now)) {
           calendar.add(Calendar.DAY_OF_MONTH, 1);

            Toast.makeText(context, "1: Alarm at: " + calendar, Toast.LENGTH_LONG).show();

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            Toast.makeText(context, "2: Alarm at: " + calendar, Toast.LENGTH_LONG).show();

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private class downloadGiftList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            //The gift list file
            File giftListFile = new File(MainActivity.this.getFilesDir(), "gifts.txt");

            try {
                if (!giftListFile.isFile()) {
                    //Create the gift list file
                    giftListFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (!new CheckInternet().CheckInternetConnection()) {
                    return "";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Get the contents of online gift list file.
            //The url
            URL url = null;
            try {
                url = new URL(BuildConfig.GiftList);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            Scanner s = null;
            try {
                if (url != null) {
                    //Open the online file
                    s = new Scanner(new InputStreamReader(url.openStream()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Put file contents in arraylist 'list'
            ArrayList<String> list = new ArrayList<>();
            if (s != null) {
                while (s.hasNext()){
                    list.add(s.nextLine());
                }
            }
            if (s != null) {
                s.close();
            }

            //Write online file to internal file
            try {
                //Writer
                FileWriter writer = new FileWriter(giftListFile);

                //Write every gift to file
                for (int i = 0; i < list.size(); i++) {
                    writer.append("\n").append(list.get(i));
                }
                //Close
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("")) {
                Toast.makeText(MainActivity.this, "" + result, Toast.LENGTH_SHORT).show();
            }
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    /* Maybe for later use?
    public void readFile (View v) throws Exception {
        try {
            FileInputStream inStream = new FileInputStream(String.valueOf(new File(this.getFilesDir() + "/gifts.txt")));
            InputStreamReader inputStreamReader = new InputStreamReader(inStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder finalString = new StringBuilder();
            String oneLine;

            while ((oneLine = bufferedReader.readLine()) != null) {
                finalString.append(oneLine);
            }

            bufferedReader.close();
            inStream.close();
            inputStreamReader.close();

            Toast.makeText(MainActivity.this, "" + finalString, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    } */

    public void onClick(View v) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                Intent intent = new Intent(getApplicationContext(), NewBirthdayActivity.class);
                startActivity(intent);

                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        Animation fadeIn = new AlphaAnimation(1, 0);
                        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                        fadeIn.setDuration(250);

                        AnimationSet animation = new AnimationSet(false);
                        animation.addAnimation(fadeIn);
                        fab.setAnimation(animation);
                        fab.startAnimation(animation);

                        fab.setVisibility(View.INVISIBLE);
                    }
                }, 200);
            }
        }, 300);

    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                spring.setEndValue(1f);
                return false;
            case MotionEvent.ACTION_UP:
                spring.setEndValue(0f);
                return false;
        }

        return false;
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        float scale = 1f - (value * 0.5f);
        fab.setScaleX(scale);
        fab.setScaleY(scale);
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        dbManager = new DBManagerBirthdays(MainActivity.this);
        dbManager.open();
        Cursor cursor = dbManager.fetch();

        fab.setVisibility(View.VISIBLE);

        final ListView listView = (ListView) findViewById(R.id.BirthdayListview);
        listView.setEmptyView(findViewById(R.id.BirthdayEmpty));

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.item_birthday, cursor, from, to, 0);
        adapter.notifyDataSetChanged();

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.Image) {

                    int getIndex = cursor.getColumnIndex("image");
                    String stage = cursor.getString(getIndex);

                    if(!stage.equals("noImage")) {
                        view.setVisibility(View.VISIBLE);
                        Picasso.with(getApplicationContext()).load(new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE), "birthdayPics" + stage + ".jpg")).error(R.drawable.ic_person_black_24dp).placeholder(R.drawable.ic_person_black_24dp).transform(new CircleTransform()).into((ImageView) view);
                    } else {
                        view.setVisibility(View.VISIBLE);
                        Picasso.with(getApplicationContext()).load(R.drawable.ic_person_black_24dp).error(R.drawable.ic_person_black_24dp).placeholder(R.drawable.ic_person_black_24dp).into((ImageView) view);

                    }
                    return true;
                } if (view.getId() == R.id.tillText) {

                    Calendar calendar = Calendar.getInstance();

                    int year = calendar.get(Calendar.YEAR);

                    int getIndex = cursor.getColumnIndex("date");
                    String date = cursor.getString(getIndex);
                    String[] date2 = date.split("/");

                    DateTime endDate = new DateTime();

                    DateTime startDate = new DateTime(year, Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);

                    String days = String.valueOf(Days.daysBetween(new LocalDate(endDate), new LocalDate(startDate)).getDays());

                    if (Integer.parseInt(days) < 0) {
                        startDate = new DateTime((year + 1), Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);
                        days = String.valueOf(Days.daysBetween(endDate, startDate).getDays());
                        ((TextView) view).setText(getApplicationContext().getString(R.string.in_days, days));
                    } else if (Integer.parseInt(days) == 0) {
                        ((TextView) view).setText(R.string.today);
                    } else {
                        ((TextView) view).setText(getApplicationContext().getString(R.string.in_days, days));
                    }

                    return true;
                } if (view.getId() == R.id.turnsText) {
                    Calendar calendar = Calendar.getInstance();

                    int getIndex = cursor.getColumnIndex("date");
                    String date = cursor.getString(getIndex);
                    String[] date2 = date.split("/");

                    DateTime startDate = new DateTime(Integer.parseInt(date2[2]), Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);
                    DateTime endDate = new DateTime();

                    Years y = Years.yearsBetween(startDate, endDate);
                    int years = y.getYears();
                    String years2 = String.valueOf(years + 1);

                    ((TextView) view).setText(getApplicationContext().getString(R.string.turns, years2));
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(adapter);

        // OnCLickListiner For List Items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long viewId) {
                TextView idTextView = (TextView) view.findViewById(R.id.Id);

                ImageView imageView = (ImageView) view.findViewById(R.id.Image);

                Intent intent = new Intent(getApplicationContext(), BirthdayActivity.class);
                intent.putExtra("id", idTextView.getText().toString());
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(MainActivity.this, imageView, "picture");
                if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Not working yet. This app is still in alpha", Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.remove_all) {
            dbManager.deleteAll();
        }
        if (id == R.id.new_gift) {
            Intent intent = new Intent(getApplicationContext(), NewGiftActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}

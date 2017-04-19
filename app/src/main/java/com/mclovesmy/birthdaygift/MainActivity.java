package com.mclovesmy.birthdaygift;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
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
import com.mclovesmy.birthdaygift.utils.AlarmReceiver;
import com.mclovesmy.birthdaygift.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.io.File;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, SpringListener {

    private static double TENSION = 800;
    private static double DAMPER = 20;

    private SpringSystem springSystem;
    private Spring spring;

    FloatingActionButton fab;

    PendingIntent pendingIntent;

    private DBManagerBirthdays dbManager;

    private SimpleCursorAdapter adapter;

        final String[] from = new String[] { BirthdayDatabaseHelper._ID,
                BirthdayDatabaseHelper.NAME, BirthdayDatabaseHelper.DATE, BirthdayDatabaseHelper.IMAGE,
                BirthdayDatabaseHelper.NAME, BirthdayDatabaseHelper.NAME};

        final int[] to = new int[] { R.id.Id, R.id.Name, R.id.Date, R.id.Image, R.id.tillText, R.id.turnsText};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAlarm();

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

        springSystem = SpringSystem.create();

        // Add a spring to the system.
        spring = springSystem.createSpring();
        spring.addListener(this);

        SpringConfig config = new SpringConfig(TENSION, DAMPER);
        spring.setSpringConfig(config);
    }

    public void setAlarm(){
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 15);

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        if (calendar.before(now)) {
           calendar.add(Calendar.DAY_OF_MONTH, 1);

            manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

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

        adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.item_birthday, cursor, from, to, 0);
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
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    int getIndex = cursor.getColumnIndex("date");
                    String date = cursor.getString(getIndex);
                    String[] date2 = date.split("/");

                    DateTime endDate = new DateTime();

                    DateTime startDate = new DateTime(year, Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);

                    String days = String.valueOf(Days.daysBetween(new LocalDate(endDate), new LocalDate(startDate)).getDays());

                    if (Integer.parseInt(days) < 0) {
                        startDate = new DateTime((year + 1), Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);
                        days = String.valueOf(Days.daysBetween(endDate, startDate).getDays());
                        ((TextView) view).setText("In " + days + " days");
                    } else if (Integer.parseInt(days) == 0) {
                        ((TextView) view).setText("Today!");
                    } else {
                        ((TextView) view).setText("In " + days + " days");
                    }

                    return true;
                } if (view.getId() == R.id.turnsText) {
                    Calendar calendar = Calendar.getInstance();
                    int now = calendar.get(Calendar.YEAR);

                    int getIndex = cursor.getColumnIndex("date");
                    String date = cursor.getString(getIndex);
                    String[] date2 = date.split("/");

                    DateTime startDate = new DateTime(Integer.parseInt(date2[2]), Integer.parseInt(date2[1]), Integer.parseInt(date2[0]), 0, 0);
                    DateTime endDate = new DateTime();

                    Years y = Years.yearsBetween(startDate, endDate);
                    int years = y.getYears();

                    ((TextView) view).setText("Turns " + (years + 1));
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

        return super.onOptionsItemSelected(item);
    }

}

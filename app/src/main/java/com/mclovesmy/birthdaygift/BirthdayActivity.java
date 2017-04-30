package com.mclovesmy.birthdaygift;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adroitandroid.chipcloud.ChipCloud;
import com.adroitandroid.chipcloud.ChipListener;
import com.adroitandroid.chipcloud.FlowLayout.Gravity;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.mclovesmy.birthdaygift.Databases.DBManagerBirthdays;
import com.mclovesmy.birthdaygift.utils.CircleProgressBar;
import com.mclovesmy.birthdaygift.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class BirthdayActivity extends AppCompatActivity{

    ImageView birthdayPicture;

    private DBManagerBirthdays dbManager;

    Intent intent;
    String id;

    Dialog dialog;

    int dialogVersion;

    String[] present_ideas;

    CircleProgressBar circleProgressBar;

    RequestQueue requestQueue;

    Cursor cursor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new getGiftList().execute("");

        requestQueue = Volley.newRequestQueue(this);

        itemSuggestions();

        intent = getIntent();
        id = intent.getStringExtra("id");

        dbManager = new DBManagerBirthdays(BirthdayActivity.this);
        dbManager.open();
        cursor = dbManager.fetch2(id);

        presentsGivenInit();

        int getNameIndex = cursor.getColumnIndex("name");
        String name = cursor.getString(getNameIndex);

        final ChipCloud chipCloud = (ChipCloud) findViewById(R.id.chip_cloud);

        int getNameIndex2 = cursor.getColumnIndex("presents_ideas");
        final String presents_ideas = cursor.getString(getNameIndex2);

        if (!presents_ideas.trim().equals("")) {

            present_ideas = convertStringToArray(presents_ideas);
        } else {
            present_ideas = new String[1];
            present_ideas[0] = "No present ideas found.";
        }

        new ChipCloud.Configure()
                .chipCloud(chipCloud)
                .selectedColor(Color.parseColor("#ff00cc"))
                .selectedFontColor(Color.parseColor("#ffffff"))
                .deselectedColor(Color.parseColor("#e1e1e1"))
                .deselectedFontColor(Color.parseColor("#333333"))
                .selectTransitionMS(500)
                .deselectTransitionMS(250)
                .labels(present_ideas)
                .mode(ChipCloud.Mode.MULTI)
                .allCaps(false)
                .gravity(Gravity.LEFT)
                .textSize(getResources().getDimensionPixelSize(R.dimen.default_textsize))
                .verticalSpacing(getResources().getDimensionPixelSize(R.dimen.vertical_spacing))
                .minHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing))
                .chipListener(new ChipListener() {
                    @Override
                    public void chipSelected(int index) {
                        List<String> stringList = new ArrayList<>(Arrays.asList(present_ideas));

                        stringList.remove(index);

                        String present_ideas = TextUtils.join(", ", stringList);

                        dbManager.update2(Long.parseLong(id), present_ideas + "");

                        onResume();
                    }
                    @Override
                    public void chipDeselected(int index) {
                        //...
                    }
                })
                .build();

        chipCloud.setVisibility(View.INVISIBLE);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {
                chipCloud.setVisibility(View.VISIBLE);
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(1250);

                AnimationSet animation = new AnimationSet(false); //change to false
                animation.addAnimation(fadeIn);
                chipCloud.setAnimation(animation);
            }
        }, 200);

        this.setTitle(name + "");

        birthdayPicture = (ImageView) findViewById(R.id.birthdayPicture);

        int getIndex = cursor.getColumnIndex("image");
        String image = cursor.getString(getIndex);

        if (!image.equals("noImage")) {
            Picasso.with(getApplicationContext()).load(new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE), "birthdayPics" + image + ".jpg")).error(R.drawable.ic_person_black_24dp).placeholder(R.drawable.ic_person_black_24dp).transform(new CircleTransform()).into(birthdayPicture);
        }

        //Days to go
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);

        int getIndex2 = cursor.getColumnIndex("date");
        String date = cursor.getString(getIndex2);
        String[] date2 = date.split("/");

        DateTime endDate = new DateTime();

        DateTime startDate = new DateTime(year, parseInt(date2[1]), parseInt(date2[0]), 0, 0);
        DateTime startDate2 = new DateTime((year + 1), parseInt(date2[1]), parseInt(date2[0]), 0, 0);

        int days = Days.daysBetween(new LocalDate(endDate), new LocalDate(startDate)).getDays();

        if (days < 0) {
            startDate = new DateTime((year + 1), parseInt(date2[1]), parseInt(date2[0]), 0, 0);
            days = Days.daysBetween(new LocalDate(endDate), new LocalDate(startDate)).getDays();
            startDate2 = new DateTime((year + 2), parseInt(date2[1]), parseInt(date2[0]), 0, 0);
        }

        final float daysBetweenAll = Days.daysBetween(new LocalDate(startDate), new LocalDate(startDate2)).getDays();

        final float progressBarPercent = 100 - ((days / daysBetweenAll) * 100);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                circleProgressBar = (CircleProgressBar) findViewById(R.id.custom_progressBar);
                circleProgressBar.setProgressWithAnimation(progressBarPercent);
            }
        }, 200);

        DateTime startDate3 = new DateTime(parseInt(date2[2]), parseInt(date2[1]), parseInt(date2[0]), 0, 0);
        DateTime endDate2 = new DateTime();

        Years y = Years.yearsBetween(startDate3, endDate2);
        int years = y.getYears();

        TextView textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setText(getApplicationContext().getString(R.string.in_days, String.valueOf(days)));

        TextView textView3 = (TextView) findViewById(R.id.textView3);
        textView3.setText(getApplicationContext().getString(R.string.turns, String.valueOf(years + 1)));

        TextView genderText = (TextView) findViewById(R.id.genderText);

        if (cursor.getString(cursor.getColumnIndex("gender")).equals("Male")) {
            genderText.setText(R.string.male);
        } else if (cursor.getString(cursor.getColumnIndex("gender")).equals("Female")) {
            genderText.setText(R.string.female);
        }

    }

    String[] present_given;

    public void presentsGivenInit() {
        Cursor cursor = dbManager.fetch2(id);

        final ChipCloud chipCloud = (ChipCloud) findViewById(R.id.chip_cloud2);

        int getNameIndex2 = cursor.getColumnIndex("presents_given");
        String presents_given = cursor.getString(getNameIndex2);

        if (!presents_given.trim().equals("")) {
            present_given = convertStringToArray(presents_given);
        } else {
            present_given = new String[1];
            present_given[0] = "No given presents found.";
        }

        new ChipCloud.Configure()
                .chipCloud(chipCloud)
                .selectedColor(Color.parseColor("#ff00cc"))
                .selectedFontColor(Color.parseColor("#ffffff"))
                .deselectedColor(Color.parseColor("#e1e1e1"))
                .deselectedFontColor(Color.parseColor("#333333"))
                .selectTransitionMS(500)
                .deselectTransitionMS(250)
                .labels(present_given)
                .mode(ChipCloud.Mode.MULTI)
                .allCaps(false)
                .gravity(Gravity.LEFT)
                .textSize(getResources().getDimensionPixelSize(R.dimen.default_textsize))
                .verticalSpacing(getResources().getDimensionPixelSize(R.dimen.vertical_spacing))
                .minHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing))
                .chipListener(new ChipListener() {
                    @Override
                    public void chipSelected(int index) {
                        List<String> stringList = new ArrayList<>(Arrays.asList(present_given));

                        stringList.remove(index);

                        if (present_given != null) {

                            String present_given = TextUtils.join(", ", stringList);

                            dbManager.update3(Long.parseLong(id), present_given + "");

                        } else {
                            present_given = new String[1];
                            present_given[0] = "No given presents found.";
                            dbManager.update3(Long.parseLong(id), Arrays.toString(present_given) + "");
                        }
                        onResume();
                    }
                    @Override
                    public void chipDeselected(int index) {

                    }
                })
                .build();

        chipCloud.setVisibility(View.INVISIBLE);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {
                chipCloud.setVisibility(View.VISIBLE);
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(1250);

                AnimationSet animation = new AnimationSet(false); //change to false
                animation.addAnimation(fadeIn);
                chipCloud.setAnimation(animation);
            }
        }, 200);
    }

    ArrayList<String> list;

    private class getGiftList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Scanner s = new Scanner(new File(BirthdayActivity.this.getFilesDir() + "/gifts.txt"));
                list = new ArrayList<>();
                while (s.hasNext()) {
                    list.add(s.nextLine());
                }
                s.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public void addPresentIdea (View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BirthdayActivity.this);
        // Get the layout inflater

        final LayoutInflater inflater = BirthdayActivity.this.getLayoutInflater();

        final LayoutInflater factory = getLayoutInflater();
        final View editTextInflater = factory.inflate(R.layout.dialog_present, null);

        TextView textView = (TextView) editTextInflater.findViewById(R.id.textView);
        textView.setText(R.string.new_gift_idea);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_present, null));

        dialog = builder.create();

        dialog.show();

        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        AutoCompleteTextView editText2 = (AutoCompleteTextView) dialog.findViewById(R.id.presentEditText);

        editText2.setAdapter(adapter);
        editText2.setThreshold(1);

        dialogVersion = 1;
    }

    public void addPresentGiven (View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(BirthdayActivity.this);
        // Get the layout inflater

        final LayoutInflater factory = getLayoutInflater();
        final View editTextInflater = factory.inflate(R.layout.dialog_present, null);

        TextView textView = (TextView) editTextInflater.findViewById(R.id.textView);
        textView.setText(R.string.new_given_gift);

        EditText editText = (EditText) editTextInflater.findViewById(R.id.presentEditText);

        editText.setTag("givenEditText");

        final LayoutInflater inflater = BirthdayActivity.this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_present, null));

        dialog = builder.create();

        dialog.show();

        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, list);

        AutoCompleteTextView editText2 = (AutoCompleteTextView) dialog.findViewById(R.id.presentEditText);

        editText2.setAdapter(adapter);
        editText2.setThreshold(1);

        dialogVersion = 2;
    }

    public void savePresent (View view) {

        EditText editText = (EditText) dialog.findViewById(R.id.presentEditText);

        Cursor cursor = dbManager.fetch2(id);

        if (dialogVersion == 1) {
            int getNameIndex = cursor.getColumnIndex("presents_ideas");
            String presents_ideas = cursor.getString(getNameIndex);

            if (!editText.getText().toString().trim().equals("")) {
                if (presents_ideas != null) {

                    String[] ideasArray = convertStringToArray(presents_ideas);

                    List<String> stringList = new ArrayList<>(Arrays.asList(ideasArray));

                    stringList.add(editText.getText().toString());

                    String present_ideas = TextUtils.join(", ", stringList);

                    dbManager.update2(Long.parseLong(id), present_ideas + "");
                } else {
                    dbManager.update2(Long.parseLong(id), "" + editText.getText().toString());
                }

                dialog.dismiss();
                onResume();
            } else {
                editText.setError(getText(R.string.gift_idea_error));
            }
        } else if (dialogVersion == 2) {
            int getNameIndex = cursor.getColumnIndex("presents_given");
            String presents_ideas = cursor.getString(getNameIndex);

            if (!editText.getText().toString().trim().equals("")) {
                if (presents_ideas != null) {

                    String[] ideasArray = convertStringToArray(presents_ideas);

                    List<String> stringList = new ArrayList<>(Arrays.asList(ideasArray));

                    stringList.add(editText.getText().toString());

                    String present_ideas = TextUtils.join(", ", stringList);

                    dbManager.update3(Long.parseLong(id), present_ideas + "");
                } else {
                    dbManager.update3(Long.parseLong(id), "" + editText.getText().toString());
                }

                dialog.dismiss();
                onResume();
            } else {
                editText.setError(getText(R.string.given_gift_error));
            }
        }

    }

    public static String strSeparator = ",";

    public static String[] convertStringToArray(String str){
        return str.split(strSeparator);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_birthday, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (itemId == R.id.edit_birthday) {
            ImageView imageView = (ImageView) findViewById(R.id.birthdayPicture);

            Intent intent2 = new Intent(BirthdayActivity.this, EditBirthdayActivity.class);
            intent2.putExtra("id", "" + intent.getStringExtra("id"));

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(BirthdayActivity.this, imageView, "picture");
            if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                startActivity(intent2, options.toBundle());
            } else {
                startActivity(intent2);
            }

            circleProgressBar.setProgressWithAnimation(0);
            return true;
        }
        if (itemId == R.id.delete_birthday) {
            dbManager.delete(Long.parseLong(id));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    JSONObject jsonObject;

    double random;

    double random2;

    private void itemSuggestions() {

        // Create request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String[] product_feeds = {"" + BuildConfig.Feed1, "" + BuildConfig.Feed2};
        //  Create json array request
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest((product_feeds[new Random().nextInt(product_feeds.length)]), new Response.Listener<JSONArray>() {
            public void onResponse(final JSONArray jsonArray) {
                try {
                    Random r = new Random();

                    String gender = cursor.getString(cursor.getColumnIndex("gender")) + "";

                    for (int i = r.nextInt(jsonArray.length() - 10); i < jsonArray.length(); i++) {

                        random = Math.random();

                        random2 = Math.random();

                        jsonObject = jsonArray.getJSONObject(i);

                        if (random < 0.4) {
                            //if (random2 < 0.6) {
                                if (jsonObject.getJSONObject("categories").toString().contains("Consumer Electronics")) {
                                    break;
                                }
                            //}
                        } else {
                            if (gender.equals("Male")) {
                                if (!jsonObject.getString("name").toLowerCase().contains(" men ") && !jsonObject.getString("name").toLowerCase().contains(" male ") && !jsonObject.getString("name").toLowerCase().contains(" mens ")) {
                                } else {
                                    break;
                                }
                            } else if (gender.equals("Female")) {
                                if (!jsonObject.getString("name").toLowerCase().contains(" women ") && !jsonObject.getString("name").toLowerCase().contains(" female ")) {
                                } else {
                                    break;
                                }
                            }
                        }
                    }


                    TextView nameItem1 = (TextView) findViewById(R.id.itemName1);
                    TextView namePrice1 = (TextView) findViewById(R.id.itemPrice1);
                    ImageView imageItem1 = (ImageView) findViewById(R.id.itemImage1);
                    LinearLayout item1 = (LinearLayout) findViewById(R.id.item1);

                    item1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent browserIntent = null;
                            try {
                                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + jsonObject.getString("URL")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivity(browserIntent);
                        }
                    });

                    nameItem1.setText(jsonObject.getString("name") + "");
                    namePrice1.setText("€" + jsonObject.getJSONObject("price").getString("amount") + "");

                    Picasso.with(getApplicationContext()).load(jsonObject.getJSONArray("images").getString(0) + "").into(imageItem1);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });

        requestQueue.add(jsonArrayRequest);
    }


}

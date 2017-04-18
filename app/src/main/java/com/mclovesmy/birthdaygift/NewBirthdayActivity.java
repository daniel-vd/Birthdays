package com.mclovesmy.birthdaygift;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mclovesmy.birthdaygift.Databases.DBManagerBirthdays;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import static com.mclovesmy.birthdaygift.R.id.birthdayPerson;

public class NewBirthdayActivity extends AppCompatActivity {

    //Image
    private int PICK_IMAGE_REQUEST = 1;
    Bitmap bitmap = null;
    String image = "noImage";

    //Datepicker
    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;

    //Database
    private DBManagerBirthdays dbManagerBirthdays;

    //Views
    Button setDate;
    Button addImage;
    ImageView imageView;
    TextView date;

    String realDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.setTheme(R.style.AppTheme);
        super.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_birthday);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setDate = (Button) findViewById(R.id.setDate);
        addImage = (Button) findViewById(R.id.addImageBirthday);
        imageView = (ImageView) findViewById(R.id.birthdayImageView);
        date = (TextView) findViewById(R.id.date);

        setDate.setVisibility(View.GONE);
        addImage.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        date.setVisibility(View.GONE);

        ImageView birthdayImageView = (ImageView) findViewById(R.id.birthdayImageView);
        birthdayImageView.setVisibility(View.GONE);
        Button birthdayImagePicker = (Button) findViewById(R.id.addImageBirthday);
        birthdayImagePicker.setVisibility(View.GONE);

        this.setTitle("New birthday");

        int width = LayoutParams.WRAP_CONTENT;
        int height = LayoutParams.WRAP_CONTENT;
        this.getWindow().setLayout(width, height);

        //Calendar initialize
        dateView = (TextView) findViewById(R.id.date);
        calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        showDate(year, MONTHS[month], day);

        realDate = day + "/" + (month + 1) + "/" + year;

        dbManagerBirthdays = new DBManagerBirthdays(this);
        dbManagerBirthdays.open();
    }

    int next = 0;

    //Next choice
    public void next (View view) {

        final EditText editText1 = (EditText) findViewById(R.id.birthdayPerson);
        final Button nextButton1 = (Button) findViewById(R.id.nextButton1);
        final RadioGroup genderGroup = (RadioGroup) findViewById(R.id.genderGroup);

        //Which choice?
        if (next == 0) {
            //First choice = to datePicker
            if (editText1.getText().toString().trim().equals("")) {
                editText1.setError("Please fill in a name");
                return;
            }

            //Animate the edittext out.
            editText1.animate()
                    .translationY(50)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            editText1.setVisibility(View.GONE);
                        }
                    });

            //Animate the genderGroup out.
            genderGroup.animate()
                    .translationY(50)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            genderGroup.setVisibility(View.GONE);
                        }
                    });

            nextButton1.animate()
                    .translationY(50)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            nextButton1.setVisibility(View.GONE);
                        }
                    });

            //Resize dialog
            final LinearLayout newBirthdayLayout = (LinearLayout) findViewById(R.id.newBirthdayLayout);

            /* ResizeAnimation rs = new ResizeAnimation(newBirthdayLayout, 700, true);

            newBirthdayLayout.startAnimation(rs); */

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    setDate.setVisibility(View.VISIBLE);
                    setDate.setAlpha(0);
                    setDate.animate()
                            .translationY(-50)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });

                    date.setVisibility(View.VISIBLE);
                    date.setAlpha(0);
                    date.animate()
                            .translationY(-50)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });

                    nextButton1.setVisibility(View.VISIBLE);
                    nextButton1.setAlpha(0);
                    nextButton1.animate()
                            .translationY(-50)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });

                    next = 1;
                }
            }, 500);
        } else if (next == 1) {
            nextButton1.animate()
                    .translationY(50)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            nextButton1.setVisibility(View.GONE);
                        }
                    });

            date.animate()
                    .translationY(50)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            date.setVisibility(View.GONE);
                        }
                    });

            setDate.animate()
                    .translationY(50)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            setDate.setVisibility(View.GONE);
                        }
                    });

            //Resize dialog
            final LinearLayout newBirthdayLayout = (LinearLayout) findViewById(R.id.newBirthdayLayout);

            /* ResizeAnimation rs = new ResizeAnimation(newBirthdayLayout, 1100, true);

            newBirthdayLayout.startAnimation(rs); */

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    ImageView birthdayImageView = (ImageView) findViewById(R.id.birthdayImageView);
                    birthdayImageView.setVisibility(View.GONE);
                    Button birthdayImagePicker = (Button) findViewById(R.id.addImageBirthday);
                    birthdayImagePicker.setVisibility(View.GONE);

                    birthdayImageView.setVisibility(View.VISIBLE);
                    birthdayImageView.animate()
                            .translationY(-50)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });

                    birthdayImagePicker.setVisibility(View.VISIBLE);
                    birthdayImagePicker.setAlpha(0);
                    birthdayImagePicker.animate()
                            .translationY(-50)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });

                    nextButton1.setVisibility(View.VISIBLE);
                    nextButton1.setAlpha(0);
                    nextButton1.animate()
                            .translationY(-50)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });

                    nextButton1.setText("Save");
                    next = 2;
                }
            }, 500);
        } else if (next == 2) {
            addBirthday();
        }
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

                    realDate = arg3 + "/" + (arg2 +1) + "/" + arg1;

                    showDate(arg1, MONTHS[arg2], arg3);
                }
            };

    private void showDate(int year, String month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    public void addImage (View view) {

        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    Uri uri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();

           /*try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            } */

            Picasso.with(NewBirthdayActivity.this).load(uri).noPlaceholder().into(target);

        }
    }

    final Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap2, LoadedFrom from) {

            final ImageView imageView = (ImageView) findViewById(R.id.birthdayImageView);

            imageView.setImageBitmap(bitmap2);

            bitmap = bitmap2;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };


    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        SharedPreferences settings = getSharedPreferences("prefs", 0);

        int noteImage2 = settings.getInt("birthdayImage", 1);
        settings.edit().putInt("birthdayImage", noteImage2 + 1).commit();
        image = String.valueOf(settings.getInt("birthdayImage", 1));

        // Create imageDir
        File mypath=new File(directory,"birthdayPics" + image + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.15), (int)(bitmap.getHeight()*0.15), true);
            bitmapImage.compress(CompressFormat.PNG, 1, fos);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public void addBirthday () {
        EditText birthday = (EditText) findViewById(birthdayPerson);

        if (bitmap != null) {
            /*BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            bitmap = BitmapFactory.decodeFile(String.valueOf(uri), opts); */

            saveToInternalStorage(bitmap);
        }

        RadioGroup genderGroup = (RadioGroup) findViewById(R.id.genderGroup);

        RadioButton male = (RadioButton) findViewById(R.id.male);
        RadioButton female = (RadioButton) findViewById(R.id.female);

        String gender = "";

        if (genderGroup.getCheckedRadioButtonId() == male.getId()) {
            gender = "Male";
        } else if (genderGroup.getCheckedRadioButtonId() == female.getId()) {
            gender = "Female";
        }

        DBManagerBirthdays.insert(birthday.getText().toString(), realDate, image, "", "", gender);

        finish();

    }

    public static Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;

        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }

}

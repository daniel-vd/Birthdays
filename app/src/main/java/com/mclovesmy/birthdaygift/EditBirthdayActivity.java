package com.mclovesmy.birthdaygift;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mclovesmy.birthdaygift.Databases.DBManagerBirthdays;
import com.mclovesmy.birthdaygift.utils.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class EditBirthdayActivity extends AppCompatActivity{

    private DBManagerBirthdays dbManager;
    Cursor cursor;

    Intent intent;
    String id;

    EditText name;
    EditText birthdate;
    Button setImage;
    ImageView birthdayPicture;

    //Datepicker
    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;

    private int PICK_IMAGE_REQUEST = 1;
    Bitmap bitmap = null;
    String image = "noImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_birthday);

        intent = getIntent();
        id = intent.getStringExtra("id");

        dbManager = new DBManagerBirthdays(EditBirthdayActivity.this);
        dbManager.open();
        cursor = dbManager.fetch2(id);

        this.setTitle("Edit: " + cursor.getString(cursor.getColumnIndex("name")));

        name = (EditText) findViewById(R.id.name);
        birthdate = (EditText)  findViewById(R.id.birthDate);
        setImage = (Button) findViewById(R.id.setImage);
        birthdayPicture = (ImageView) findViewById(R.id.birthdayPicture);

        name.setText(cursor.getString(cursor.getColumnIndex("name")) + "");
        birthdate.setText(cursor.getString(cursor.getColumnIndex("date")) + "");

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        int getIndex = cursor.getColumnIndex("image");
        String image = cursor.getString(getIndex);

        if (!image.equals("noImage")) {
            Picasso.with(getApplicationContext()).load(new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE), "birthdayPics" + image + ".jpg")).error(R.drawable.ic_person_black_24dp).placeholder(R.drawable.ic_person_black_24dp).transform(new CircleTransform()).into(birthdayPicture);
        }
    }

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

                    birthdate.setText(arg3 + "/" + (arg2 + 1) + "/" + arg1);
                }
            };

    //Image handling
    public void addImage (View view) {

        Intent intent = new Intent();
        // Show only images, no videos or anything else

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            Picasso.with(EditBirthdayActivity.this).load(uri).noPlaceholder().into(target);

        }
    }

    final Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap2, LoadedFrom from) {

            final ImageView imageView = (ImageView) birthdayPicture;

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

        if (image.equals("noImage")) {
            SharedPreferences settings = getSharedPreferences("prefs", 0);

            int noteImage = settings.getInt("birthdayImage", 1);
            settings.edit().putInt("birthdayImage", noteImage + 1).commit();
            image = String.valueOf(settings.getInt("birthdayImage", 1));
        } else {
            image = cursor.getString(cursor.getColumnIndex("image"));
        }

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
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
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

    public void saveEdit(View view) {

        if (bitmap != null) {

            saveToInternalStorage(bitmap);

            DBManagerBirthdays.update4(Long.parseLong(intent.getStringExtra("id")), name.getText().toString().trim(), birthdate.getText().toString(), image, "");
            Picasso.with(getApplicationContext()).invalidate(new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE), "birthdayPics" + image + ".jpg"));
            finish();

        } else {
            DBManagerBirthdays.update5(Long.parseLong(intent.getStringExtra("id")), name.getText().toString().trim(), birthdate.getText().toString(), "");
            finish();
        }

    }

}

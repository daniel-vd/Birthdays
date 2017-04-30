package com.mclovesmy.birthdaygift.helpActivities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mclovesmy.birthdaygift.BuildConfig;
import com.mclovesmy.birthdaygift.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NewGiftActivity extends AppCompatActivity {

    EditText editNewGift;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_gift);

        editNewGift = (EditText) findViewById(R.id.editNewGift);

    }

    //Save the new gift and upload to file
    public void saveNewGift (View v) {
        String gift = editNewGift.getText().toString().trim();

        if (gift.equals("")) {
            editNewGift.setError(getText(R.string.new_gift_error));
        } else {
            ArrayList<String> list = null;
            try {
                Scanner s = new Scanner(new File(this.getFilesDir() + "/gifts.txt"));
                list = new ArrayList<String>();
                while (s.hasNext()) {
                    list.add(s.nextLine());
                }
                s.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //Manipulate gift string
            gift = gift.toLowerCase();
            gift = gift.substring(0, 1).toUpperCase() + gift.substring(1);

            if (list != null && list.contains(gift)) {
                Toast.makeText(NewGiftActivity.this, R.string.new_gift_exists, Toast.LENGTH_LONG).show();
                return;
            }

            final String finalGift = gift;

            //Upload the gift to the file
            StringRequest stringRequest = new StringRequest(Request.Method.POST, BuildConfig.NewGift,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(NewGiftActivity.this, R.string.gift_added, Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(NewGiftActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("gift", finalGift);
                    return params;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);

            editNewGift.setText("");
        }
    }

}

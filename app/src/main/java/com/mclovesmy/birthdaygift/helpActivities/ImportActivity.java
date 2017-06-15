package com.mclovesmy.birthdaygift.helpActivities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mclovesmy.birthdaygift.Databases.DBManagerBirthdays;
import com.mclovesmy.birthdaygift.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImportActivity extends AppCompatActivity {

    private DBManagerBirthdays dbManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        dbManager = new DBManagerBirthdays(this);
        dbManager.open();
    }

    public void Import (View view) {
        EditText importText = (EditText) findViewById(R.id.importEditText);

        if (!importText.getText().toString().isEmpty()) {
            //Field is not empty.
            //Now delete all data before inserting.
            dbManager.deleteAll();

            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(importText.getText().toString());
            } catch (JSONException e) { //The backup is not valid.
                Toast.makeText(ImportActivity.this, "This is not a valid Birthdays back-up.", Toast.LENGTH_LONG).show();
            }

            if (jsonarray != null) {
                for (int i = 0; i < jsonarray.length(); i++) {

                    JSONObject jsonobject = null;
                    try {
                        jsonobject = jsonarray.getJSONObject(i);
                    } catch (JSONException e) { //The backup is not valid.
                        Toast.makeText(ImportActivity.this, "This is not a valid Birthdays backup.", Toast.LENGTH_LONG).show();
                    }
                    try {
                        //Everything is good, now insert.
                        dbManager.insert2(jsonobject);
                    } catch (JSONException e) { //The backup is not valid.
                        Toast.makeText(ImportActivity.this, "This is not a valid Birthdays backup.", Toast.LENGTH_LONG).show();
                    }

                    Toast.makeText(ImportActivity.this, "Import succeeded!", Toast.LENGTH_LONG).show();
                    importText.getText().clear();

                }
            } else { //The backup is not valid.
                Toast.makeText(ImportActivity.this, "This is not a valid Birthdays backup.", Toast.LENGTH_LONG).show();
            }
        } else { //Empty field
            Toast.makeText(ImportActivity.this, "Please paste your ideas backup in the text field", Toast.LENGTH_LONG).show();
        }
    }
}

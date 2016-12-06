package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CardSearchActivity extends AppCompatActivity {

    private String searchString;
    private int deckId;

    private SharedPreferences sharedPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_search);

        Intent intent = getIntent();
        deckId = intent.getIntExtra("deck_id", -1);

        RadioButton rad_name = (RadioButton) findViewById(R.id.rad_name);
        rad_name.setChecked(true);

        Button btn_search = (Button) findViewById(R.id.btn_cardSearch);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchString = "";

                TextView searchTerms = (TextView) findViewById(R.id.search_terms);
                RadioGroup rdg_searchType = (RadioGroup) findViewById(R.id.rdg_searchType);

                CheckBox chkW = (CheckBox) findViewById(R.id.colorW);
                CheckBox chkU = (CheckBox) findViewById(R.id.colorU);
                CheckBox chkB = (CheckBox) findViewById(R.id.colorB);
                CheckBox chkR = (CheckBox) findViewById(R.id.colorR);
                CheckBox chkG = (CheckBox) findViewById(R.id.colorG);

                if(!searchTerms.getText().toString().equals("")) {
                    switch(rdg_searchType.getCheckedRadioButtonId()){
                        case R.id.rad_name:
                            searchString += "name=";
                            break;
                        case R.id.rad_type:
                            searchString += "type=";
                            break;
                    }

                    try {
                        searchString += URLEncoder.encode(searchTerms.getText().toString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                addColorParam(chkW, "white");
                addColorParam(chkU, "blue");
                addColorParam(chkB, "black");
                addColorParam(chkR, "red");
                addColorParam(chkG, "green");

                Intent intent = new Intent(CardSearchActivity.this, CardResultActivity.class);
                intent.putExtra("searchString", searchString.toLowerCase());
                intent.putExtra("deck_id", deckId);
                startActivity(intent);
            }
        });

        sharedPrefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);

        switch(sharedPrefs.getString("colour", "Blue")) {
            case "Blue":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF0000FF));
                break;
            case "Green":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF008000));
                break;
            case "Black":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000));
                break;
            case "Red":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFFFF0000));
                break;
        }
    }

    private void addColorParam(CheckBox checkBox, String color) {
        if(checkBox.isChecked()) {
            if(!searchString.equals("")) {
                searchString += "&";
            }
            searchString += "color=" + color;
        }
    }
}

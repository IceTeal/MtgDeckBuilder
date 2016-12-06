package com.example.finalproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewDeckActivity extends AppCompatActivity {

    private EditText deckName;
    private Button createButton;

    private SharedPreferences sharedPrefs;
    // Database Helper
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_deck);

        db = new DatabaseHelper(getApplicationContext());

        deckName = (EditText) findViewById(R.id.et_deckName);
        createButton = (Button) findViewById(R.id.btn_create);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Deck newDeck = new Deck();
                newDeck.setName(deckName.getText().toString());
                db.createDeck(newDeck);

                NewDeckActivity.this.finish();
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
}

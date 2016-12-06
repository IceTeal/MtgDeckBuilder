package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DecksActivity extends AppCompatActivity {

    private ArrayList<Deck> decks = new ArrayList<Deck>();
    private DeckAdapter adapter;

    // Database Helper
    private DatabaseHelper db;

    private Button newDeck;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decks);

        db = new DatabaseHelper(getApplicationContext());

        ListView listView = (ListView) findViewById(R.id.deck_list);
        newDeck = (Button) findViewById(R.id.btn_newDeck);

        decks = db.getAllDecks();
        adapter = new DeckAdapter(this, android.R.layout.simple_list_item_1, decks);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DecksActivity.this, DeckListActivity.class);
                intent.putExtra("id", decks.get(position).getId());
                startActivity(intent);
            }
        });

        newDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DecksActivity.this, NewDeckActivity.class);
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

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private class DeckAdapter extends ArrayAdapter<Deck> {
        private ArrayList<Deck> decks;

        public DeckAdapter(Context context, int textViewResourceId,
                           ArrayList<Deck> decks) {
            super(context, textViewResourceId, decks);
            this.decks = decks;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.deck_list_item, null);
            }

            Deck deck = decks.get(position);

            if (deck != null) {
                TextView name = (TextView)view.findViewById(R.id.deck_name);

                if (deck.getName().length() >= 85) {
                    name.setText(deck.getName().substring(0, 49) + "...");
                } else {
                    name.setText(deck.getName());
                }
            }

            return view;
        }
    }
}

package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DeckListActivity extends AppCompatActivity {

    static final String API_URL = "https://api.deckbrew.com/mtg/cards/";

    private int deck_id;

    private ArrayList<Card> cards = new ArrayList<Card>();
    private DeckCardAdapter adapter;

    // Database Helper
    private DatabaseHelper db;

    private ListView listView;
    private Button deleteDeck;
    private Button addCard;

    private ProgressBar progressBar;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_list);

        Intent intent = getIntent();
        deck_id = intent.getIntExtra("id", -1);

        db = new DatabaseHelper(getApplicationContext());

        listView = (ListView) findViewById(R.id.card_list);
        TextView deckName = (TextView) findViewById(R.id.deck_name);
        deleteDeck = (Button) findViewById(R.id.btn_deleteDeck);
        addCard = (Button) findViewById(R.id.btn_addCard);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        String name = db.getDeck(deck_id).getName();
        deckName.setText(name);

        cards = db.getAllCardsInDeck(deck_id);
        adapter = new DeckCardAdapter(this, android.R.layout.simple_list_item_1, cards);
        listView.setAdapter(adapter);

        new RetrieveFeedTask().execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DeckListActivity.this, DeckCardDetailActivity.class);
                intent.putExtra("card_id", cards.get(position).getId());
                intent.putExtra("deck_id", deck_id);
                startActivity(intent);
            }
        });

        deleteDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteDeck(deck_id);

                DeckListActivity.this.finish();
            }
        });

        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeckListActivity.this, CardSearchActivity.class);
                intent.putExtra("deck_id", deck_id);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), DecksActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private class DeckCardAdapter extends ArrayAdapter<Card> {
        private ArrayList<Card> cards;

        public DeckCardAdapter(Context context, int textViewResourceId,
                           ArrayList<Card> cards) {
            super(context, textViewResourceId, cards);
            this.cards = cards;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.deck_card_list_item, null);
            }

            Card card = cards.get(position);

            TextView name = (TextView) view.findViewById(R.id.name);
            TextView mana = (TextView) view.findViewById(R.id.mana_cost);
            TextView quantity = (TextView) view.findViewById(R.id.quantity);


            if (card != null) {
                if (card.getName() != null) {
                    if (card.getName().length() >= 85) {
                        name.setText(card.getName().substring(0, 49) + "...");
                    } else {
                        name.setText(card.getName());
                    }
                } else {
                    name.setText("placeholder");
                }

                if(card.getMana() != null) {
                    mana.setText(card.getMana());
                } else {
                    mana.setText("placeholder");
                }

                quantity.setText(String.valueOf(card.getQuantity()));
            }

            return view;
        }
    }
    private class RetrieveFeedTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

        protected String doInBackground(Void... urls) {
            try {
                Card card;
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                stringBuilder.append("[\n");

                for (int i = 0; i < cards.size(); i++) {
                    card = cards.get(i);

                    URL url = new URL(API_URL + card.getId());

                    Log.i("INFO", card.getId());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                    } finally {
                        urlConnection.disconnect();
                    }
                    if( i < cards.size() -1) {
                        stringBuilder.append(",\n");
                    }
                }

                stringBuilder.append("]");
                return stringBuilder.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }

            //Log.i("INFO", response);
            try {
                JSONArray jsonCards = (JSONArray) new JSONTokener(response).nextValue();

                for(int i=0; i<jsonCards.length(); i++) {

                    JSONObject jsonCard = jsonCards.getJSONObject(i);

                    Log.i("INFO", jsonCard.getString("id"));

                    String id = jsonCard.getString("id");
                    cards.get(i).setName(jsonCard.getString("name"));
                    cards.get(i).setMana(jsonCard.getString("cost"));
                }

                Collections.sort(cards, new Comparator<Card>() {
                    @Override
                    public int compare(Card o1, Card o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);

                adapter.notifyDataSetChanged();
                Log.i("INFO", "Adapter notified");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

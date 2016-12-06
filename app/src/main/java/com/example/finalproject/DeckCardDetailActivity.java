package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeckCardDetailActivity extends AppCompatActivity {

    static final String API_URL = "https://api.deckbrew.com/mtg/cards/";

    private String cardId;
    private int deckId;
    private ProgressBar progressBar;

    private TextView card_name;
    private TextView mana_cost;
    private TextView card_type;
    private TextView card_text;

    private TextView card_power;
    private TextView card_toughness;

    private TextView card_loyalty;

    private LinearLayout card_pnt;

    private Button update;
    private Button remove;

    private DatabaseHelper db;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_card_detail);

        Intent intent = getIntent();
        cardId = intent.getStringExtra("card_id");
        deckId = intent.getIntExtra("deck_id", -1);

        db = new DatabaseHelper(getApplicationContext());

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        card_name = (TextView) findViewById(R.id.card_name);
        mana_cost = (TextView) findViewById(R.id.mana_cost);
        card_text = (TextView) findViewById(R.id.card_text);
        card_type = (TextView) findViewById(R.id.card_type);
        card_power = (TextView) findViewById(R.id.card_power);
        card_toughness = (TextView) findViewById(R.id.card_toughness);
        card_loyalty = (TextView) findViewById(R.id.card_loyalty);

        card_pnt = (LinearLayout) findViewById(R.id.card_pnt);

        update = (Button) findViewById(R.id.btn_update);
        remove = (Button) findViewById(R.id.btn_remove);

        final Spinner dropdown = (Spinner)findViewById(R.id.qty_spinner);
        String[] items = new String[]{"1", "2", "3", "4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        new RetrieveFeedTask().execute();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQty = Integer.valueOf(dropdown.getSelectedItem().toString());

                db.updateCardDeck(deckId, cardId, newQty);
                DeckCardDetailActivity.this.finish();
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteCardDeck(deckId, cardId);
                DeckCardDetailActivity.this.finish();
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

    private class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(API_URL + cardId);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);

            Log.i("INFO", response);
            // TODO: check this.exception
            // TODO: do something with the feed

            try {
                JSONObject jsonCard = (JSONObject) new JSONTokener(response).nextValue();

                String name = jsonCard.getString("name");
                int cmc = jsonCard.getInt("cmc");
                String cost = jsonCard.getString("cost");

                JSONArray types = jsonCard.getJSONArray("types");
                String text = jsonCard.getString("text");


                String power = null;
                String toughness = null;
                String loyalty = null;

                try {
                    power = jsonCard.getString("power");
                    card_power.setText("Power: " + power);

                    toughness = jsonCard.getString("toughness");
                    card_toughness.setText("Toughness: " + toughness);
                } catch (JSONException e) {
                    card_pnt.setVisibility(View.GONE);
                    Log.i("INFO", "No Power/Toughness");
                }

                try {
                    loyalty = jsonCard.getString("loyalty");
                    card_loyalty.setText("Loyalty: " + loyalty);
                } catch (JSONException e) {
                    card_loyalty.setVisibility(View.GONE);
                    Log.i("INFO", "No Loyalty");
                }

                card_name.setText(name);
                mana_cost.setText(cost);

                String type;

                for(int i=0; i<types.length(); i++) {
                    type = types.get(i).toString();
                    card_type.append(type.substring(0, 1).toUpperCase() + type.substring(1));
                    if(i != types.length()-1) {
                        card_type.append(", ");
                    }
                }

                try {
                    JSONArray subtypes = jsonCard.getJSONArray("subtypes");

                    card_type.append(" - ");
                    String subType;

                    for (int i = 0; i < subtypes.length(); i++) {
                        subType = subtypes.get(i).toString();
                        card_type.append(subType.substring(0, 1).toUpperCase() + subType.substring(1));
                        if (i != subtypes.length() - 1) {
                            card_type.append(", ");
                        }
                    }
                } catch (JSONException e) {
                    Log.i("INFO", "No Subtypes");
                }


                card_text.setText(text);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

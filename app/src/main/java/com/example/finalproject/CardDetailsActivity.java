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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CardDetailsActivity extends AppCompatActivity {

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
    private LinearLayout hidden;

    private Spinner dropdown;

    private Button addToDeck;

    private ImageView cardImage;

    private DatabaseHelper db;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);

        Intent intent = getIntent();
        cardId = intent.getStringExtra("card_id");
        deckId = intent.getIntExtra("deck_id", -1);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        card_name = (TextView) findViewById(R.id.card_name);
        mana_cost = (TextView) findViewById(R.id.mana_cost);
        card_text = (TextView) findViewById(R.id.card_text);
        card_type = (TextView) findViewById(R.id.card_type);
        card_power = (TextView) findViewById(R.id.card_power);
        card_toughness = (TextView) findViewById(R.id.card_toughness);
        card_loyalty = (TextView) findViewById(R.id.card_loyalty);

        card_pnt = (LinearLayout) findViewById(R.id.card_pnt);

        cardImage = (ImageView) findViewById(R.id.cardImage) ;

        hidden = (LinearLayout) findViewById(R.id.add_to_deck);

        db = new DatabaseHelper(getApplicationContext());

        dropdown = (Spinner)findViewById(R.id.qty_spinner);
        String[] items = new String[]{"1", "2", "3", "4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        addToDeck = (Button) findViewById(R.id.btn_addThisCard);

        addToDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(dropdown.getSelectedItem().toString());
                db.createCardDeck((long) deckId, cardId, qty);
                Intent intent = new Intent(CardDetailsActivity.this, DeckListActivity.class);
                intent.putExtra("id", deckId);
                startActivity(intent);
                finish();
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

        new RetrieveFeedTask().execute();
    }


    private class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... urls) {
            // Do some validation here

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

                try {
                    JSONArray editions = jsonCard.getJSONArray("editions");

                    int maxMultiverseId = 0;
                    int recentEditionIndex = 0;
                    int compareId;

                    JSONObject edition;

                    for(int i=0; i<editions.length(); i++)
                    {
                        edition = editions.getJSONObject(i);

                        compareId = edition.getInt("multiverse_id");

                        if(compareId > maxMultiverseId) {
                            maxMultiverseId = compareId;
                            recentEditionIndex = i;
                        }
                    }
                    String imageURL = editions.getJSONObject(recentEditionIndex).getString("image_url");

                    Picasso.with(CardDetailsActivity.this)
                            .load(imageURL)
                            .into(cardImage);

                    cardImage.setScaleType(ImageView.ScaleType.FIT_XY);

                } catch (JSONException e) {
                    Log.i("INFO", "No image");
                }

                if(deckId != -1) {
                    hidden.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

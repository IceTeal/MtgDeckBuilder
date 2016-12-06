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
import android.widget.Toast;

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

public class CardResultActivity extends AppCompatActivity{

    static final String API_URL = "https://api.deckbrew.com/mtg/cards?";

    private ProgressBar progressBar;
    private String searchString;

    private ArrayList<Card> listItems = new ArrayList<Card>();
    private CardAdapter adapter;

    private int pageNumber;

    private Button btn_prev;
    private Button btn_next;
    private TextView tv_page;

    private int deckId;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_result);

        pageNumber = 0;

        tv_page = (TextView) findViewById(R.id.page_number);

        tv_page.setText(Integer.toString(pageNumber + 1));

        Intent intent = getIntent();
        searchString = intent.getStringExtra("searchString");
        deckId = intent.getIntExtra("deck_id", -1);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ListView listView = (ListView) findViewById(R.id.card_list);

        adapter = new CardAdapter(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        new RetrieveFeedTask().execute();

        btn_prev = (Button) findViewById(R.id.btn_prev);
        btn_prev.setVisibility(View.GONE);

        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber++;
                tv_page.setText(Integer.toString(pageNumber + 1));
                new RetrieveFeedTask().execute();
                adapter.notifyDataSetChanged();
            }
        });

        btn_prev.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber--;
                tv_page.setText(Integer.toString(pageNumber + 1));
                if(pageNumber == 0) {
                    v.setVisibility(View.GONE);
                }
                new RetrieveFeedTask().execute();
                adapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CardResultActivity.this, CardDetailsActivity.class);
                intent.putExtra("card_id", listItems.get(position).getId());
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

    private class CardAdapter extends ArrayAdapter<Card> {
        private ArrayList<Card> cards;

        public CardAdapter(Context context, int textViewResourceId,
                              ArrayList<Card> cards) {
            super(context, textViewResourceId, cards);
            this.cards = cards;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item, null);
            }

            Card card = cards.get(position);

            if (card != null) {
                TextView name = (TextView)view.findViewById(R.id.list_view_item);
                TextView mana = (TextView)view.findViewById(R.id.mana_cost);

                if (card.getName().length() >= 85) {
                    name.setText(card.getName().substring(0, 49) + "...");
                } else {
                    name.setText(card.getName());
                }
                mana.setText(card.getMana());
            }

            return view;
        }
    }

    private class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private boolean hasNext;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            if(pageNumber > 0) {
                btn_prev.setVisibility(View.VISIBLE);
            }
        }

        protected String doInBackground(Void... urls) {

            try {
                URL url = new URL(API_URL + searchString + "&page=" + pageNumber);

                Log.i("INFO", searchString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();

                    URL nextUrl = new URL(API_URL + searchString + "&page=" + (pageNumber + 1));
                    HttpURLConnection nextUrlConnection = (HttpURLConnection) nextUrl.openConnection();
                    BufferedReader nextBufferedReader = new BufferedReader(new InputStreamReader(nextUrlConnection.getInputStream()));

                    if(nextBufferedReader.readLine().equals("[]")) {
                        hasNext = false;
                    } else {
                        hasNext = true;
                    }

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
                listItems.clear();

                JSONArray jsonCards = (JSONArray) new JSONTokener(response).nextValue();

                for(int i=0; i<jsonCards.length(); i++) {

                    JSONObject jsonCard = jsonCards.getJSONObject(i);

                    String id = jsonCard.getString("id");
                    String name = jsonCard.getString("name");
                    String mana = jsonCard.getString("cost");

                    Card card = new Card(id, name, mana);

                    listItems.add(card);
                }

                Collections.sort(listItems, new Comparator<Card>() {
                    @Override
                    public int compare(Card o1, Card o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                adapter.notifyDataSetChanged();

                if(hasNext) {
                    btn_next.setVisibility(View.VISIBLE);
                } else {
                    btn_next.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(CardResultActivity.this, "No cards found.", Toast.LENGTH_LONG).show();
                CardResultActivity.this.finish();
            }
        }
    }
}

package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Bryce on 2016-11-30.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = DatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "fpDeckManager";

    //Table Names
    private static final String TABLE_DECKS = "decks";
    private static final String TABLE_CARDDECKS = "carddecks";

    //Common column names
    private static final String KEY_ID = "id";

    // Decks Table - column names
    private static final String KEY_NAME = "name";

    //CardDecks Table - column names
    private static final String KEY_CARD_ID = "card_id";
    private static final String KEY_DECK_ID = "deck_id";
    private static final String KEY_CARD_QTY = "quantity";

    //Table Create Statements

    //Deck table create statement
    private final String CREATE_TABLE_DECK =
            "CREATE TABLE " + TABLE_DECKS + "(" +
                    KEY_ID + " INTEGER PRIMARY KEY, " +
                    KEY_NAME + " TEXT" + ")";

    //CardDeck table create statement
    private final String CREATE_TABLE_CARDDECK =
            "CREATE TABLE " + TABLE_CARDDECKS + "(" +
                    KEY_ID + " INTEGER PRIMARY KEY, " +
                    KEY_CARD_ID + " TEXT," +
                    KEY_DECK_ID + " INTEGER," +
                    KEY_CARD_QTY + " INTEGER" + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creating required tables
        db.execSQL(CREATE_TABLE_DECK);
        db.execSQL(CREATE_TABLE_CARDDECK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DECKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDDECKS);

        // create new tables
        onCreate(db);
    }

    // ------------------------ "deck" table methods ----------------//
    /**
     * Creating a deck
     */
    public long createDeck(Deck deck) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, deck.getName());

        //insert row
        long deck_id = db.insert(TABLE_DECKS, null, values);

        return deck_id;
    }

    /**
     * get single deck
     */
    public Deck getDeck(long deck_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_DECKS + " WHERE "
                + KEY_ID + " = " + deck_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Deck deck = new Deck();
        deck.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        deck.setName((c.getString(c.getColumnIndex(KEY_NAME))));

        return deck;
    }

    /**
     * get all decks
     */
    public ArrayList<Deck> getAllDecks() {
        ArrayList<Deck> decks = new ArrayList<Deck>();
        String selectQuery = "SELECT * FROM " + TABLE_DECKS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Deck d = new Deck();
                d.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                d.setName(c.getString(c.getColumnIndex(KEY_NAME)));

                // adding to tags list
                decks.add(d);
            } while (c.moveToNext());
        }
        return decks;
    }

    /**
     * getting deck count
     */
    public int getDeckCount() {
        String countQuery = "SELECT  * FROM " + TABLE_DECKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a deck
     */
    public int updateDeck(Deck deck) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, deck.getName());

        return db.update(TABLE_DECKS, values, KEY_ID + " = ?",
                new String[] {String.valueOf(deck.getId())});
    }

    /**
     * Deleting a deck
     */
    public void deleteDeck(long deck_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DECKS, KEY_ID + " = ?",
                new String[] { String.valueOf(deck_id) });
    }

    // ------------------------ "carddeck" table methods ----------------//
    public long createCardDeck(long deck_id, String card_id, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CARD_ID, card_id);
        values.put(KEY_DECK_ID, deck_id);
        values.put(KEY_CARD_QTY, quantity);

        long id = db.insert(TABLE_CARDDECKS, null, values);

        return id;
    }

    public int updateCardDeck(long deck_id, String card_id, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CARD_QTY, quantity);

        // updating row
        return db.update(TABLE_CARDDECKS, values, KEY_DECK_ID + " = ? AND " + KEY_CARD_ID + " = ?" ,
                new String[] { String.valueOf(deck_id), card_id});

    }

    public void deleteCardDeck(long deck_id, String card_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CARDDECKS, KEY_DECK_ID + " = ? AND " + KEY_CARD_ID + " = ?",
                new String[] { String.valueOf(deck_id), card_id });
    }

    /**
     * get all cards in deck
     */
    public ArrayList<Card> getAllCardsInDeck(long deck_id) {
        ArrayList<Card> cards = new ArrayList<Card>();
        String selectQuery = "SELECT * FROM " + TABLE_CARDDECKS + " WHERE " + KEY_DECK_ID + " = " + deck_id;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Card card = new Card();
                card.setId(c.getString(c.getColumnIndex(KEY_CARD_ID)));
                card.setQuantity(c.getInt(c.getColumnIndex(KEY_CARD_QTY)));

                // adding to card list
                cards.add(card);
            } while (c.moveToNext());
        }
        return cards;
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}

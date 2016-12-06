package com.example.finalproject;

/**
 * Created by Bryce on 2016-11-30.
 */

public class Deck {
    private int id;
    private String name;

    public Deck() {

    }

    public Deck(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

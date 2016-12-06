package com.example.finalproject;


public class Card {
    private String id;
    private String name;
    private String mana;
    private int quantity;

    public Card() {

    }

    public Card(String id, String name, String mana) {
        this.id = id;
        this.name = name;
        this.mana = mana;
        this.quantity = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName(){ return name; }
    public void setName(String name) { this.name = name; }

    public String getMana() { return mana; }
    public void setMana(String mana) { this.mana = mana; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity;}
}
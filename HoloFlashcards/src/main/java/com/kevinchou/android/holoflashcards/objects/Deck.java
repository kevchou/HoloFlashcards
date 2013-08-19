package com.kevinchou.android.holoflashcards.objects;

public class Deck {
	
	String deckName;
	String dateCreated;
	int deckColor;
	int deckId = -1;
	
	public Deck(String deckName, String timeCreated, int deckColor) {
		this.deckName = deckName;
		this.dateCreated = timeCreated;
		this.deckColor = deckColor;
	}

	public int getDeckId() {
		return deckId;
	}
	
	public void setDeckId(int deckId) {
		this.deckId = deckId; 
	}
	
	public String getDeckName() {
		return deckName;
	}

	public void setDeckName(String deckName) {
		this.deckName = deckName;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public int getDeckColor() {
		return deckColor;
	}

	public void setDeckColor(int deckColor) {
		this.deckColor = deckColor;
	}
	
	
	
}

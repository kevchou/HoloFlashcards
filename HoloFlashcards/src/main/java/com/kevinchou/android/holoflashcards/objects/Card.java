package com.kevinchou.android.holoflashcards.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {
	
	private String deckName;
	private String cardFront;
	private String cardBack;
	private String cardId;
	
	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public Card(String deckName, String cardFront, String cardBack) {
		this.deckName = deckName;
		this.cardFront = cardFront;
		this.cardBack = cardBack;
	}

	public String getDeckName() {
		return deckName;
	}

	public void setDeckName(String deckName) {
		this.deckName = deckName;
	}

	public String getCardFront() {
		return cardFront;
	}

	public void setCardFront(String cardFront) {
		this.cardFront = cardFront;
	}

	public String getCardBack() {
		return cardBack;
	}

	public void setCardBack(String cardBack) {
		this.cardBack = cardBack;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	private Card(Parcel in) {
		this.deckName = in.readString();
		this.cardFront = in.readString();
		this.cardBack = in.readString();
		this.cardId = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(deckName);
		dest.writeString(cardFront);
		dest.writeString(cardBack);
		dest.writeString(cardId);	
	}

	   public static final Creator<Card> CREATOR = new Creator<Card>() {
	        public Card createFromParcel(Parcel in) {
	            return new Card(in);
	        }

	        public Card[] newArray(int size) {
	            return new Card[size];
	        }
	    };
}

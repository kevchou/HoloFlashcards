package com.kevinchou.android.holoflashcards;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.kevinchou.android.holoflashcards.objects.Card;
import com.kevinchou.android.holoflashcards.objects.Deck;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBTools extends SQLiteOpenHelper {

	// CONSTANTS
	public static final String DATABASE_NAME = "flashcards";

	public static final String TABLE_DECKS = "decksTable";
	public static final String TABLE_CARDS = "cardsTable";

	public static final String DECK_ID = "_deckId";
	public static final String DECK_NAME = "deckName";
	public static final String DECK_CREATED_DATE = "deckCreatedDate";
	public static final String DECK_ACCENT_COLOR = "deckAccentColor";

	public static final String CARD_ID = "_cardId";
	public static final String CARD_FRONT = "cardFront";
	public static final String CARD_BACK = "cardBack";
	
	public static final int[] ACCENT_COLORS =
		{R.color.dark_actionbar, R.color.blue, R.color.purple, R.color.green, R.color.orange, R.color.red} ;
	
	Context mContext;

	// takes position of accentcolor array, returns color int
	public static int getColor(int position, Context context) {
		return context.getResources().getColor(ACCENT_COLORS[position]);
	}
	
	
	// Constructor
	public DBTools(Context context) {
		super(context, DATABASE_NAME, null, 1);
		mContext = context;
	}		

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createDecksTableQuery = "CREATE TABLE " + TABLE_DECKS + "("
				+ DECK_ID + " INTEGER PRIMARY KEY," + DECK_NAME + " TEXT,"
				+ DECK_CREATED_DATE + " TEXT," + DECK_ACCENT_COLOR
				+ " INTEGER)";

		String createCardsTableQuery = "CREATE TABLE " + TABLE_CARDS + "("
				+ CARD_ID + " INTEGER PRIMARY KEY," + DECK_NAME + " TEXT,"
				+ CARD_FRONT + " TEXT," + CARD_BACK + " TEXT)";

		db.execSQL(createDecksTableQuery);
		db.execSQL(createCardsTableQuery);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		String deckQuery = "DROP TABLE IF EXISTS " + TABLE_DECKS;
		String cardQuery = "DROP TABLE IF EXISTS " + TABLE_CARDS;

		db.execSQL(deckQuery);
		db.execSQL(cardQuery);

		onCreate(db);
	}

	// WARNING: DELETES EVERYTHING IN DATABASE
	public void deleteEverything() {
		SQLiteDatabase db = this.getWritableDatabase();

		db.delete(TABLE_DECKS, null, null);
		db.delete(TABLE_CARDS, null, null);

		db.close();
	}

	// Runs when user opens app for first time.
	// Sets up initial (help) cards
	public void firstRun() {
		deleteEverything();
		
		Resources res = mContext.getResources();
		
		// Sets up initial help card
		String[] firstTimeArray = res.getStringArray(R.array.first_time_run_array);
		
		addNewDeck( new Deck(res.getString(R.string.first_time_run), DeckActivity.getTime(), 0) );
		addNewCard(new Card(res.getString(R.string.first_time_run), firstTimeArray[0], firstTimeArray[1]));
		addNewCard(new Card(res.getString(R.string.first_time_run), firstTimeArray[2], firstTimeArray[3]));
		addNewCard(new Card(res.getString(R.string.first_time_run), firstTimeArray[4], firstTimeArray[5]));
		addNewCard(new Card(res.getString(R.string.first_time_run), firstTimeArray[6], firstTimeArray[7]));

		
		// Sets up biology Example
		String[] biologyExample = res.getStringArray(R.array.biology_example_array);
		
		addNewDeck( new Deck(res.getString(R.string.biology_example_title), DeckActivity.getTime(), 1) );
		for (int i=0; i < biologyExample.length; i = i+2) {
			addNewCard(new Card(res.getString(R.string.biology_example_title), biologyExample[i], biologyExample[i+1]));
		}
	}

	public int getDeckSize(Deck deck) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String query = "SELECT COUNT(*) FROM " + TABLE_CARDS + 
				" WHERE " + DECK_NAME + "= '" + deck.getDeckName() + "'";
		
		Cursor cursor = db.rawQuery(query, null);
		
		int count = 0;
		
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
			cursor.close();
		}
		
		db.close();
		
		return count;
	}
	

	
	public ArrayList<Deck> getAllDecks() {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_DECKS, new String[] {DECK_ID, DECK_NAME, DECK_CREATED_DATE, DECK_ACCENT_COLOR}, null, null, null, null, null);
		
		ArrayList<Deck> decksToReturn = new ArrayList<Deck>();
		
		if (cursor.moveToFirst()) {
			do {
				
				Deck tempDeck = new Deck(cursor.getString(1), cursor.getString(2), cursor.getInt(3));
				tempDeck.setDeckId(cursor.getInt(0));
				
				decksToReturn.add(tempDeck);

			} while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return decksToReturn;
	}
	
	public long addNewDeck(Deck deck) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DECK_NAME, deck.getDeckName());
		values.put(DECK_CREATED_DATE, deck.getDateCreated());
		values.put(DECK_ACCENT_COLOR, deck.getDeckColor());
		
		long deckId = db.insert(TABLE_DECKS, null, values);
		
		db.close();
		
		return deckId;
	}

	public void removeDeck(Deck deck) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.delete(TABLE_DECKS, DECK_ID + " = ?", new String[] {String.valueOf(deck.getDeckId())});
		db.delete(TABLE_CARDS, DECK_NAME + " = ?", new String[] {String.valueOf(deck.getDeckName())});
		db.close();
	}
	
	public void updateDeck(Deck oldDeck, Deck newDeck) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues deckValues = new ContentValues();
		deckValues.put(DECK_NAME, newDeck.getDeckName());
		db.update(TABLE_CARDS, deckValues, DECK_NAME + " = ?", new String[] {oldDeck.getDeckName()} );
		
		deckValues.put(DECK_CREATED_DATE, newDeck.getDateCreated());
		deckValues.put(DECK_ACCENT_COLOR, newDeck.getDeckColor());
		db.update(TABLE_DECKS, deckValues, DECK_ID + " = ?", new String[] {String.valueOf(oldDeck.getDeckId())} );
		
	}
	
	public void removeCard(Card card) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.delete(TABLE_CARDS, CARD_ID + " = ?", new String[] {card.getCardId()});
		db.close();
	}
	
	public ArrayList<Card> getCardsFrom(String deckName) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		ArrayList<Card> cardArray = new ArrayList<Card>();
		
		Cursor cursor = db.query(TABLE_CARDS, new String[] {CARD_ID, DECK_NAME, CARD_FRONT,  CARD_BACK}, DECK_NAME + "= ?", new String[] {deckName}, null, null, null);
		
		if (cursor.moveToFirst()) {
			do {
				Card card = new Card(cursor.getString(1), cursor.getString(2), cursor.getString(3));
				card.setCardId(cursor.getString(0));
				cardArray.add(card);
				
			} while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return cardArray;
	}
	
	public long addNewCard(Card card) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DECK_NAME, card.getDeckName());
		values.put(CARD_FRONT, card.getCardFront());
		values.put(CARD_BACK, card.getCardBack());
		
		long cardId = db.insert(TABLE_CARDS, null, values);
		
		db.close();
		
		return cardId;
	}
	
	public void editCard(Card oldCard, Card newCard) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues cardValues = new ContentValues();
		cardValues.put(DECK_NAME, newCard.getDeckName());
		cardValues.put(CARD_FRONT, newCard.getCardFront());
		cardValues.put(CARD_BACK, newCard.getCardBack());
		
		
		
		db.update(TABLE_CARDS, cardValues, CARD_ID + " = ?", new String[] {oldCard.getCardId()});
		db.close();
	}
	
	
	

	final String SEP = ",";
	
	public Boolean backupDatabaseCSV() {
		
		boolean returnCode = true;

		ArrayList<Deck> deckArray = getAllDecks();

		for (Deck deck : deckArray) {
			boolean tempReturnCode = exportDeck(deck.getDeckName());
			if (returnCode) returnCode = tempReturnCode;
		}
		
		return returnCode;
	}
	
	public boolean exportDeck(String deckName) {
		boolean returnCode = true;
		
		String csvValues = "";
		SQLiteDatabase db = this.getReadableDatabase();
		
		try {
			// Path to export to; in the app folder. Makes the folder if it
			// doesn't exist
			File path = new File(Environment.getExternalStorageDirectory(), mContext.getResources().getString(R.string.app_name));
			if (!path.exists()) path.mkdir();

			// File name to save it is
			File outFile = new File(path, deckName);

			FileWriter fileWriter = new FileWriter(outFile);
			BufferedWriter out = new BufferedWriter(fileWriter);

			Cursor cursor = db.query(TABLE_CARDS, new String[] {CARD_FRONT, CARD_BACK }, DECK_NAME + " = ?",
					new String[] { deckName }, null, null, null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						csvValues = cursor.getString(0) + SEP + cursor.getString(1);
						csvValues += "\n";
						
						out.write(csvValues);
					} while (cursor.moveToNext());
				}
				cursor.close();
			}

			out.close();
		} catch (IOException e) {
			returnCode = false;
			Log.d("WRITE", "IOException: " + e.getMessage());
		}
		db.close();
		return returnCode;
	}
	
}

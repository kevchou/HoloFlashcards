package com.kevinchou.android.holoflashcards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.kevinchou.android.holoflashcards.DeckAddEditDialogFragment.DeckAddEditHandler;
import com.kevinchou.android.holoflashcards.objects.Card;
import com.kevinchou.android.holoflashcards.objects.Deck;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeckActivity extends ActionBarActivity implements DeckAddEditHandler {

	// For checking if first time running
	private static String				KEY_FIRST_RUN	= "";
	private SharedPreferences			sharedPreferences;
	private SharedPreferences.Editor	editor;

	Context								mContext;
	ArrayList<Deck>						mDecks;
	DeckListAdapter						mAdapter;
	ActionMode							mActionMode;

	DBTools								mDbTools		= new DBTools(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_deck);
		ListView listView = (ListView) findViewById(R.id.deckList);

		// Action Bar Color final 
		// ActionBar actionBar = getSupportActionBar(); 
		// Drawable actionBarColor = new ColorDrawable(Color.argb(230, 228, 228, 228));
		// actionBar.setBackgroundDrawable(actionBarColor);

		mContext = this;

		sharedPreferences = getPreferences(MODE_PRIVATE);
		// first time running app
		if (!sharedPreferences.contains("KEY_FIRST_RUN")) {
			KEY_FIRST_RUN = "something";

			// Set ups initial decks on first run
			mDbTools.firstRun();

			editor = sharedPreferences.edit();
			editor.putString("KEY_FIRST_RUN", KEY_FIRST_RUN);
			editor.commit();
		}

		mDecks = mDbTools.getAllDecks();

		mAdapter = new DeckListAdapter(this, R.layout.item_deck, mDecks);

		listView.setAdapter(mAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				if (mActionMode == null) {
					// no items selected in CAB, so perform item click actions
					Intent intent = new Intent(mContext, CardActivity.class);
					intent.putExtra(DBTools.DECK_NAME, mDecks.get(position).getDeckName());
					intent.putExtra(DBTools.DECK_ACCENT_COLOR, mDecks.get(position).getDeckColor());
					startActivity(intent);

				} else
					// add or remove selection for current list item
					onListItemSelect(position);
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemSelect(position);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mActionMode != null) {
					onListItemSelect(position);
				}

				return true;
			}
		});
	}

	private void onListItemSelect(int position) {
		mAdapter.toggleSelection(position);
		boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

		if (hasCheckedItems && mActionMode == null)
			// there are some selected items, start the actionMode
			mActionMode = startSupportActionMode(new ActionMode.Callback() {

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					final SparseBooleanArray selected = mAdapter.getSelectedIds();

					switch (item.getItemId())
						{

						case R.id.cab_deck_edit:
							// show dialog
							FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
							DialogFragment newFragment = DeckAddEditDialogFragment.newInstance(selected.keyAt(0));
							newFragment.show(ft, "dialog");

							// finish actionmode after clicking edit button
							mode.finish();
							return true;

						case R.id.cab_deck_delete:
							// retrieve selected items and delete them out
							Builder builder = new Builder(mContext);
							builder.setTitle(R.string.delete_decks);
							builder.setMessage(getResources().getString(R.string.delete_deck_message)
									+ " "
									+ (selected.size() == 1 ? "\"" + mAdapter.getItem(selected.keyAt(0)).getDeckName()
											+ "\"?" : selected.size() + " decks?"));

							builder.setNegativeButton(R.string.cancel, null);
							builder.setPositiveButton(R.string.delete, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									for (int i = (selected.size() - 1); i >= 0; i--) {
										if (selected.valueAt(i)) {
											Deck selectedItem = mAdapter.getItem(selected.keyAt(i));
											mAdapter.remove(selectedItem);
										}
									}
								}
							});
							builder.create().show();

							mode.finish(); // Action picked, so close the CAB
							return true;
						default:
							return false;
						}
				}

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					// inflate contextual menu
					mode.getMenuInflater().inflate(R.menu.cab_deck, menu);
					return true;
				}

				@Override
				public void onDestroyActionMode(ActionMode arg0) {
					mAdapter.removeSelection();
					mActionMode = null;
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					MenuItem item = menu.findItem(R.id.cab_deck_edit);
					if (mAdapter.getSelectedCount() == 1) {
						item.setVisible(true);
					} else {
						item.setVisible(false);
					}
					return true;
				}

			});
		else if (!hasCheckedItems && mActionMode != null)
			// there no selected items, finish the actionMode
			mActionMode.finish();

		if (mActionMode != null) {
			mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount()) + " selected");
			mActionMode.invalidate();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.deck, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
			{
			case R.id.menu_deck_add:
				// mAdapter.add(new Deck("Test Add", String.format("%.8s",
				// Math.random()), Color.CYAN));
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				DialogFragment newFragment = DeckAddEditDialogFragment.newInstance();
				newFragment.show(ft, "dialog");
				return true;
				
			case R.id.menu_help:
				Builder builder = new Builder(this);
				builder.setTitle("Help");
				builder.setView(LayoutInflater.from(this).inflate(R.layout.help_popup_dialog, null));
				builder.setPositiveButton("Got it!", null);
				builder.create().show();

				return true;
			case R.id.menu_import:
				Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
				fileintent.setType("gagt/sdf");
				try {
					startActivityForResult(fileintent, 1);
				} catch (ActivityNotFoundException e) {
					Log.e("tag", "No activity can handle picking a file. Showing alternatives.");
				}

				return true;
			case R.id.menu_export_all:
				if (mDbTools.backupDatabaseCSV())
					Toast.makeText(getBaseContext(), "Success!", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getBaseContext(), "Fail!", Toast.LENGTH_SHORT).show();
				return true;
			}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11.
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Fix no activity available
		if (data == null)
			return;
		switch (requestCode)
			{
			case 1:
				if (resultCode == RESULT_OK) {
					// Find path of imported file
					mImportedFilePath = data.getData().getPath();

					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					DialogFragment newFragment = DeckAddEditDialogFragment.newInstance(mImportedFilePath);
					newFragment.show(ft, "dialog");
				}
			}
	}

	public String	mImportedFilePath	= "";
	public String	mImportedDeckName	= "";

	@Override
	public void AddCardsToImportedDeck(String filePath) {
		Log.d("TEST", "method called");

		final File inFile = new File(filePath);

		try {
			BufferedReader in = new BufferedReader(new FileReader(inFile));
			String reader = "";
			while ((reader = in.readLine()) != null) {
				String[] rowData = reader.split(",");

				String cardFront = "";
				String cardBack = "";

				try {
					cardFront = rowData[0];
				} catch (Exception e) {
				}

				try {
					cardBack = rowData[1];
				} catch (Exception e) {
				}

				mDbTools.addNewCard(new Card(mImportedDeckName, cardFront, cardBack));
			}
			in.close();

		} catch (IOException e) {
			Log.d("WRITE", "IOException: " + e.getMessage());
		}
		mImportedDeckName = "";

	}

	public static String getTime() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("d/MMM/yyyy");
		return df.format(c.getTime());
	}

	public class DeckListAdapter extends ArrayAdapter<Deck> {
		private SparseBooleanArray	mSelectedItemsIds;

		public DeckListAdapter(Context context, int resId, ArrayList<Deck> decks) {
			super(context, resId, decks);
			mSelectedItemsIds = new SparseBooleanArray();
		}

		private class ViewHolder {
			TextView	deckNameTextView;
			TextView	deckSizeTextView;
			TextView	deckCreatedDateTextView;
			View		accentColorView;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.item_deck, null);

				holder = new ViewHolder();
				holder.deckNameTextView = (TextView) convertView.findViewById(R.id.deckNameTextView);
				holder.deckSizeTextView = (TextView) convertView.findViewById(R.id.deckSizeTextView);
				holder.deckCreatedDateTextView = (TextView) convertView.findViewById(R.id.deckCreatedDateTextView);
				holder.accentColorView = (View) convertView.findViewById(R.id.accentColorView);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Deck deck = getItem(position);

			int deckSize = mDbTools.getDeckSize(deck);

			holder.deckNameTextView.setText(deck.getDeckName());
			holder.deckSizeTextView.setText(String.format("%s %s", deckSize, deckSize == 1 ? "Card" : "Cards"));
			holder.deckCreatedDateTextView.setText("Created: " + deck.getDateCreated());
			holder.accentColorView.setBackgroundColor(DBTools.getColor(deck.getDeckColor(), mContext));

			convertView.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4 : Color.TRANSPARENT);

			return convertView;
		}

		@Override
		public void add(Deck deck) {
			int newDeckId = (int) mDbTools.addNewDeck(deck);

			deck.setDeckId(newDeckId);
			mDecks.add(deck);

			notifyDataSetChanged();
		}

		@Override
		public void remove(Deck deck) {
			mDecks.remove(deck);
			mDbTools.removeDeck(deck);
			notifyDataSetChanged();
		}

		public void update(int position, Deck newDeck) {

			Deck oldDeck = mDecks.get(position);

			mDecks.set(position, newDeck);
			mDbTools.updateDeck(oldDeck, newDeck);
			notifyDataSetChanged();
		}

		public void toggleSelection(int position) {
			selectView(position, !mSelectedItemsIds.get(position));
		}

		public void removeSelection() {
			mSelectedItemsIds = new SparseBooleanArray();
			notifyDataSetChanged();
		}

		public void selectView(int position, boolean value) {
			if (value)
				mSelectedItemsIds.put(position, value);
			else
				mSelectedItemsIds.delete(position);

			notifyDataSetChanged();
		}

		public int getSelectedCount() {
			return mSelectedItemsIds.size();
		}

		public SparseBooleanArray getSelectedIds() {
			return mSelectedItemsIds;
		}
	}

	@Override
	public Deck getDeck(int position) {
		return mDecks.get(position);
	}

	@Override
	public boolean deckArrayContains(String deckName) {
		boolean nameAlreadyExists = false;

		for (Deck deck : mDecks) {
			if (deck.getDeckName().equals(deckName)) {
				nameAlreadyExists = true;
				break;
			}
		}
		return nameAlreadyExists;
	}

}
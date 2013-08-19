package com.kevinchou.android.holoflashcards;

import java.util.ArrayList;

import com.kevinchou.android.holoflashcards.CardAddEditDialogFragment.CardAddEditHandler;
import com.kevinchou.android.holoflashcards.ListViewFragment.ListViewCardHandler;
import com.kevinchou.android.holoflashcards.QuizStartDialog.QuizStartDialogHandler;
import com.kevinchou.android.holoflashcards.objects.Card;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CardListActivity extends ActionBarActivity implements ListViewCardHandler, CardAddEditHandler, QuizStartDialogHandler {

	TextView		noCardListTextView;
	DBTools			mDbTools	= new DBTools(this);
	String			mDeckName;
	ArrayList<Card>	mCardArray;
	int				mColorIdx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get arguments
		Bundle args = getIntent().getExtras();
		mDeckName = args.getString(DBTools.DECK_NAME);
		mColorIdx = args.getInt(DBTools.DECK_ACCENT_COLOR);
		int deckColor = DBTools.getColor(mColorIdx, this); // get color int

		Log.d("TEST", "asdfasd" + mColorIdx);
		// Action Bar Color
		Drawable accentColorDrawable = new ColorDrawable(deckColor);
		Drawable actionBarBottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
		LayerDrawable ld = new LayerDrawable(new Drawable[] { accentColorDrawable, actionBarBottomDrawable });

		// Get cardArray
		mCardArray = mDbTools.getCardsFrom(mDeckName);

		// inflate layout containing the list fragment
		setContentView(R.layout.activity_card_list);
		noCardListTextView = (TextView) findViewById(R.id.noCardListTextView);

		if (mCardArray.isEmpty())
			noCardListTextView.setVisibility(View.VISIBLE);

		// Action Bar Stuff
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(mDeckName);
		actionBar.setBackgroundDrawable(ld);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.card_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
			{
			case android.R.id.home:
				navigateUpToCardView();
				return true;
			case R.id.menu_card_add:
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				DialogFragment newFragment = CardAddEditDialogFragment.newInstance(mDeckName);
				newFragment.show(ft, "dialog");
				return true;
			case R.id.menu_card_view_as_card:
				Intent viewListIntent = new Intent(this, CardActivity.class);
				viewListIntent.putExtra(DBTools.DECK_NAME, mDeckName);
				viewListIntent.putExtra(DBTools.DECK_ACCENT_COLOR, mColorIdx);
				startActivity(viewListIntent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				return true;
			case R.id.menu_card_startquiz:
				ListViewFragment frag = (ListViewFragment) getSupportFragmentManager().findFragmentById(
						R.id.cardAsListFragment);
				if (!frag.mCardArray.isEmpty()) {
					FragmentTransaction fragTran = getSupportFragmentManager().beginTransaction();
					DialogFragment df = new QuizStartDialog();
					df.show(fragTran, "quiz_dialog");
				} else {
					Toast.makeText(getApplicationContext(), R.string.quiz_start_no_card, Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.menu_card_list_exportdeck:
				if (mDbTools.exportDeck(mDeckName)) {
					Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		return super.onOptionsItemSelected(item);
	}

	public void navigateUpToCardView() {
		Intent viewCardsIntent = new Intent(this, CardActivity.class);
		viewCardsIntent.putExtra(DBTools.DECK_NAME, mDeckName);
		viewCardsIntent.putExtra(DBTools.DECK_ACCENT_COLOR, mColorIdx);
		viewCardsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(viewCardsIntent);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onBackPressed() {
		navigateUpToCardView();
	}

	// methods for interacting with list fragment
	@Override
	public ArrayList<Card> getCardArray() {
		return mCardArray;
	}

	@Override
	public boolean listSelectable() {
		return true;
	}

	@Override
	public DBTools getDbTools() {
		return mDbTools;
	}

	@Override
	public CardListActivity getActivity() {
		return this;
	}

	// Add Edit card interface methods
	@Override
	public Card getCard(int position) {
		ListViewFragment frag = (ListViewFragment) getSupportFragmentManager()
				.findFragmentById(R.id.cardAsListFragment);
		return frag.mCardArray.get(position);

	}

	@Override
	public void addCard(Card card) {
		ListViewFragment frag = (ListViewFragment) getSupportFragmentManager()
				.findFragmentById(R.id.cardAsListFragment);
		frag.mAdapter.add(card);
		if (noCardListTextView.getVisibility() == View.VISIBLE)
			noCardListTextView.setVisibility(View.GONE);
	}

	@Override
	public void editCard(Card oldCard, Card newCard) {
		ListViewFragment frag = (ListViewFragment) getSupportFragmentManager()
				.findFragmentById(R.id.cardAsListFragment);
		frag.mAdapter.editCard(oldCard, newCard);
	}

	@Override
	public String getDeckName() {
		return mDeckName;
	}

	@Override
	public int getColorIdx() {
		return mColorIdx;
	}

}

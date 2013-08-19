package com.kevinchou.android.holoflashcards;

import java.util.ArrayList;

import com.kevinchou.android.holoflashcards.CardAddEditDialogFragment.CardAddEditHandler;
import com.kevinchou.android.holoflashcards.QuizStartDialog.QuizStartDialogHandler;
import com.kevinchou.android.holoflashcards.objects.Card;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class CardActivity extends ActionBarActivity implements CardAddEditHandler, QuizStartDialogHandler {

	DBTools				mDbTools	= new DBTools(this);
	String				mDeckName;
	int					mDeckColor;
	int					mColorIdx;
	ArrayList<Card>		mCardArray;
	MyViewPagerAdapter	mAdapter;
	ViewPager			mCardPager;
	TextView			noCardsTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card);
		noCardsTextView = (TextView) findViewById(R.id.noCardsTextView);

		// get and set up arguments
		Bundle args = getIntent().getExtras();
		mDeckName = args.getString(DBTools.DECK_NAME);
		mColorIdx = args.getInt(DBTools.DECK_ACCENT_COLOR);
		mDeckColor = DBTools.getColor(mColorIdx, this); // get color int

		// Action Bar Color
		Drawable accentColorDrawable = new ColorDrawable(mDeckColor);
		Drawable actionBarBottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
		LayerDrawable ld = new LayerDrawable(new Drawable[] { accentColorDrawable, actionBarBottomDrawable });

		// Action Bar Stuff
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(mDeckName);
		actionBar.setBackgroundDrawable(ld);

		mCardArray = mDbTools.getCardsFrom(mDeckName);

		if (mCardArray.isEmpty())
			noCardsTextView.setVisibility(View.VISIBLE);

		mAdapter = new MyViewPagerAdapter(getSupportFragmentManager());

		mCardPager = (ViewPager) findViewById(R.id.cardPager);

		mCardPager.setAdapter(mAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.card, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
			{
			case android.R.id.home:
				navigateUpToDecks();
				return true;
			case R.id.menu_card_add:
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				DialogFragment newFragment = CardAddEditDialogFragment.newInstance(mDeckName);
				newFragment.show(ft, "dialog");
				return true;
			case R.id.menu_card_view_as_list:
				Intent viewListIntent = new Intent(this, CardListActivity.class);
				viewListIntent.putExtra(DBTools.DECK_NAME, mDeckName);
				viewListIntent.putExtra(DBTools.DECK_ACCENT_COLOR, mColorIdx);
				startActivity(viewListIntent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				return true;
			case R.id.menu_card_startquiz:
				if (!mCardArray.isEmpty()) {
					
					FragmentTransaction fragTran = getSupportFragmentManager().beginTransaction();
					DialogFragment quizDialog = new QuizStartDialog();
					quizDialog.show(fragTran, "quiz_dialog");
					
				} else {
					Toast.makeText(getApplicationContext(), R.string.quiz_start_no_card, Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.menu_card_exportdeck:
				if (mDbTools.exportDeck(mDeckName)) {
					Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		return super.onOptionsItemSelected(item);
	}

	public void navigateUpToDecks() {
		Intent viewDecksIntent = new Intent(this, DeckActivity.class);
		viewDecksIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(viewDecksIntent);
	}

	@Override
	public void onBackPressed() {
		navigateUpToDecks();
	}




	public static class CardFragment extends Fragment {

		boolean	animationOver	= true;

		public static CardFragment newInstance(Card card, int deckColor, int position, int deckSize) {
			CardFragment cardFrag = new CardFragment();

			Bundle args = new Bundle();
			args.putString(DBTools.DECK_NAME, card.getDeckName());
			args.putString(DBTools.CARD_FRONT, card.getCardFront());
			args.putString(DBTools.CARD_BACK, card.getCardBack());
			args.putInt(DBTools.DECK_ACCENT_COLOR, deckColor);
			args.putInt("CARD_POSITION", position);
			args.putInt("DECK_SIZE", deckSize);

			cardFrag.setArguments(args);

			return cardFrag;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			// The last two arguments ensure LayoutParams are inflated properly.
			final View view = inflater.inflate(R.layout.fragment_card, container, false);

			TextView cardPageTextView = (TextView) view.findViewById(R.id.cardPageTextView);
			TextView cardFrontTextView = (TextView) view.findViewById(R.id.cardFrontTextView);
			TextView cardBackTextView = (TextView) view.findViewById(R.id.cardBackTextView);
			View accentColor = (View) view.findViewById(R.id.accentColor);
			View accentColor2 = (View) view.findViewById(R.id.accentColor2);
			ImageButton editCardButton = (ImageButton) view.findViewById(R.id.editCardButton);
			ImageButton deleteCardButton = (ImageButton) view.findViewById(R.id.deleteCardButton);

			// For setting alpha of view
			AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
			alpha.setDuration(0); // Make animation instant
			alpha.setFillAfter(true); // Tell it to persist after animation end
			accentColor.startAnimation(alpha);
			
			alpha = new AlphaAnimation(0.2F, 0.2F);
			alpha.setDuration(0); // Make animation instant
			alpha.setFillAfter(true); // Tell it to persist after animation end
			accentColor2.startAnimation(alpha);

			
			
			Bundle args = getArguments();
			final int cardPosition = args.getInt("CARD_POSITION");
			final int deckSize = args.getInt("DECK_SIZE");

			// Set up all the text in the Card
			cardPageTextView.setText(String.format("%s/%s", cardPosition + 1, deckSize));
			cardFrontTextView.setText(args.getString(DBTools.CARD_FRONT));
			cardBackTextView.setText(args.getString(DBTools.CARD_BACK));

			// Set the accent color
			accentColor.setBackgroundColor(args.getInt(DBTools.DECK_ACCENT_COLOR));
			accentColor2.setBackgroundColor(args.getInt(DBTools.DECK_ACCENT_COLOR));

			// Set buttons and flip
			deleteCardButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((CardActivity) getActivity()).mAdapter.deleteCard(cardPosition);
				}
			});

			editCardButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
					DialogFragment newFragment = CardAddEditDialogFragment.newInstance(cardPosition);
					newFragment.show(ft, "dialog");
				}
			});

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View cardFront = (View) view.findViewById(R.id.cardFrontLayout);
					View cardBack = (View) view.findViewById(R.id.cardBackLayout);

					FlipAnimation flipAnimation = new FlipAnimation(cardFront, cardBack);

					flipAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							animationOver = false;
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							animationOver = true;
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}
					});

					if (cardFront.getVisibility() == View.GONE)
						flipAnimation.reverse();

					if (animationOver)
						view.startAnimation(flipAnimation);
				}
			});

			return view;
		}

	}

	public class MyViewPagerAdapter extends FragmentStatePagerAdapter {
		public MyViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment;
			if (!mCardArray.isEmpty())
				fragment = CardFragment.newInstance(mCardArray.get(i), mDeckColor, i, mCardArray.size());
			else
				fragment = CardFragment.newInstance(new Card(mDeckName, null, null), mDeckColor, i, mCardArray.size());

			return fragment;
		}

		@Override
		public int getCount() {
			return mCardArray.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mDeckName + " page " + (position + 1);
		}

		@Override
		public int getItemPosition(Object object) {
			return FragmentStatePagerAdapter.POSITION_NONE;
		}

		public void addNewCard(Card card) {
			long cardId = mDbTools.addNewCard(card);
			card.setCardId(String.valueOf(cardId));
			mCardArray.add(card);
			notifyDataSetChanged();
			if (noCardsTextView.getVisibility() == View.VISIBLE)
				noCardsTextView.setVisibility(View.GONE);
		}

		public void deleteCard(int i) {
			mDbTools.removeCard(mCardArray.get(i));
			mCardArray.remove(i);
			notifyDataSetChanged();
			if (mCardArray.isEmpty())
				noCardsTextView.setVisibility(View.VISIBLE);
		}

		public void editCard(Card oldCard, Card newCard) {
			int position = mCardArray.indexOf(oldCard);
			mCardArray.set(position, newCard);
			mDbTools.editCard(oldCard, newCard);
			notifyDataSetChanged();
		}

	}

	@Override
	public Card getCard(int position) {
		return mCardArray.get(position);
	}

	@Override
	public void addCard(Card card) {
		mAdapter.addNewCard(card);
		mCardPager.setCurrentItem(mCardArray.size() - 1);
	}

	@Override
	public void editCard(Card oldCard, Card newCard) {
		mAdapter.editCard(oldCard, newCard);
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

package com.kevinchou.android.holoflashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import com.kevinchou.android.holoflashcards.objects.Card;

import android.R.color;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends ActionBarActivity implements ListViewFragment.ListViewCardHandler {

	TextView		quizCardsLeftTextView;
	String			mDeckName;
	ArrayList<Card>	mCardArray;
	ArrayList<Card>	mWrongCards;
	Stack<Integer>	mQuizStack;
	int				mColorIdx;
	int				mNumCorrect;
	int				mCurrentPosition;
	boolean			mFlipped;
	DBTools			mDbTools	= new DBTools(this);
	LayerDrawable	mActionBarDrawable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quiz);

		quizCardsLeftTextView = (TextView) findViewById(R.id.quizCardsLeftTextView);

		// Get extras
		mDeckName = getIntent().getStringExtra(DBTools.DECK_NAME);
		mColorIdx = getIntent().getIntExtra("AB_COLOR", 0);
		int deckColor = DBTools.getColor(mColorIdx, this);

		// Action Bar Color
		Drawable accentColorDrawable = new ColorDrawable(deckColor);
		Drawable actionBarBottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
		mActionBarDrawable = new LayerDrawable(new Drawable[] { accentColorDrawable, actionBarBottomDrawable });

		// Set up Action bar
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		actionBar.setTitle(mDeckName + " Quiz!");

		mCardArray = mDbTools.getCardsFrom(mDeckName);

		// set up rest of things
		startQuiz();

		// Sets up cards left textview
		updateCardsLeftTextView();
	}

	public void startQuiz() {

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		mNumCorrect = 0;

		mQuizStack = new Stack<Integer>();

		for (int i = 0; i < mCardArray.size(); i++)
			mQuizStack.push(Integer.valueOf(i));

		// shuffle order of cards
		Collections.shuffle(mQuizStack);

		// Starts displaying cards
		mCurrentPosition = mQuizStack.pop();
		QuizCardFragment quizFrag = QuizCardFragment.newInstance(mCurrentPosition);

		mWrongCards = new ArrayList<Card>();
		ft.add(R.id.container, quizFrag).commit();

	}

	public void updateCardsLeftTextView() {
		int cardsLeft = mQuizStack.size();
		String cardsLeftText = (cardsLeft == 1 ? " Card" : " Cards") + " left";

		String textToDisplay = cardsLeft + cardsLeftText;

		quizCardsLeftTextView.setText(textToDisplay);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
			{
			case android.R.id.home:
				// Intent intent = new Intent(this, CardActivity.class);
				// intent.putExtra(DBTools.DECK_NAME, mDeckName);
				// intent.putExtra(DBTools.DECK_ACCENT_COLOR, mColorIdx);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				// startActivity(intent);
				return true;
			}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	// QUIZ CARD FRAGMENT
	public static class QuizCardFragment extends Fragment {

		boolean	animationOver	= true;

		static QuizCardFragment newInstance(int position) {
			QuizCardFragment quizFrag = new QuizCardFragment();

			Bundle args = new Bundle();
			args.putInt("CARD_POSITION", position);
			quizFrag.setArguments(args);

			return quizFrag;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			// get arguments
			final QuizActivity activity = ((QuizActivity) getActivity());
			int position = getArguments().getInt("CARD_POSITION");
			boolean flipped = getActivity().getIntent().getBooleanExtra("REVERSE_CARDS", false);

			final Card currentCardDisplayed = activity.mCardArray.get(position);

			// Set up view
			final View view = inflater.inflate(R.layout.fragment_card_quizmode, container, false);
			TextView cardQuizFrontTextView = (TextView) view.findViewById(R.id.cardQuizFrontTextView);
			TextView cardQuizBackTextView = (TextView) view.findViewById(R.id.cardQuizBackTextView);
			Button correctButton = (Button) view.findViewById(R.id.correctButton);
			Button wrongButton = (Button) view.findViewById(R.id.wrongButton);

			if (!flipped) {
				cardQuizFrontTextView.setText(currentCardDisplayed.getCardFront());
				cardQuizBackTextView.setText(currentCardDisplayed.getCardBack());
			} else {
				cardQuizFrontTextView.setText(currentCardDisplayed.getCardBack());
				cardQuizBackTextView.setText(currentCardDisplayed.getCardFront());
			}

			correctButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.mNumCorrect += 1;
					moveToNextFragment(activity);
				}
			});

			wrongButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.mWrongCards.add(currentCardDisplayed);
					moveToNextFragment(activity);

				}
			});

			// Click anywhere on the card to flip to back
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View cardFront = (View) view.findViewById(R.id.cardQuizFrontLayout);
					View cardBack = (View) view.findViewById(R.id.cardQuizBackLayout);

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

		public void moveToNextFragment(QuizActivity activity) {
			if (!activity.mQuizStack.empty()) {
				// Instantiate next quiz card fragment
				QuizCardFragment quizFrag = QuizCardFragment.newInstance(activity.mQuizStack.pop());

				// replace current card with next card
				FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
				ft.setCustomAnimations(R.anim.scale_big, R.anim.slide_out_left);
				ft.replace(R.id.container, quizFrag);
				ft.commit();

				// update cards left text
				activity.updateCardsLeftTextView();
			} else {
				// instantiate results card
				QuizOverFragment quizOverFrag = new QuizOverFragment();

				// replace quiz card with results card
				FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
				ft.setCustomAnimations(R.anim.scale_big, R.anim.slide_out_left);
				ft.replace(R.id.container, quizOverFrag);
				ft.commit();

				// set cards left text to invisible
				activity.quizCardsLeftTextView.setVisibility(View.GONE);
			}
		}
	}

	public static class QuizOverFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			// get instance of host activity, to get access to variables
			final QuizActivity activity = (QuizActivity) getActivity();

			ActionBar ab = activity.getSupportActionBar();
			ab.setTitle(" ");
			ab.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.actionbar_bottom));
			

			View v = inflater.inflate(R.layout.fragment_quizresults, container, false);

			View wrongCardsDisplay = (View) v.findViewById(R.id.wrongCardsDisplay);

			TextView quizResultText = (TextView) v.findViewById(R.id.quizResultText);
			Button quizRetryButton = (Button) v.findViewById(R.id.quizRetryButton);
			Button quizExitButton = (Button) v.findViewById(R.id.quizExitButton);

			quizResultText.setText("You got " + activity.mNumCorrect + "/" + activity.mCardArray.size() + " correct!");

			if (activity.mWrongCards.isEmpty()) {
				wrongCardsDisplay.setVisibility(View.GONE);
				((TextView) v.findViewById(R.id.quizAllCorrectText)).setVisibility(View.VISIBLE);
			}

			quizRetryButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Restarts Activity
					Intent intent = activity.getIntent();
					activity.finish();
					startActivity(intent);
					// activity.startQuiz();
				}
			});

			quizExitButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.finish();

					// Intent intent = new Intent(activity, CardActivity.class);
					// intent.putExtra(DBTools.DECK_NAME, activity.mDeckName);
					// intent.putExtra(DBTools.DECK_ACCENT_COLOR,
					// activity.mColorIdx);
					// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					// activity.startActivity(intent);
				}
			});

			return v;
		}

	}

	@Override
	public ArrayList<Card> getCardArray() {
		return mWrongCards;
	}

	@Override
	public boolean listSelectable() {
		return false;
	}

	@Override
	public DBTools getDbTools() {
		return mDbTools;
	}

	@Override
	public CardListActivity getActivity() {
		return null;
	}

}

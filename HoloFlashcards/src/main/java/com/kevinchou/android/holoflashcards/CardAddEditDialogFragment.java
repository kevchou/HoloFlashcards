package com.kevinchou.android.holoflashcards;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kevinchou.android.holoflashcards.objects.Card;

public class CardAddEditDialogFragment extends DialogFragment {

	CardAddEditHandler mCallback;
	
	public interface CardAddEditHandler {
		public Card getCard(int position);
		public void addCard(Card card);
		public void editCard(Card oldCard, Card newCard);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (CardAddEditHandler) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
		}
	}

	public static CardAddEditDialogFragment newInstance(String deckName) {
		CardAddEditDialogFragment dialog = new CardAddEditDialogFragment();

		Bundle args = new Bundle();
		args.putBoolean("NEW_CARD", true);
		args.putString(DBTools.DECK_NAME, deckName);

		dialog.setArguments(args);
		return dialog;
	}

	public static CardAddEditDialogFragment newInstance(int position) {
		CardAddEditDialogFragment dialog = new CardAddEditDialogFragment();

		Bundle args = new Bundle();
		args.putBoolean("NEW_CARD", false);
		args.putInt("CARD_POSITION", position);

		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Set up view
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.fragment_card_addedit, null, false);
		final EditText cardFrontEditText = (EditText) v.findViewById(R.id.cardFrontEditText);
		final EditText cardBackEditText = (EditText) v.findViewById(R.id.cardBackEditText);

		Bundle args = getArguments();
		final boolean newCard = args.getBoolean("NEW_CARD");
		final Card cardToEdit;
		
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			((TextView) v.findViewById(R.id.cardFrontTextView)).setTextColor(Color.WHITE);
			((TextView) v.findViewById(R.id.cardBackTextView)).setTextColor(Color.WHITE);
		}


		String title;
		String positiveButtonText;
		
		if (newCard) {
			cardToEdit = new Card(args.getString(DBTools.DECK_NAME), "", "");
			title = getActivity().getResources().getString(R.string.add_new_card);
			positiveButtonText = getActivity().getResources().getString(R.string.add);
		} else {
			cardToEdit = mCallback.getCard(args.getInt("CARD_POSITION"));
			title = getActivity().getResources().getString(R.string.edit_card);
			positiveButtonText = getActivity().getResources().getString(R.string.save);
		}
		
		final String deckName =cardToEdit.getDeckName();
		final String cardFront= cardToEdit.getCardFront();;
		final String cardBack= cardToEdit.getCardBack();;
		
		// set up initial edit text (blank if adding a card)
		cardFrontEditText.setText(cardFront);
		cardBackEditText.setText(cardBack);

		// build alert dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title);
		builder.setView(v);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newCardFront = cardFrontEditText.getText().toString();
				String newCardBack = cardBackEditText.getText().toString();

				Card card = new Card(deckName, newCardFront, newCardBack);
				
				if (newCard) {
					mCallback.addCard(card);
				} else {
					mCallback.editCard(cardToEdit, card);
				}
				
			}
		});

		return builder.create();
	}
}

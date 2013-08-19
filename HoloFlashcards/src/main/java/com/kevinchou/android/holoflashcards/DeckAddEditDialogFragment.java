package com.kevinchou.android.holoflashcards;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kevinchou.android.holoflashcards.objects.Deck;

public class DeckAddEditDialogFragment extends DialogFragment {

	DeckAddEditHandler	mCallback;

	public interface DeckAddEditHandler {
		public Deck getDeck(int position);

		public boolean deckArrayContains(String deckName);

		public void AddCardsToImportedDeck(String filePath);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (DeckAddEditHandler) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
		}
	}

	// Called for adding a new Deck
	static DeckAddEditDialogFragment newInstance() {
		DeckAddEditDialogFragment dialog = new DeckAddEditDialogFragment();

		Bundle args = new Bundle();
		args.putBoolean("NEW_DECK", true);
		args.putString("IMPORT_PATH", "");
		dialog.setArguments(args);

		return dialog;
	}

	// Called when editing a Deck
	static DeckAddEditDialogFragment newInstance(int position) {
		DeckAddEditDialogFragment dialog = new DeckAddEditDialogFragment();

		Bundle args = new Bundle();
		args.putBoolean("NEW_DECK", false);
		args.putInt("EDIT_POSITION", position);
		dialog.setArguments(args);

		return dialog;
	}

	static DeckAddEditDialogFragment newInstance(String importPath) {
		DeckAddEditDialogFragment dialog = new DeckAddEditDialogFragment();

		Bundle args = new Bundle();
		args.putBoolean("NEW_DECK", true);
		args.putString("IMPORT_PATH", importPath);

		dialog.setArguments(args);

		return dialog;
	}

	// Called when importing a card

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Set up view
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.fragment_deck_addedit, null, false);
		final EditText deckNameEditText = (EditText) v.findViewById(R.id.deckNameEditText);
		final RadioGroup colorSelectRadioGroup = (RadioGroup) v.findViewById(R.id.colorSelectRadioGroup);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			((TextView) v.findViewById(R.id.chooseColorTextView)).setTextColor(Color.WHITE);
		}
		
		// Get and use arguments
		Bundle args = getArguments();
		final boolean newDeck = args.getBoolean("NEW_DECK"); // new deck or

		final String importPath;

		final int position = args.getInt("EDIT_POSITION");

		final Deck oldDeck;

		String dialogTitle;
		String positiveButton;

		if (newDeck) {
			importPath = args.getString("IMPORT_PATH");
			oldDeck = new Deck("", DeckActivity.getTime(), 0);
			if (importPath.equals(""))
				dialogTitle = getResources().getString(R.string.add_new_deck);
			else
				dialogTitle = getResources().getString(R.string.import_deck);
			positiveButton = getResources().getString(R.string.add);

		} else {
			importPath = "";
			oldDeck = mCallback.getDeck(position);
			dialogTitle = getResources().getString(R.string.edit_deck);
			positiveButton = getResources().getString(R.string.save);

		}

		// edit deck
		final String oldDeckName = oldDeck.getDeckName();
		final String createdDate = oldDeck.getDateCreated();
		final int deckColor = oldDeck.getDeckColor();

		// If editing, set up old fields:
		deckNameEditText.setText(oldDeckName);

		// Stupid workaround to get checked button
		int[] radioButtons = { R.id.radio0, R.id.radio1, R.id.radio2, R.id.radio3, R.id.radio4, R.id.radio5 };
		colorSelectRadioGroup.check(radioButtons[deckColor]);

		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(dialogTitle);
		builder.setView(v);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(positiveButton, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Get inputed Deck Name
				String newDeckName = deckNameEditText.getText().toString().trim();

				// Get selected color index (for use on color array)
				int radioButtonID = colorSelectRadioGroup.getCheckedRadioButtonId();
				View radioButton = colorSelectRadioGroup.findViewById(radioButtonID);
				int colorIdx = colorSelectRadioGroup.indexOfChild(radioButton);

				// make sure no deckname repeats or blank names
				if (newDeckName.isEmpty() || newDeckName.equals("")) {
					Toast.makeText(getActivity(), R.string.empty_deckname_error, Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					return;
				} else if (newDeck && mCallback.deckArrayContains(newDeckName)) {
					Toast.makeText(
							getActivity(),
							getActivity().getResources().getString(R.string.deckname_already_exists) + " '"
									+ newDeckName + "'", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					return;
				}

				DeckActivity act = ((DeckActivity) getActivity());
				// Add or replace a card in current adapter
				if (newDeck) {
					act.mAdapter.add(new Deck(newDeckName, createdDate, colorIdx));

					if (!importPath.equals("")) {
						act.mImportedDeckName = newDeckName;
						mCallback.AddCardsToImportedDeck(importPath);
					}
				} else
					act.mAdapter.update(position, new Deck(newDeckName, createdDate, colorIdx));
			}
		});
		return builder.create();
	}
}
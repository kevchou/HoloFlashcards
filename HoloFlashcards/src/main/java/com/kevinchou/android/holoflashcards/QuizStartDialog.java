package com.kevinchou.android.holoflashcards;

import com.kevinchou.android.holoflashcards.DeckAddEditDialogFragment.DeckAddEditHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class QuizStartDialog extends DialogFragment {

	QuizStartDialogHandler mCallback;
	
	public interface QuizStartDialogHandler {
		public String getDeckName();
		public int getColorIdx();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (QuizStartDialogHandler) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
		}
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
				
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dialog_start_quiz, null, false);
		
		final CheckBox quizReverseCheckBox = (CheckBox) v.findViewById(R.id.quizReverseCheckBox);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			((TextView) v.findViewById(R.id.quizDialogTextview)).setTextColor(Color.WHITE);
			quizReverseCheckBox.setTextColor(Color.WHITE);
		}
		
		// intent
		final Intent intent = new Intent(getActivity(), QuizActivity.class);
		intent.putExtra(DBTools.DECK_NAME, mCallback.getDeckName());
		intent.putExtra("AB_COLOR", mCallback.getColorIdx());
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Light));
		builder.setTitle("Start Quiz");
		builder.setView(v);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (quizReverseCheckBox.isChecked()) {
					intent.putExtra("REVERSE_CARDS", true);
					startActivity(intent);
				} else {
					intent.putExtra("REVERSE_CARDS", false);
					startActivity(intent);
				}
			}
		});
		return builder.create();
	}
}
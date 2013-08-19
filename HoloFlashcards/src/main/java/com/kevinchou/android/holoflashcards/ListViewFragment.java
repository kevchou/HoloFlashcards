package com.kevinchou.android.holoflashcards;

import java.util.ArrayList;

import com.kevinchou.android.holoflashcards.CardAddEditDialogFragment.CardAddEditHandler;
import com.kevinchou.android.holoflashcards.objects.Card;
import com.kevinchou.android.holoflashcards.objects.Deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.view.ActionMode;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ListViewFragment extends Fragment {

	ListViewCardHandler	mCallback;
	ActionMode			mActionMode;
	boolean				listIsSelectable;

	public interface ListViewCardHandler {
		public ArrayList<Card> getCardArray();

		public boolean listSelectable();

		public DBTools getDbTools();

		public CardListActivity getActivity();
	}

	ListViewCardAdapter	mAdapter;
	ArrayList<Card>		mCardArray;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (ListViewCardHandler) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_list, container, false);
		ListView cardListView = (ListView) v.findViewById(R.id.cardListView);

		mCardArray = mCallback.getCardArray();
		listIsSelectable = mCallback.listSelectable();

		mAdapter = new ListViewCardAdapter(getActivity(), R.layout.item_card_list, mCardArray, mCallback.getDbTools(),
				listIsSelectable);

		cardListView.setAdapter(mAdapter);

		cardListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if (mActionMode == null) {
					;
				} else {
					onListItemSelect(position);
				}

			}
		});

		cardListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
				onListItemSelect(position);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mActionMode != null) {
					onListItemSelect(position);
				}

				return true;
			}
		});

		return v;
	}

	private void onListItemSelect(int position) {

		mAdapter.toggleSelection(position);
		boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

		if (hasCheckedItems && mActionMode == null) // there are some
													// selected items, start
													// the actionMode
			mActionMode = mCallback.getActivity().startSupportActionMode(new ActionMode.Callback() {

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					final SparseBooleanArray selected = mAdapter.getSelectedIds();

					switch (item.getItemId())
						{
						case R.id.cab_card_edit:
							FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
							DialogFragment newFragment = CardAddEditDialogFragment.newInstance(selected.keyAt(0));
							newFragment.show(ft, "dialog");
							
							mode.finish();
							return true;

						case R.id.cab_card_delete:
							for (int i = (selected.size() - 1); i >= 0; i--) {
								if (selected.valueAt(i)) {
									Card selectedItem = mAdapter.getItem(selected.keyAt(i));
									mAdapter.remove(selectedItem);
								}
							}
							mode.finish();
							return true;
						default:
							return false;
						}
				}

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					// inflate contextual menu
					mode.getMenuInflater().inflate(R.menu.cab_cardlist, menu);
					return true;
				}

				@Override
				public void onDestroyActionMode(ActionMode arg0) {
					mAdapter.removeSelection();
					mActionMode = null;
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					MenuItem item = menu.findItem(R.id.cab_card_edit);
					if (mAdapter.getSelectedCount() == 1) {
						item.setVisible(true);
					} else {
						item.setVisible(false);
					}
					return true;
				}

			});
		else if (!hasCheckedItems && mActionMode != null)
			mActionMode.finish();

		if (mActionMode != null) {
			mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount()) + " selected");
			mActionMode.invalidate();
		}

	}


}

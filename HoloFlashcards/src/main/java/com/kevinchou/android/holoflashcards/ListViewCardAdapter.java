package com.kevinchou.android.holoflashcards;

import java.util.ArrayList;

import com.kevinchou.android.holoflashcards.objects.Card;
import com.kevinchou.android.holoflashcards.objects.Deck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListViewCardAdapter extends ArrayAdapter<Card> {

	Context	mContext;
	boolean mItemsSelectable;
	private SparseBooleanArray	mSelectedItemsIds;
	DBTools mDbTools;
	ArrayList<Card> mCardArray;

	public ListViewCardAdapter(Context context, int resId, ArrayList<Card> cards, DBTools dbTools, boolean itemsSelectable) {
		super(context, resId, cards);
		mContext = context;
		mItemsSelectable = itemsSelectable;
		mCardArray = cards;
		mSelectedItemsIds = new SparseBooleanArray();
		mDbTools = dbTools;
	}

	private class ViewHolder {
		TextView	itemCardFrontTextView;
		TextView	itemCardBackTextView;
		TextView itemCardNumTextView;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_card_list, null);

			holder = new ViewHolder();
			holder.itemCardFrontTextView = (TextView) convertView.findViewById(R.id.itemCardFrontTextView);
			holder.itemCardBackTextView = (TextView) convertView.findViewById(R.id.itemCardBackTextView);
			holder.itemCardNumTextView = (TextView) convertView.findViewById(R.id.itemCardNumTextView);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Card card = getItem(position);

		holder.itemCardFrontTextView.setText(card.getCardFront());
		holder.itemCardBackTextView.setText(card.getCardBack());
		holder.itemCardNumTextView.setText(String.valueOf(position+1));
		
		
		convertView.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4 : Color.TRANSPARENT);
		
		return convertView;
	}

	@Override
	public void add(Card card) {
		String newCardId = String.valueOf(mDbTools.addNewCard(card));
		card.setCardId(newCardId);
		mCardArray.add(card);
		notifyDataSetChanged();
	}
	
	@Override
	public void remove(Card card){
		mCardArray.remove(card);
		mDbTools.removeCard(card);
		notifyDataSetChanged();
		
		if (mCardArray.size() == 0)
			((CardListActivity) mContext).noCardListTextView.setVisibility(View.VISIBLE);
	}
	
	public void editCard(Card oldCard, Card newCard) {
		int position = mCardArray.indexOf(oldCard);
		mCardArray.set(position, newCard);
		mDbTools.editCard(oldCard, newCard);
		notifyDataSetChanged();
	}
	
	// For action mode stuff
	public void toggleSelection(int position) {
		selectView(position, !mSelectedItemsIds.get(position));
	}
	
	public void removeSelection() {
		mSelectedItemsIds = new SparseBooleanArray();
		notifyDataSetChanged();
	}
	
	public void selectView(int position, boolean value) {
		if(value)
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
	
	public Card getCard(int position) {
		return mCardArray.get(position);
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return mItemsSelectable;
	}

	@Override
	public boolean isEnabled(int position) {
		return mItemsSelectable;
	}
}

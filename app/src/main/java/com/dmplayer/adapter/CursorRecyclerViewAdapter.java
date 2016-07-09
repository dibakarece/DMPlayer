/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

/**
 * RecyclerView.Adapter that manages a Cursor, comparable to the CursorAdapter
 * usable in ListView/GridView.
 */
public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	private final Context context;
	private final DataSetObserver observer = new AdapterDataSetObserver();

	private Cursor cursor;
	private boolean valid;

	protected CursorRecyclerViewAdapter(Context context, Cursor cursor) {
		this.context = context;
		this.cursor = cursor;
		if (cursor != null) {
			valid = true;
			cursor.registerDataSetObserver(observer);
		}

		setHasStableIds(true);
	}

	public Context getContext() {
		return context;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == cursor) {
			return null;
		}

		final Cursor oldCursor = cursor;
		if (oldCursor != null) {
			oldCursor.unregisterDataSetObserver(observer);
		}

		cursor = newCursor;
		if (cursor != null) {
			cursor.registerDataSetObserver(observer);
		}

		valid = cursor != null;
		notifyDataSetChanged();
		return oldCursor;
	}

	@Override
	public int getItemCount() {
		return isActiveCursor() ? cursor.getCount() : 0;
	}

	@Override
	public long getItemId(int position) {
		return isActiveCursor() && cursor.moveToPosition(position) ? cursor.getLong(cursor.getColumnIndexOrThrow("_id")) : 0;
	}

	public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

	@Override
	public void onBindViewHolder(VH viewHolder, int position) {
		if (!isActiveCursor()) {
			throw new IllegalStateException("this should only be called when the cursor is valid");
		}
		if (!cursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}
		onBindViewHolder(viewHolder, cursor);
	}

	private boolean isActiveCursor() {
		return valid && cursor != null;
	}

	private class AdapterDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			super.onChanged();
			valid = true;
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			valid = false;
			notifyDataSetChanged();
		}
	}
}

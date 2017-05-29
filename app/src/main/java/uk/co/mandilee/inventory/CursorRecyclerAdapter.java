package uk.co.mandilee.inventory;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

import uk.co.mandilee.inventory.Contract.ProductEntry;

public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private Cursor mCursor;

    private ProductListActivity mActivityContext;

    private boolean isDataValid;

    private int mRowColumnId;

    private DataSetObserver mDataSetObserver;

    public CursorRecyclerAdapter(ProductListActivity context, Cursor c) {
        mActivityContext = context;
        mCursor = c;
        isDataValid = mCursor != null;
        mRowColumnId = isDataValid ? mCursor.getColumnIndex(ProductEntry._ID) : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (isDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (isDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowColumnId);
        }
        return 0;
    }

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException(mActivityContext.getString(R.string.could_not_move_cursor, position));
        }
        if (!isDataValid) {
            throw new IllegalStateException(mActivityContext.getString(R.string.invalid_cursor));
        }
        onBindViewHolder(holder, mCursor);
    }

    public void changeCursor(Cursor mCursor) {
        Cursor old = swapCursor(mCursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }

        final Cursor existingCursor = mCursor;

        if (existingCursor != null && mDataSetObserver != null) {
            existingCursor.unregisterDataSetObserver(mDataSetObserver);
        }

        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowColumnId = newCursor.getColumnIndexOrThrow(ProductEntry._ID);
            isDataValid = true;
            notifyDataSetChanged();

        } else {
            mRowColumnId = -1;
            isDataValid = false;
            notifyDataSetChanged();
        }

        return existingCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            isDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            isDataValid = false;
            notifyDataSetChanged();
        }
    }
}

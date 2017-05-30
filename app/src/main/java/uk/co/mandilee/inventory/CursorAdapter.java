package uk.co.mandilee.inventory;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

import uk.co.mandilee.inventory.Contract.ProductEntry;

abstract class CursorAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private final ProductListActivity mActivityContext;
    private final DataSetObserver mDataSetObserver;
    private Cursor mCursor;
    private boolean isDataValid;
    private int mRowColumnId;

    CursorAdapter(ProductListActivity context, Cursor c) {
        // set context, mainly for getString()
        mActivityContext = context;

        // set cursor
        mCursor = c;

        // check & set if data is valid
        isDataValid = mCursor != null;

        // set current row id if data is valid
        mRowColumnId = isDataValid ? mCursor.getColumnIndex(ProductEntry._ID) : -1;

        // set data observer to notify when data changes
        mDataSetObserver = new NotifyingDataSetObserver();

        // if cursor isn't null, register the data observer
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    /**
     * get the number of items
     */
    @Override
    public int getItemCount() {
        return (isDataValid && mCursor != null)
                ? mCursor.getCount()
                : 0;
    }

    /**
     * get the current item id
     */
    @Override
    public long getItemId(int position) {
        return (isDataValid && mCursor != null && mCursor.moveToPosition(position))
                ? mCursor.getLong(mRowColumnId)
                : 0;
    }

    /**
     * set up binding view holder for recycling the views as scroll is performed
     */
    protected abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

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

    /**
     * replace cursor in use with cursor provided, assuming it's valid and different to existing
     */
    public Cursor switchCursor(Cursor newCursor) {
        if (mCursor == newCursor) {
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

    /**
     * quick class to notify data observer as changed
     */
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

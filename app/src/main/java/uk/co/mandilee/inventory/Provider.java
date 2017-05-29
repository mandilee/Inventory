package uk.co.mandilee.inventory;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import uk.co.mandilee.inventory.Contract.ProductEntry;

public class Provider extends ContentProvider {
    private static final int PRODUCT_ID = 101;
    private static final int PRODUCTS = 100;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_PRODUCTS + "/#", PRODUCT_ID);
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_PRODUCTS, PRODUCTS);
    }

    private Context mContext;
    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductEntry.PRODUCT_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.PRODUCT_TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException(mContext.getString(R.string.error_unknown_uri, uri));
        }

        cursor.setNotificationUri(mContext.getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;

            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;

            default:
                throw new IllegalStateException(mContext.getString(R.string.unknown_uri_match, uri, match));
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);

            default:
                throw new IllegalArgumentException(mContext.getString(R.string.insertion_not_supported, uri));
        }
    }

    private boolean checkValue(String column, ContentValues values) {
        switch (column) {
            case ProductEntry.COLUMN_PRODUCT_NAME:
                if (values.getAsString(column) == null)
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_name));

            case ProductEntry.COLUMN_PRODUCT_PART_NO:
                if (values.getAsString(column) == null)
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_part_no));

            case ProductEntry.COLUMN_PRODUCT_PRICE:
                if (values.getAsString(column) == null)
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_price));

            case ProductEntry.COLUMN_PRODUCT_STOCK:
                if (values.getAsString(column) == null)
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_stock));

            case ProductEntry.COLUMN_PRODUCT_DESCRIPTION:
                if (values.getAsString(column) == null)
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_description));

            case ProductEntry.COLUMN_PRODUCT_PICTURE:
                if (values.getAsString(column) == null)
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_image));
        }

        return true;
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        checkValue(ProductEntry.COLUMN_PRODUCT_NAME, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_PART_NO, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_PRICE, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_STOCK, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_PICTURE, values);

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ProductEntry.PRODUCT_TABLE_NAME, null, values);
        mContext.getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.PRODUCT_TABLE_NAME, selection, selectionArgs);
                break;

            case PRODUCTS:
                rowsDeleted = database.delete(ProductEntry.PRODUCT_TABLE_NAME, null, null);
                break;

            default:
                throw new IllegalArgumentException(mContext.getString(R.string.deletion_not_supported, uri));
        }

        if (rowsDeleted != 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);

            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException(mContext.getString(R.string.update_not_supported, uri));
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        boolean isValid = true;
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            isValid = checkValue(ProductEntry.COLUMN_PRODUCT_NAME, values);
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PART_NO)) {
            isValid = checkValue(ProductEntry.COLUMN_PRODUCT_PART_NO, values);
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            isValid = checkValue(ProductEntry.COLUMN_PRODUCT_PRICE, values);
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_STOCK)) {
            isValid = checkValue(ProductEntry.COLUMN_PRODUCT_STOCK, values);
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_DESCRIPTION)) {
            isValid = checkValue(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, values);
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PICTURE)) {
            isValid = checkValue(ProductEntry.COLUMN_PRODUCT_PICTURE, values);
        }

        if (values.size() == 0 || !isValid) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ProductEntry.PRODUCT_TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}

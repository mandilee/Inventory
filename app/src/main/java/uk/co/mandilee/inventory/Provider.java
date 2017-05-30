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

    // keep these unique
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    // required URI stuffs
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_PRODUCTS + "/#", PRODUCT_ID);
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_PRODUCTS, PRODUCTS);
    }

    // context for easier finding later
    private Context mContext;

    // db helper for lots of things
    private DbHelper mDbHelper;

    /**
     * populate mContext and mDbHelper during onCreate
     */
    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    /**
     * get a readable database to query the table and return the resulting cursor
     * query changes based on whether a product id is provided or not
     * throws IllegalArgumentException if neither products or productid uri is valid
     */
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

    /**
     * get the type of uri it's using
     * throws IllegalArgumentException if neither products or productid uri is valid
     */
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

    /**
     * get the type of uri it's using
     * throws IllegalArgumentException if neither products or productid uri is valid
     */
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

    /**
     * checks the content value of the specified column
     * throws IllegalArgumentException if neither it's null
     */
    private void checkValue(String column, ContentValues values) {
        switch (column) {
            case ProductEntry.COLUMN_PRODUCT_NAME:
                if (values.getAsString(column).equals(""))
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_name));

            case ProductEntry.COLUMN_PRODUCT_PART_NO:
                if (values.getAsString(column).equals(""))
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_part_no));

            case ProductEntry.COLUMN_PRODUCT_PRICE:
                if (values.getAsString(column).equals(""))
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_price));

            case ProductEntry.COLUMN_PRODUCT_STOCK:
                if (values.getAsString(column).equals(""))
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_stock));

            case ProductEntry.COLUMN_PRODUCT_PICTURE:
                if (values.getAsString(column).equals(""))
                    throw new IllegalArgumentException(mContext.getString(R.string.required_product_image));
        }
    }

    /**
     * add the new product to the database after checking no values are null
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        checkValue(ProductEntry.COLUMN_PRODUCT_NAME, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_PART_NO, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_PRICE, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_STOCK, values);
        checkValue(ProductEntry.COLUMN_PRODUCT_PICTURE, values);

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ProductEntry.PRODUCT_TABLE_NAME, null, values);
        mContext.getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * delete selected product (or all products) from database
     * throws IllegalArgumentException if uri is invalid
     */
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

    /**
     * update selected product (or all products)
     * throws IllegalArgumentException if uri is invalid
     */
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

    /**
     * update selected product after checking any changed values are not null
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME))
            checkValue(ProductEntry.COLUMN_PRODUCT_NAME, values);

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PART_NO))
            checkValue(ProductEntry.COLUMN_PRODUCT_PART_NO, values);

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE))
            checkValue(ProductEntry.COLUMN_PRODUCT_PRICE, values);

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_STOCK))
            checkValue(ProductEntry.COLUMN_PRODUCT_STOCK, values);

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PICTURE))
            checkValue(ProductEntry.COLUMN_PRODUCT_PICTURE, values);

        if (values.size() == 0)
            return 0;

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ProductEntry.PRODUCT_TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0)
            mContext.getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }
}

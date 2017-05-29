package uk.co.mandilee.inventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.co.mandilee.inventory.Contract.ProductEntry;

class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private static final String DB_NAME = "inventory.db";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_SQL = "CREATE TABLE " + ProductEntry.PRODUCT_TABLE_NAME + "("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PART_NO + " TEXT NOT NULL ,"
                + ProductEntry.COLUMN_PRODUCT_PRICE + " DECIMAL(12,2) DEFAULT 0,"
                + ProductEntry.COLUMN_PRODUCT_STOCK + " INTEGER DEFAULT 0,"
                + ProductEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT NOT NULL,"
                + ProductEntry.COLUMN_PRODUCT_PICTURE + " TEXT NOT NULL"
                + ");";
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + ProductEntry.PRODUCT_TABLE_NAME);
    }
}

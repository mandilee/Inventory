package uk.co.mandilee.inventory;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {

    public static final String CONTENT_AUTHORITY = "uk.co.mandilee.inventory";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";


    public static abstract class ProductEntry implements BaseColumns {
        public static final String PRODUCT_TABLE_NAME = "products";

        public static final Uri PRODUCT_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // the columns
        public static final String _ID = BaseColumns._ID,
                COLUMN_PRODUCT_NAME = "name",
                COLUMN_PRODUCT_PART_NO = "part_no",
                COLUMN_PRODUCT_PRICE = "price",
                COLUMN_PRODUCT_STOCK = "stock",
                COLUMN_PRODUCT_DESCRIPTION = "description",
                COLUMN_PRODUCT_PICTURE = "picture";


        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
    }
}

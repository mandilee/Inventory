package uk.co.mandilee.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import uk.co.mandilee.inventory.Contract.ProductEntry;

public class ProductListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 1;

    private ProductCursorAdapter mCursorAdapter;

    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ProductListActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        emptyView = findViewById(R.id.empty_view);
        mCursorAdapter = new ProductCursorAdapter(this, null);

        recyclerView.setAdapter(mCursorAdapter);
        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    public void onItemClick(long id) {
        Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
        intent.setData(ContentUris.withAppendedId(ProductEntry.PRODUCT_CONTENT_URI, id));
        startActivity(intent);
    }

    public void onSoldClick(long id, int stock) {
        --stock;
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_STOCK, stock);

        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.PRODUCT_CONTENT_URI, id);
        getContentResolver().update(currentProductUri, values, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PART_NO,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_STOCK,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_PICTURE};

        return new CursorLoader(this,
                ProductEntry.PRODUCT_CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}

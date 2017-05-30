package uk.co.mandilee.inventory;

import android.database.Cursor;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import uk.co.mandilee.inventory.Contract.ProductEntry;


public class ProductCursorAdapter extends CursorAdapter<ProductCursorAdapter.ViewHolder> {

    // set up context, mostly for getString()
    private final ProductListActivity activityContext;

    public ProductCursorAdapter(ProductListActivity context, Cursor c) {
        super(context, c);
        activityContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

        final long productId = cursor.getLong(cursor.getColumnIndex(ProductEntry._ID));

        int indexName = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME),
                indexPrice = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE),
                indexStock = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_STOCK),
                indexPicture = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);

        String productName = cursor.getString(indexName),
                imageUriString = cursor.getString(indexPicture);

        float productPrice = cursor.getFloat(indexPrice);

        Uri imageUri = Uri.parse(imageUriString);

        final int mStock = cursor.getInt(indexStock);

        viewHolder.nameTextView.setText(productName);
        viewHolder.priceTextView.setText(activityContext.getString(R.string.currency_price, productPrice));
        viewHolder.stockTextView.setText((mStock > 0)
                ? activityContext.getString(R.string.num_in_stock, mStock)
                : activityContext.getString(R.string.out_of_stock));
        viewHolder.productPicture.setImageURI(imageUri);
        viewHolder.productPicture.invalidate();

        // if in stock, remove one and update display
        // if out of stock, show toast message saying so
        viewHolder.sold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStock > 0) {
                    activityContext.onSoldClick(productId, mStock);
                } else {
                    Toast.makeText(activityContext, activityContext.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewHolder.wholeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityContext.onItemClick(productId);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView nameTextView;
        final TextView priceTextView;
        final TextView stockTextView;
        final ImageView sold;
        final ImageView productPicture;
        final ConstraintLayout wholeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.text_product_name);
            priceTextView = (TextView) itemView.findViewById(R.id.text_product_price);
            stockTextView = (TextView) itemView.findViewById(R.id.text_product_stock);
            sold = (ImageView) itemView.findViewById(R.id.sold);
            productPicture = (ImageView) itemView.findViewById(R.id.product_image);
            wholeLayout = (ConstraintLayout) itemView.findViewById(R.id.whole_layout);
        }
    }
}

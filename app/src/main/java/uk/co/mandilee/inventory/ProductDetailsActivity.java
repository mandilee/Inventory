package uk.co.mandilee.inventory;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import uk.co.mandilee.inventory.Contract.ProductEntry;

public class ProductDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;

    Uri imageUri;

    private EditText mNameEditText,
            mPartNoEditText,
            mPriceEditText,
            mStockEditText,
            mDescriptionEditText;
    private ImageView mImageView;

    // these four to be hidden when adding new product
    private TextView mAdjustStockTextView;
    private ImageButton mAddStockButton,
            mRemoveStockButton;
    private Button mOrderStockButton;

    private int mCurrentStock;

    private Uri mCurrentProductUri;

    private boolean mProductEdited = false;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductEdited = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        initializeVariables();
        if (mCurrentProductUri == null) {
            newProductSetup();
        } else {
            existingProductSetup();
        }
        setupListeners();
    }

    private void initializeVariables() {
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPartNoEditText = (EditText) findViewById(R.id.edit_product_part_no);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mStockEditText = (EditText) findViewById(R.id.edit_product_stock);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mImageView = (ImageView) findViewById(R.id.product_picture);
        mAddStockButton = (ImageButton) findViewById(R.id.add_stock_button);
        mRemoveStockButton = (ImageButton) findViewById(R.id.remove_stock_button);
        mAdjustStockTextView = (TextView) findViewById(R.id.adjust_stock);
        mOrderStockButton = (Button) findViewById(R.id.order_stock_button);
    }

    private void setupListeners() {
        mNameEditText.setOnTouchListener(mOnTouchListener);
        mPartNoEditText.setOnTouchListener(mOnTouchListener);
        mPriceEditText.setOnTouchListener(mOnTouchListener);
        mStockEditText.setOnTouchListener(mOnTouchListener);
        mDescriptionEditText.setOnTouchListener(mOnTouchListener);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
                mProductEdited = true;
            }
        });
        mAddStockButton.setOnTouchListener(mOnTouchListener);
        mRemoveStockButton.setOnTouchListener(mOnTouchListener);
        if (mCurrentProductUri != null) {
            mOrderStockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orderMore();
                }
            });
            mAddStockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stockUpOne();
                }
            });
            mRemoveStockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stockDownOne();
                }
            });
        }
    }

    private void newProductSetup() {
        setTitle(getString(R.string.title_add_product));
        mNameEditText.setEnabled(true);
        mPartNoEditText.setEnabled(true);
        mRemoveStockButton.setVisibility(View.INVISIBLE);
        mAddStockButton.setVisibility(View.INVISIBLE);
        mAdjustStockTextView.setVisibility(View.INVISIBLE);
        mOrderStockButton.setVisibility(View.INVISIBLE);
        invalidateOptionsMenu();
    }

    private void existingProductSetup() {
        setTitle(getString(R.string.title_edit_product));
        getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        mNameEditText.setEnabled(false);
        mPartNoEditText.setEnabled(false);
        mRemoveStockButton.setVisibility(View.VISIBLE);
        mAddStockButton.setVisibility(View.VISIBLE);
        mStockEditText.setVisibility(View.VISIBLE);
        mAdjustStockTextView.setVisibility(View.VISIBLE);
        mOrderStockButton.setVisibility(View.VISIBLE);
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            chooseImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImage();
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                mImageView.setImageURI(imageUri);
                mImageView.invalidate();
            }
        }
    }

    private void dialogUnsavedChanges(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.dialog_unsaved_changes);
        dialog.setPositiveButton(R.string.dialog_option_discard, discardButtonClickListener);
        dialog.setNegativeButton(R.string.dialog_option_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // no need to delete on new products
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_product);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_product:
                if (saveProduct()) {
                    finish();
                }
                return true;
            case R.id.action_delete_product:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductEdited) {
                    NavUtils.navigateUpFromSameTask(ProductDetailsActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(ProductDetailsActivity.this);
                            }
                        };
                dialogUnsavedChanges(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void orderMore() {
        String mailBody = "We need a new order of " + mNameEditText.getText().toString().trim()
                + "\nPart No: " + mPartNoEditText.getText().toString().trim();
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:order@fictional_supplier.co.uk"));
        i.putExtra(Intent.EXTRA_SUBJECT, "Stock Order");
        i.putExtra(Intent.EXTRA_TEXT, mailBody);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);
        }
    }

    private boolean saveProduct() {
        String nameString = mNameEditText.getText().toString().trim(),
                priceString = mPriceEditText.getText().toString().trim(),
                partNoString = mPartNoEditText.getText().toString().trim(),
                descriptionString = mDescriptionEditText.getText().toString().trim(),
                stockString = mStockEditText.getText().toString();
        if (mCurrentProductUri == null &&
                nameString.isEmpty() && priceString.isEmpty() && stockString.isEmpty() &&
                partNoString.isEmpty() && descriptionString.isEmpty() &&
                imageUri == null) {
            return true; // nothing changed, nothing to save
        } else if (nameString.isEmpty()) {
            mNameEditText.setError(getString(R.string.required_product_name));
            return false; // name required!
        } else if (partNoString.isEmpty()) {
            mPartNoEditText.setError(getString(R.string.required_product_part_no));
            return false; // part no required!
        } else if (priceString.isEmpty()) {
            mPriceEditText.setError(getString(R.string.required_product_price));
            return false; // price required!
        } else if (stockString.isEmpty()) {
            mStockEditText.setError(getString(R.string.required_product_stock));
            return false; // stock required!
        } else if (imageUri == null) {
            showToast(R.string.required_product_image);
            return false; // image required!
        }
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PART_NO, partNoString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_STOCK, stockString);
        values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, imageUri.toString());
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.PRODUCT_CONTENT_URI, values);
            if (newUri == null) {
                showToast(R.string.error_saving);
            } else {
                showToast(R.string.save_successful);
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                showToast(R.string.error_saving);
            } else {
                if (mProductEdited) {
                    showToast(R.string.save_successful);
                }
            }
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.
                setMessage(R.string.delete_dialog)
                .setPositiveButton(R.string.delete_option_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteProduct();
                    }
                })
                .setNegativeButton(R.string.delete_option_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        // Create and show the AlertDialog
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            if (getContentResolver().delete(mCurrentProductUri, null, null) == 0) {
                showToast(R.string.delete_failed);
            } else {
                showToast(R.string.delete_success);
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_STOCK,
                ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_PART_NO};
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int indexName = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME),
                    indexPartNo = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PART_NO),
                    indexPrice = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE),
                    indexStock = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_STOCK),
                    indexDescription = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION),
                    indexImage = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);
            String name = cursor.getString(indexName),
                    partNo = cursor.getString(indexPartNo),
                    description = cursor.getString(indexDescription),
                    imageUriString = cursor.getString(indexImage);
            mCurrentStock = cursor.getInt(indexStock);
            float price = cursor.getFloat(indexPrice);
            mNameEditText.setText(name);
            mPartNoEditText.setText(partNo);
            mPriceEditText.setText(getString(R.string.formatted_price, price));
            mStockEditText.setText(String.format(Locale.UK, "%d", mCurrentStock));
            mDescriptionEditText.setText(description);
            imageUri = Uri.parse(imageUriString);
            mImageView.setImageURI(imageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPartNoEditText.setText("");
        mPriceEditText.setText("");
        mStockEditText.setText("");
        mDescriptionEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        if (mProductEdited) {
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    };
            dialogUnsavedChanges(discardButtonClickListener);
        } else {
            super.onBackPressed();
        }
    }

    public void stockUpOne() {
        ++mCurrentStock;
        updateStockDisplay();
    }

    public void stockDownOne() {
        if (mCurrentStock < 1) {
            showToast(R.string.cant_lower_stock);
        } else {
            --mCurrentStock;
            updateStockDisplay();
        }
    }

    public void updateStockDisplay() {
        mStockEditText.setText(String.valueOf(mCurrentStock));
    }

    private void showToast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

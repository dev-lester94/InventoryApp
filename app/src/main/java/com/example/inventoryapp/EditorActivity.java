package com.example.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.inventoryapp.data.InventoryContract.ItemEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Log tag with the className as the tag
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private int INVENTORY_LOADER = 0;
    /** EditText field to enter the item's name */
    private EditText mNameEditText;

    /** EditText field to enter the items's quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the item's price */
    private EditText mPriceEditText;

    /** EditText field to enter the item's email */
    private EditText mEmailEditText;

    /** EditText field to enter the item's phone */
    private EditText mPhoneEditText;


    //Uri for the item that has being clicked,
    //helps deterine if we are editing or inserting a new item
    private Uri mCurrentItemUri;

    //Spots any changes to edittext views that will be used to show a dialog prompt
    //if an item has been edited for update
    private boolean mItemHasChanged = false;

    //Buttons to increment and decrement the quantity field
    private Button mQuantityIncrement;
    private Button mQuantityDecrement;

    //Email and phone button to open up the email or phone app
    private Button mEmailButton;
    private Button mPhoneButton;

    //Touch Listener to determine if the item fields have been edited for update
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quanity);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mEmailEditText = (EditText) findViewById(R.id.edit_item_email);
        mPhoneEditText = (EditText) findViewById(R.id.edit_item_phone);

        //Set the touch listeners to the edit text views
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);

        //Find all the relevant views that are needed to increment
        // the quantity or email and phone the provider of the item
        mQuantityIncrement = (Button) findViewById(R.id.quantity_increment);
        mQuantityDecrement = (Button) findViewById(R.id.quantity_decrement);;
        mEmailButton = (Button) findViewById(R.id.email_button);
        mPhoneButton = (Button) findViewById(R.id.phone_button);


        //Get the uri passed when an item was clicked in the list view
        Intent intent = getIntent();
        Uri currentPetUri = intent.getData();
        mCurrentItemUri = intent.getData();


        if(currentPetUri == null){
            //Add a new item mode, invalidate option menu and disable the buttons
            setTitle(getString(R.string.editor_activity_title_new_item));
            invalidateOptionsMenu();
            mQuantityIncrement.setEnabled(false);
            mQuantityDecrement.setEnabled(false);

            mEmailButton.setEnabled(false);

            mPhoneButton.setEnabled(false);

        }else{
            //Edit mode, create onclicklisteners to the buttons
            setTitle(getString(R.string.editor_activity_title_edit_item));

            //Increment the quantity
            mQuantityIncrement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int quantity = Integer.parseInt(String.valueOf(mQuantityEditText.getText()));
                    quantity++;
                    mQuantityEditText.setText(Integer.toString(quantity));
                }
            });

            //Decrement the quantity
            mQuantityDecrement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int quantity = Integer.parseInt(String.valueOf(mQuantityEditText.getText()));
                    if(quantity == 0){
                        return;
                    }
                    quantity--;
                    mQuantityEditText.setText(Integer.toString(quantity));
                }
            });

            //Open email app with item provider's email as the address
            mEmailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this

                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{String.valueOf(mEmailEditText.getText())});
                    //intent.putExtra(Intent.EXTRA_SUBJECT, "Sunject Text Here..");
                    Log.i("email", String.valueOf(mEmailEditText.getText()));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }

                }
            });

            //Open phone app with the item provider's phone number
            mPhoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + mPhoneEditText.getText()));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }


        //Set Up button to the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Start loader to get the items attributes and set them in the edit text views
        getLoaderManager().initLoader(INVENTORY_LOADER,null,  this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item to be inserted, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                //Respond to the up button
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Create a alertdialog for any unsaved changes when updating an
    // item when the up or back button is pressed
    private void showUnsavedChangesDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        showUnsavedChangesDialog();
    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Delete an item in the database
    private void deleteItem() {
        int rowsDeleted = getContentResolver().delete(mCurrentItemUri,null,null);
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    //Save newly inserted item
    private void saveItem() {
        String nameString = mNameEditText.getText().toString().trim();

        //Item must have a name to be inserted to the database
        if (mCurrentItemUri == null && TextUtils.isEmpty(nameString))  {
            return;
        }

        //Get the attribute from the edittext of the item to create a contentvalue to
        //insert the item with the attributes
        int quantity = 0;
        String quantityString = mQuantityEditText.getText().toString().toString();
        if(!TextUtils.isEmpty(quantityString)){
            quantity = Integer.parseInt(quantityString);
        }

        double price = 0;
        String priceString = mPriceEditText.getText().toString().toString();
        if(!TextUtils.isEmpty(priceString)){
            price = Double.parseDouble(priceString);
        }

        String emailString = mEmailEditText.getText().toString().trim();

        String phoneString = mPhoneEditText.getText().toString().trim();


        //Create the content values with the approriate attributes
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
        values.put(ItemEntry.COLUMN_ITEM_EMAIL, emailString);
        values.put(ItemEntry.COLUMN_ITEM_PHONE, phoneString);

        //Add an item mode to insert a newly item to the inventory database
        if(mCurrentItemUri == null){
            Uri newRowUriId = getContentResolver().insert(ItemEntry.CONTENT_URI,values);
            if(newRowUriId!=null){
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            }
            Log.i(LOG_TAG,newRowUriId.toString());


        }else{
            //Edit an item mode to update an item already in the inventory database

            int rowsAffected = getContentResolver().update(mCurrentItemUri,values,null,null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            //Log.i(LOG_TAG,numberR);
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        //Edit mode
        //Create a request to the database and returns the inventory items in the database as a cursor
        //SELECT * from items
        //Return null during add a new item mode

        if (mCurrentItemUri == null) {
            return null;
        }
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_EMAIL,
                ItemEntry.COLUMN_ITEM_PHONE
        };

        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //Load all the cursor data into the edittext views for update
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int emailColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_EMAIL);
            int phoneColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PHONE);
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Double.toString(price));
            mEmailEditText.setText(email);
            mPhoneEditText.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Just make sure the cursoradpater contains no cursor data during reset
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mEmailEditText.setText("");
        mPhoneEditText.setText("");
    }
}
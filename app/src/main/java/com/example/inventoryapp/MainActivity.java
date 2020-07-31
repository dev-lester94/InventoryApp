package com.example.inventoryapp;
import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.Toast;

import com.example.inventoryapp.data.InventoryDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.inventoryapp.data.InventoryContract.ItemEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Reference to the database to read or write to it
    private InventoryDbHelper mDbHelper;

    //Id of the Loader
    private int INVENTORY_LOADER = 0;

    //Cursor adapter that will hold all the cursor data and
    //how to create a inventory item to be added into the list
    private ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mDbHelper = new InventoryDbHelper(this);

        // Find the ListView which will be populated with the pet data
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        //Create and set cursorAdapter to listView to populate the inventory items
        mCursorAdapter = new ItemCursorAdapter(this,null);
        inventoryListView.setAdapter(mCursorAdapter);

        //Click in inventory item to open an intent to update the item
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentPetUri  = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });



        //Start the loader to get cursor data to populate the listview with inventory items
        getLoaderManager().initLoader(INVENTORY_LOADER,null,  this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
        //Log.v("MainActivity", rowsDeleted + " rows deleted from items database");
    }


    private void insertItem() {
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and dummy item attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Nintendo Switch");
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 1);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, 300.00);
        values.put(ItemEntry.COLUMN_ITEM_EMAIL, "abc@gmail.com");
        values.put(ItemEntry.COLUMN_ITEM_PHONE, "123456789");

        //Contact the resolver to insert the item with the content values to the database
        //Returns the uri where the item is placed in the database

        Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Create a request to the database and returns the inventory items in the database as a cursor
        //SELECT _id, name, quantity, price from items
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
        };
        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Swap any old cursor data with newly updated data and populate the listview
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Just make sure the cursoradpater contains no cursor data during reset
        mCursorAdapter.swapCursor(null);
    }
}
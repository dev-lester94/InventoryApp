package com.example.inventoryapp;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.inventoryapp.data.InventoryContract.ItemEntry;

import java.text.DecimalFormat;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.inventory_item,parent,false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // TODO: Fill out this method

        TextView tvName = (TextView) view.findViewById(R.id.inventory_item_name);
        TextView tvQuantity = (TextView) view.findViewById(R.id.inventory_item_quantity);
        TextView tvPrice= (TextView) view.findViewById(R.id.inventory_item_price);


        String name  = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
        int quantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
        double price = cursor.getDouble(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE));

        DecimalFormat df = new DecimalFormat("0.00");


        tvName.setText(name);
        tvQuantity.setText(Integer.toString(quantity));
        tvPrice.setText("$"+df.format(price));

    }
}

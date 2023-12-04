package com.example.compshop.Cart.Tasks;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.AsyncTask;

import com.example.compshop.Cart.DatabaseHelper;
import com.example.compshop.Cart.DatabaseManager;
import com.example.compshop.Cart.Interface.ItemCallback;
import com.example.compshop.Models.Item;

import java.util.ArrayList;
import java.util.List;

public class DatabaseTask extends AsyncTask<Void, Void, List<Item>> {
    private DatabaseManager databaseManager;
    private ItemCallback callback;

    public DatabaseTask(DatabaseManager databaseManager, ItemCallback callback) {
        this.databaseManager = databaseManager;
        this.callback = callback;
    }

    @SuppressLint("Range")
    @Override
    protected List<Item> doInBackground(Void... voids) {
        // Perform the database operation (retrieve Cursor)
        List<Item> itemList = new ArrayList<>();
        Cursor cursor = databaseManager.getAllItems();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEMNAME));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE));
                String item_Id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEMID));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP));
                String Uid = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UID));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEMIMAGE));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL));

                Item itm = new Item(name, category, description, price, item_Id, timestamp, Uid, image, quantity, total);
                itm.setId(id);//set the id from the database

                //add the itm onject to the arraylist
                itemList.add(itm);


            } while (cursor.moveToNext());
            cursor.close();
        }
        //  databaseManager.close();
        return itemList;
    }

    @Override
    protected void onPostExecute(List<Item> itemList) {
        // Pass the result to the callback interface
        callback.onDatabaseTaskComplete(itemList);
    }
}


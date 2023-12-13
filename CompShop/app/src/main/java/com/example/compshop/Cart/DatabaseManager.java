package com.example.compshop.Cart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.compshop.Models.Item;

import java.sql.SQLDataException;


public class DatabaseManager {
    Context context;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;

    public DatabaseManager(Context context) {
        this.context = context;
    }

    public DatabaseManager open() throws SQLDataException {
        databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        databaseHelper.close();
    }

    public long addItem(Item item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_ITEMNAME, item.getName());
        contentValues.put(DatabaseHelper.COLUMN_CATEGORY, item.getCategory());
        contentValues.put(DatabaseHelper.COLUMN_DESCRIPTION, item.getDescription());
        contentValues.put(DatabaseHelper.COLUMN_PRICE, item.getPrice());
        contentValues.put(DatabaseHelper.COLUMN_ITEMID, item.getItem_Id());
        contentValues.put(DatabaseHelper.COLUMN_TIMESTAMP, item.getTimestamp());
        contentValues.put(DatabaseHelper.COLUMN_UID, item.getUid());
        contentValues.put(DatabaseHelper.COLUMN_ITEMIMAGE, item.getImage());
        contentValues.put(DatabaseHelper.COLUMN_QUANTITY, item.getQuantity());
        contentValues.put(DatabaseHelper.COLUMN_TOTAL, item.getTotal());
        contentValues.put(DatabaseHelper.COLUMN_FAVORITE, 0);

        return db.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }

    public void deleteItem(int id) {
        db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteAll() {
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_NAME);
    }

    // Get all items from cart
    public Cursor getAllItems() {
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME;
        return db.rawQuery(selectQuery, null);
    }

    public void updateItem(Item item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("quantity", item.getQuantity());
        contentValues.put("total", item.getTotal());

        db.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(item.getId())});
    }

    public void updateFavStatus(Item item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_FAVORITE, item.isFavorite() ? 1 : 0);

        db.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(item.getId())});
    }

    public boolean checkAlreadyExists(String itemname) {
        String[] item = new String[]{DatabaseHelper.COLUMN_ITEMNAME};
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, item, DatabaseHelper.COLUMN_ITEMNAME + "='" + itemname + "'", null, null, null, null);
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    // Inside DatabaseManager class
    public boolean isItemInCart(Item item) {
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NAME,
                new String[]{DatabaseHelper.COLUMN_ITEMID},
                DatabaseHelper.COLUMN_ITEMID + "=?",
                new String[]{String.valueOf(item.getItem_Id())},
                null,
                null,
                null
        );

        boolean isItemInCart = cursor.getCount() > 0;

        cursor.close();
        return isItemInCart;
    }


    //favorites part
    public long addToFavorites(Item item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_ITEMNAME, item.getName());
        contentValues.put(DatabaseHelper.COLUMN_CATEGORY, item.getCategory());
        contentValues.put(DatabaseHelper.COLUMN_DESCRIPTION, item.getDescription());
        contentValues.put(DatabaseHelper.COLUMN_PRICE, item.getPrice());
        contentValues.put(DatabaseHelper.COLUMN_ITEMID, item.getItem_Id());
        contentValues.put(DatabaseHelper.COLUMN_TIMESTAMP, item.getTimestamp());
        contentValues.put(DatabaseHelper.COLUMN_UID, item.getUid());
        contentValues.put(DatabaseHelper.COLUMN_ITEMIMAGE, item.getImage());
        contentValues.put(DatabaseHelper.COLUMN_QUANTITY, item.getQuantity());
        contentValues.put(DatabaseHelper.COLUMN_TOTAL, item.getTotal());

        // Set the favorite flag to 1
        contentValues.put(DatabaseHelper.COLUMN_FAVORITE, 1);

        return db.insert(DatabaseHelper.TABLE_NAME_FAVORITES, null, contentValues);
    }

    public void removeFromFavorites(long itemId) {
        // Remove the item from the favorites table
        db.delete(DatabaseHelper.TABLE_NAME_FAVORITES, DatabaseHelper.COLUMN_ITEMID + "=?", new String[]{String.valueOf(itemId)});

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_FAVORITE, 0);
        db.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ITEMID + "=?", new String[]{String.valueOf(itemId)});
    }

    public Cursor getFavoriteItems() {
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_FAVORITES + " WHERE " + DatabaseHelper.COLUMN_FAVORITE + " = 1";
        return db.rawQuery(selectQuery, null);
    }
}

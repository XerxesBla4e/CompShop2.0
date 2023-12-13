package com.example.compshop.Cart;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Information
    private static final String DB_NAME = "my_cart_database";
    private static final int DB_VERSION = 6;  // Increment the version for the database upgrade
    // Table Names
    public static final String TABLE_NAME = "my_cart_items";
    public static final String TABLE_NAME_FAVORITES = "favorite_items";  // New table for favorites

    // Common Columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ITEMNAME = "itemname";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_ITEMID = "itemId";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_UID = "Uid";
    public static final String COLUMN_ITEMIMAGE = "itemimage";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_TOTAL = "total";

    // Columns specific to the Favorites table
    public static final String COLUMN_FAVORITE = "favorite";

    // Queries for creating tables
    static final String CREATE_CART_ITEMS_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ITEMNAME + " TEXT NOT NULL, " +
            COLUMN_CATEGORY + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
            COLUMN_PRICE + " TEXT NOT NULL, " +
            COLUMN_ITEMID + " TEXT NOT NULL, " +
            COLUMN_TIMESTAMP + " TEXT NOT NULL, " +
            COLUMN_UID + " TEXT NOT NULL, " +
            COLUMN_ITEMIMAGE + " TEXT, " +
            COLUMN_QUANTITY + " INTEGER NOT NULL, " +
            COLUMN_TOTAL + " INTEGER NOT NULL, " +
            COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT 0" +
            ");";

    static final String CREATE_FAVORITES_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME_FAVORITES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ITEMNAME + " TEXT NOT NULL, " +
            COLUMN_CATEGORY + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
            COLUMN_PRICE + " TEXT NOT NULL, " +
            COLUMN_ITEMID + " TEXT NOT NULL, " +
            COLUMN_TIMESTAMP + " TEXT NOT NULL, " +
            COLUMN_UID + " TEXT NOT NULL, " +
            COLUMN_ITEMIMAGE + " TEXT, " +
            COLUMN_QUANTITY + " INTEGER NOT NULL, " +
            COLUMN_TOTAL + " INTEGER NOT NULL," +
            COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT 0" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CART_ITEMS_TABLE_QUERY);
        sqLiteDatabase.execSQL(CREATE_FAVORITES_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FAVORITES);
        onCreate(sqLiteDatabase);
    }
}

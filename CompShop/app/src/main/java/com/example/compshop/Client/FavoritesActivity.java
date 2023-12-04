package com.example.compshop.Client;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.compshop.Adapters.FavAdapter;
import com.example.compshop.Cart.DatabaseHelper;
import com.example.compshop.Cart.DatabaseManager;
import com.example.compshop.Models.Item;
import com.example.compshop.databinding.ActivityFavoritesBinding;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private ActivityFavoritesBinding activityFavoritesBinding;
    private FavAdapter favAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityFavoritesBinding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(activityFavoritesBinding.getRoot());

        // Initialize RecyclerView and its adapter
        favAdapter = new FavAdapter(this);
        activityFavoritesBinding.recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        activityFavoritesBinding.recyclerViewFavorites.setAdapter(favAdapter);

        // Retrieve favorite items from the database and populate the RecyclerView
        retrieveFavoriteItems();
    }

    @SuppressLint("Range")
    private void retrieveFavoriteItems() {
        List<Item> favoriteItemList = new ArrayList<>();

        // Open the database
        DatabaseManager databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();

            // Retrieve favorite items from the database
            Cursor cursor = databaseManager.getFavoriteItems();
            if (cursor.moveToFirst()) {
                do {
                    // Use getColumnIndexOrThrow to get column indices safely
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
                    itm.setId(id);

                    favoriteItemList.add(itm);

                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (SQLDataException e) {
            e.printStackTrace();
        } finally {
            // Close the database
            databaseManager.close();
        }

        // Update the adapter with the retrieved favorite items
        favAdapter.updateItemList(favoriteItemList);
    }


}

package com.example.compshop.Client;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Adapters.ItemAdapter;
import com.example.compshop.Adapters.MyAdapter;
import com.example.compshop.Authentication.LoginActivity;
import com.example.compshop.Authentication.UpdateProfile;
import com.example.compshop.Cart.DatabaseManager;
import com.example.compshop.Interface.ActionType;
import com.example.compshop.Interface.OnItemClickListener;
import com.example.compshop.Location.LocationManagerHelper;
import com.example.compshop.Models.Item;
import com.example.compshop.Models.category;
import com.example.compshop.R;
import com.example.compshop.databinding.CategoryitemsactivityBinding;
import com.example.compshop.databinding.HomeBinding;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.squareup.picasso.Picasso;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryItems extends AppCompatActivity {
    private CategoryitemsactivityBinding categoryItemsBinding;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList = new ArrayList<>();
    private FirebaseFirestore db;
    private category selectedCategory;
    DatabaseManager databasemanager;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private SearchView searchView;
    private String uid1;
    BottomNavigationView bottomNavigationView;

    //Dialog instances
    private TextView name, price2, newprice2, description, totalamount, quantitytextview, viewall;
    private ImageButton addQty, reduceQty;
    private ImageButton favoriteButton;
    private Button addToCartBtn;
    ImageView imageView1;
    ShimmerFrameLayout shimmerFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryItemsBinding = CategoryitemsactivityBinding.inflate(getLayoutInflater());
        setContentView(categoryItemsBinding.getRoot());

        initViews(categoryItemsBinding);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize ItemAdapter
        itemAdapter = new ItemAdapter(getApplicationContext());
        recyclerView.setAdapter(itemAdapter);

        shimmerFrameLayout.startShimmer();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        databasemanager = new DatabaseManager(getApplicationContext());
        try {
            databasemanager.open();
        } catch (SQLDataException e) {
            e.printStackTrace();
        }

        initBottomNavView();

        itemAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Item item, int position, ActionType actionType) {
                switch (actionType) {
                    case ADD_TO_CART:
                        callpopupdialog(item);
                        break;

                    case ADD_TO_FAVORITES:
                        item.setFavorite(!item.isFavorite());

                        itemAdapter.notifyItemChanged(position);

                        if (item.isFavorite()) {
                            databasemanager.addToFavorites(item);
                        } else {
                            databasemanager.removeFromFavorites(Long.parseLong(item.getItem_Id()));
                        }
                        break;
                    default:

                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchQuery = query.trim();
                shimmerFrameLayout.startShimmer();
                filterItems(searchQuery);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String searchQuery = newText.trim();
                shimmerFrameLayout.startShimmer();
                filterItems(searchQuery);
                return true;
            }
        });

        // Retrieve category from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("categorydata")) {
            selectedCategory = (category) intent.getSerializableExtra("categorydata");
        } else {
            Toast.makeText(this, "Category not provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (firebaseUser != null) {
            uid1 = firebaseUser.getUid();
            fetchItemsByCategory(selectedCategory.getCategoryName());
        } else {
            startActivity(new Intent(CategoryItems.this, LoginActivity.class));
            finish();
        }

    }

    private void initBottomNavView() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_cart) {
                    Intent x = new Intent(CategoryItems.this, CartActivity.class);
                    x.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x);
                    return true;
                } else if (item.getItemId() == R.id.nav_prof2) {
                    Intent x6 = new Intent(CategoryItems.this, UpdateProfile.class);
                    //   x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x6);
                    return true;
                } else if (item.getItemId() == R.id.nav_orders) {
                    Intent x4 = new Intent(CategoryItems.this, OrdersActivity.class);
                    x4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x4);
                    return true;
                } else if (item.getItemId() == R.id.nav_favourites) {
                    Intent x4 = new Intent(CategoryItems.this, FavoritesActivity.class);
                    x4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x4);
                    return true;
                } else if (item.getItemId() == R.id.nav_home) {
                    Intent x4 = new Intent(CategoryItems.this, ClientMain.class);
                    x4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x4);
                    return true;
                }
                return false;
            }
        });
    }

    private void filterItems(String searchQuery) {
        List<Item> filteredList = new ArrayList<>();

        // Check if search query is empty
        if (searchQuery.isEmpty()) {
            filteredList.addAll(itemList);
        } else {
            // Apply search query filter
            for (Item item : itemList) {
                if (item.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        // Update RecyclerView adapter with filtered list
        itemAdapter.updateItemList(filteredList);
        itemAdapter.notifyDataSetChanged();

        updateUIAndStopShimmer();
    }

    private void initViews(CategoryitemsactivityBinding categoryItemsBinding) {
        recyclerView = categoryItemsBinding.recyclerViewpopv;
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        shimmerFrameLayout = categoryItemsBinding.shimmerLayout3;
        searchView = categoryItemsBinding.searchView;
        bottomNavigationView = categoryItemsBinding.bottomNavgation;
        viewall = categoryItemsBinding.textView59;
    }

    private void updateUIAndStopShimmer() {
        // UI-related code here
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void callpopupdialog(Item item) {
        runOnUiThread(() -> {
            DialogPlus dialogPlus = DialogPlus.newDialog(CategoryItems.this)
                    .setContentHolder(new ViewHolder(R.layout.popupmenu))
                    .setExpanded(true, 1100)
                    .setGravity(Gravity.BOTTOM) // Set the dialog to appear from the bottom
                    .create();
            View dialogView = dialogPlus.getHolderView();

            imageView1 = dialogView.findViewById(R.id.imageView0);
            addQty = dialogView.findViewById(R.id.imageButtonAdd);
            favoriteButton = dialogView.findViewById(R.id.favoriteButton1);
            reduceQty = dialogView.findViewById(R.id.imageButtonRemove);
            name = dialogView.findViewById(R.id.foodNameTextView);
            name.setText(item.getName());
            description = dialogView.findViewById(R.id.descriptionTextView);
            description.setText(item.getDescription());
            quantitytextview = dialogView.findViewById(R.id.textViewQuantity);
            quantitytextview.setText(String.valueOf(item.getQuantity()));
            addToCartBtn = dialogView.findViewById(R.id.button2);
            price2 = dialogView.findViewById(R.id.amountTextView);
            price2.setText(item.getPrice());
            newprice2 = dialogView.findViewById(R.id.discountAmountTextView);
            totalamount = dialogView.findViewById(R.id.totalAmountTextView);
            totalamount.setText(String.valueOf(item.getTotal()));

            if (item.isFavorite()) {
                // Item is a favorite, set red color
                favoriteButton.setColorFilter(ContextCompat.getColor(CategoryItems.this, R.color.colorRed));
            } else {
                // Item is not a favorite, set default color
                favoriteButton.setColorFilter(ContextCompat.getColor(CategoryItems.this, R.color.defaultHeartColor));
            }

            computePriceDiscount(item, price2, newprice2);

            addQty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int quantity = item.getQuantity();
                    quantity++; // Increment the quantity

                    // Update the quantity and total in the food object
                    item.setQuantity(quantity);
                    quantitytextview.setText(String.valueOf(item.getQuantity()));
                    totalamount.setText(String.valueOf(item.getTotal()));
                }
            });

            reduceQty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int quantity = item.getQuantity();
                    if (quantity > 1) {
                        quantity--; // Decrement the quantity

                        // Update the quantity and total in the food object
                        item.setQuantity(quantity);
                        quantitytextview.setText(String.valueOf(item.getQuantity()));
                        totalamount.setText(String.valueOf(item.getTotal()));
                    }
                }
            });

            addToCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Check if the item with the same ID already exists in the cart
                    if (databasemanager.isItemInCart(item)) {
                        Toast.makeText(view.getContext(), "Item is already in the cart", Toast.LENGTH_SHORT).show();
                    } else {
                        // Item is not in the cart, proceed to add it
                        Item item2 = new Item(item.getName(), item.getCategory(), item.getDescription(), item.getPrice(),
                                item.getItem_Id(), item.getTimestamp(), item.getUid(), item.getImage(), item.getQuantity(), item.getTotal());

                        // Update the total based on the provided discount and discount description
                        item2.updateTotal(item.getDiscount(), item.getDiscountdescription());

                        int res = (int) databasemanager.addItem(item2);

                        if (res > 0) {
                            Toast.makeText(view.getContext(), "ADDED TO CART", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            String imagePath = item.getImage();
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    Picasso.get().load(item.getImage()).into(imageView1);
                } else {
                    imageView1.setImageResource(R.mipmap.ic_launcher);
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView1.setImageResource(R.mipmap.ic_launcher);
            }
            quantitytextview.setText(String.valueOf(item.getQuantity()));

            dialogPlus.show();
        });

    }

    private void computePriceDiscount(Item item, TextView price2, TextView newprice2) {
        if (item.getDiscount() != null && !item.getDiscount().isEmpty() && item.getDiscountdescription() != null && !item.getDiscountdescription().isEmpty()) {
            int discount = Integer.parseInt(item.getDiscount());
            if (discount > 0 && item.getDiscountdescription().contains("%")) {
                double newPrice = Double.parseDouble(item.getPrice()) * (1 - discount / 100.0);
                newprice2.setVisibility(View.VISIBLE);
                newprice2.setText(String.format(Locale.getDefault(), "Price: Shs %.2f", newPrice));
                // Add crossline through old price
                price2.setPaintFlags(price2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                // Remove crossline if discount condition is not met
                price2.setPaintFlags(price2.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                newprice2.setVisibility(View.GONE);
            }
        } else {
            // Remove crossline and clear new price if discount conditions are not met
            price2.setPaintFlags(price2.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            newprice2.setVisibility(View.GONE);
        }
    }

    private void fetchItemsByCategory(String categoryName) {
        CollectionReference usersCollection = db.collection("users");

        // Query users with "Admin" account type
        Query adminUsersQuery = usersCollection.whereEqualTo("accounttype", "Admin");

        adminUsersQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                itemList.clear();
                for (QueryDocumentSnapshot userDocument : task.getResult()) {
                    CollectionReference productsCollection = userDocument.getReference().collection("Products");

                    Query productsQuery = productsCollection.whereEqualTo("category", categoryName);

                    productsQuery.get().addOnCompleteListener(productsTask -> {
                        if (productsTask.isSuccessful()) {
                            for (QueryDocumentSnapshot productDocument : productsTask.getResult()) {
                                Item item = productDocument.toObject(Item.class);
                                itemList.add(item);
                            }

                            if (itemList.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "No items available for the selected category", Toast.LENGTH_SHORT).show();
                            }

                            // Update the adapter's item list and notify the change
                            itemAdapter.updateItemList(itemList);
                            itemAdapter.notifyDataSetChanged();

                            // Common code for updating UI and stopping shimmer effect
                            updateUIAndStopShimmer();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error getting products for the selected category", Toast.LENGTH_SHORT).show();
                            finish();

                            // Common code for updating UI and stopping shimmer effect
                            updateUIAndStopShimmer();
                        }
                    });
                }
            } else {
                Toast.makeText(getApplicationContext(), "Error getting admin user documents", Toast.LENGTH_SHORT).show();
                finish();

                // Common code for updating UI and stopping shimmer effect
                updateUIAndStopShimmer();
            }
        });
    }

}

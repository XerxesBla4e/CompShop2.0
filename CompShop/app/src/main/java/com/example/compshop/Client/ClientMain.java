package com.example.compshop.Client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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
import com.example.compshop.Interface.OnMoveToItemsListener;
import com.example.compshop.Location.LocationManagerHelper;
import com.example.compshop.Models.Item;
import com.example.compshop.Models.category;
import com.example.compshop.Onboarding.SplashScreen;
import com.example.compshop.R;
import com.example.compshop.databinding.HomeBinding;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.squareup.picasso.Picasso;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClientMain extends AppCompatActivity {
    HomeBinding homeBinding;
    RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList = new ArrayList<>();
    private boolean loading = true;
    private FirebaseFirestore db;
    DatabaseManager databasemanager;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private SearchView searchView;
    private String uid1;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;

    //Dialog instances
    private TextView name, price2, newprice2, description, totalamount, quantitytextview, viewall;
    private ImageButton addQty, reduceQty;
    private ImageButton favoriteButton;
    private Button addToCartBtn;
    LocationManagerHelper locationManagerHelper;
    ImageView imageView1;
    ShimmerFrameLayout shimmerFrameLayout;
    ArrayList<category> categoryArrayList = new ArrayList<>();
    RecyclerView recyclerView1;
    MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeBinding = HomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        initViews(homeBinding);

        setSupportActionBar(toolbar);

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

        if (firebaseUser != null) {
            uid1 = firebaseUser.getUid();
            fetchItems();
        } else {
            startActivity(new Intent(ClientMain.this, LoginActivity.class));
            finish();
        }

        // Initialize the LocationManagerHelper
        locationManagerHelper = new LocationManagerHelper(this, null, null);

        // Call the location-related tasks
        locationManagerHelper.checkAndRequestLocationPermissions();

        initBottomNavView();

        fetchCategory();

        // Add a scroll listener to load more items when needed
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    int visibleItemCount = gridLayoutManager.getChildCount();
                    int totalItemCount = gridLayoutManager.getItemCount();
                    int pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            fetchNextPage();
                        }
                    }
                }
            }
        });


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

        viewall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewall.setTextColor(Color.RED);

                fetchNextPage();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewall.setTextColor(getResources().getColor(R.color.originalviewalltextcolor));
                    }
                }, 1000);
            }
        });

        myAdapter.setOnMoveToItemsListener(new OnMoveToItemsListener() {
            @Override
            public void onMoveToDets(category category) {
                Intent intent = new Intent(ClientMain.this, CategoryItems.class);
                intent.putExtra("categorydata", category);
                startActivity(intent);
            }
        });
    }

    private void callpopupdialog(Item item) {
        runOnUiThread(() -> {
            DialogPlus dialogPlus = DialogPlus.newDialog(ClientMain.this)
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
                favoriteButton.setColorFilter(ContextCompat.getColor(ClientMain.this, R.color.colorRed));
            } else {
                // Item is not a favorite, set default color
                favoriteButton.setColorFilter(ContextCompat.getColor(ClientMain.this, R.color.defaultHeartColor));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Delegate the permission result to LocationManagerHelper
        locationManagerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {

        } else if (id == R.id.action_logout) {
            makeOffline();
            firebaseAuth.signOut();
            startActivity(new Intent(ClientMain.this, SplashScreen.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBottomNavView() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_cart) {
                    Intent x = new Intent(ClientMain.this, CartActivity.class);
                    x.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x);
                    return true;
                } else if (item.getItemId() == R.id.nav_prof2) {
                    Intent x6 = new Intent(ClientMain.this, UpdateProfile.class);
                    //   x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x6);
                    return true;
                } else if (item.getItemId() == R.id.nav_orders) {
                    Intent x4 = new Intent(ClientMain.this, OrdersActivity.class);
                    x4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x4);
                    return true;
                } else if (item.getItemId() == R.id.nav_favourites) {
                    Intent x4 = new Intent(ClientMain.this, FavoritesActivity.class);
                    x4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x4);
                    return true;
                }
                return false;
            }
        });
    }

    private void makeOffline() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentRef = firestore.collection("users").document(uid1);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("online", "false");

        documentRef.update(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                        //Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        Toast.makeText(getApplicationContext(), e.getMessage() + "", Toast.LENGTH_SHORT).show();
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

        //shimmerLayout.stopShimmer();
        //shimmerLayout.setVisibility(View.GONE);
    }

    private void initViews(HomeBinding homeBinding) {
        recyclerView = homeBinding.recyclerViewpopv;
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        shimmerFrameLayout = homeBinding.shimmerLayout3;
        searchView = homeBinding.searchView;
        bottomNavigationView = homeBinding.bottomNavgation;
        toolbar = homeBinding.toolbar;
        viewall = homeBinding.textView59;
        itemAdapter = new ItemAdapter(getApplicationContext());
        recyclerView.setAdapter(itemAdapter);
        recyclerView1 = homeBinding.recyclerViewHorizontal;
        recyclerView1.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        myAdapter = new MyAdapter(getApplicationContext(), categoryArrayList);
    }

    private void updateUIAndStopShimmer() {
        // UI-related code here
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        loading = true;
    }

    private void updateUIAndStopShimmer1() {
        //shimmerLayout1.stopShimmer();
        //shimmerLayout1.setVisibility(View.GONE);

    }

    private void fetchItems() {
        Query userQuery = db.collection("users").whereEqualTo("accounttype", "Admin");

        userQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                itemList.clear(); // Clear existing items before adding new ones

                for (QueryDocumentSnapshot userDocument : task.getResult()) {
                    CollectionReference productsCollection = userDocument.getReference().collection("Products");

                    Query productsQuery = productsCollection.limit(10);

                    productsQuery.get().addOnCompleteListener(productsTask -> {
                        if (productsTask.isSuccessful()) {
                            for (QueryDocumentSnapshot productDocument : productsTask.getResult()) {
                                Item item = productDocument.toObject(Item.class);
                                itemList.add(item);
                            }

                            if (itemList.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "No items available", Toast.LENGTH_SHORT).show();
                            }
                            // Update the adapter's item list and notify the change
                            itemAdapter.updateItemList(itemList);
                            itemAdapter.notifyDataSetChanged();

                            // Common code for updating UI and stopping shimmer effect
                            updateUIAndStopShimmer();
                        } else {
                            Log.d("TAG", "Error getting product documents: ", productsTask.getException());
                            // Common code for updating UI and stopping shimmer effect
                            updateUIAndStopShimmer();
                        }
                    });
                }
            } else {
                Log.d("TAG", "Error getting user documents: ", task.getException());
                // Common code for updating UI and stopping shimmer effect
                updateUIAndStopShimmer();
            }
        });
    }

    private void fetchNextPage() {
        // Check if itemList is not empty before using startAfter
        if (!itemList.isEmpty()) {
            Query nextQuery = db.collection("users")
                    .whereEqualTo("accounttype", "Admin")
                    .orderBy("timestamp")
                    .limit(10)
                    .startAfter(itemList.get(itemList.size() - 1).getTimestamp());

            nextQuery.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Iterate through the admin users
                    for (QueryDocumentSnapshot userDocument : task.getResult()) {
                        // Access the "Products" subcollection for each admin user
                        CollectionReference productsCollection = userDocument.getReference().collection("Products");

                        // Query products within the "Products" subcollection
                        Query productsQuery = productsCollection.limit(10);

                        // Fetch products within the subcollection
                        productsQuery.get().addOnCompleteListener(productsTask -> {
                            if (productsTask.isSuccessful()) {
                                // Exclude items already in the list
                                List<Item> newItems = new ArrayList<>();
                                for (QueryDocumentSnapshot productDocument : productsTask.getResult()) {
                                    Item item = productDocument.toObject(Item.class);
                                    if (!itemList.contains(item)) {
                                        newItems.add(item);
                                    }
                                }
                                // Update the adapter's item list and notify the change
                                itemList.addAll(newItems);
                                itemAdapter.updateItemList(itemList);
                                itemAdapter.notifyDataSetChanged();

                                // Common code for updating UI and stopping shimmer effect
                                updateUIAndStopShimmer();
                            } else {
                                Log.d("TAG", "Error getting product documents: ", productsTask.getException());
                                // Common code for updating UI and stopping shimmer effect
                                updateUIAndStopShimmer();
                            }
                        });
                    }
                } else {
                    Log.d("TAG", "Error getting admin user documents: ", task.getException());
                    updateUIAndStopShimmer();
                }
            });
        } else {
            Log.d("TAG", "itemList is empty");
        }
    }


    private void fetchCategory() {
        Query userQuery = db.collection("users").whereEqualTo("accounttype", "Admin");

        userQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoryArrayList.clear(); // Clear existing items before adding new ones

                for (QueryDocumentSnapshot userDocument : task.getResult()) {
                    CollectionReference productsCollection = userDocument.getReference().collection("Categories");

                    Query productsQuery = productsCollection.limit(10);

                    productsQuery.get().addOnCompleteListener(productsTask -> {
                        if (productsTask.isSuccessful()) {
                            for (QueryDocumentSnapshot productDocument : productsTask.getResult()) {
                                category item = productDocument.toObject(category.class);
                                categoryArrayList.add(item);
                            }
                            // Notify the adapter of the data change
                            myAdapter.notifyDataSetChanged();
                            updateUIAndStopShimmer1();
                        } else {
                            Log.d("TAG", "Error getting category documents: ", productsTask.getException());
                            // Common code for updating UI and stopping shimmer effect
                            updateUIAndStopShimmer1();
                        }
                    });
                }
                // Set the adapter outside the loop after all data is collected
                recyclerView1.setAdapter(myAdapter);
            } else {
                Log.d("TAG", "Error getting user documents: ", task.getException());
                // Common code for updating UI and stopping shimmer effect
                updateUIAndStopShimmer1();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop location updates in onDestroy
        locationManagerHelper.stopLocationUpdates();
    }
}

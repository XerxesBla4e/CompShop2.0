package com.example.compshop.Admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Adapters.OrderAdapter;
import com.example.compshop.Admin.Handler.OrderDeletionHandler;
import com.example.compshop.Admin.Interface.OnOrderDeletedListener;
import com.example.compshop.Authentication.LoginActivity;
import com.example.compshop.Authentication.UpdateProfile;
import com.example.compshop.Client.ClientMain;
import com.example.compshop.Dialogs.AddTransactionDialog;
import com.example.compshop.Interface.OnMoveToDetsListener;
import com.example.compshop.Location.LocationManagerHelper;
import com.example.compshop.Models.Order;

import com.example.compshop.Onboarding.SplashScreen;
import com.example.compshop.R;
import com.example.compshop.databinding.ActivityAdminMainBinding;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminMain extends AppCompatActivity {
    ActivityAdminMainBinding activityAdminMainBinding;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton floatingActionButton;
    private List<Order> orderList;
    private OrderAdapter orderAdapter;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;
    String uid1;
    RecyclerView recyclerView;
    private LocationManager locationManager;

    private static final String TAG = "Location";
    private LocationListener locationListener;
    ShimmerFrameLayout shimmerFrameLayout;
    LocationManagerHelper locationManagerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityAdminMainBinding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(activityAdminMainBinding.getRoot());

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        initViews(activityAdminMainBinding);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            uid1 = firebaseUser.getUid();
            shimmerFrameLayout.startShimmer();
            fetchOrders();
            shimmerFrameLayout.hideShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
        } else {
            startActivity(new Intent(AdminMain.this, LoginActivity.class));
            finish();
        }

        initBottomNavView();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddTransactionDialog addTransactionDialog = new AddTransactionDialog();
                addTransactionDialog.show(getSupportFragmentManager(), "add transaction dialog");
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    showConfirmationDialog(viewHolder);
                } else {
                    orderAdapter.notifyDataSetChanged(); // Refresh the adapter to undo the swipe action
                }
            }
        }).attachToRecyclerView(recyclerView);

        orderAdapter.setOnMoveToDetsListener(new OnMoveToDetsListener() {
            @Override
            public void onMoveToDets(Order order) {
                Intent intent = new Intent(getApplicationContext(), ClientDetails.class);
                intent.putExtra("ordersModel", order);
                startActivity(intent);
            }
        });

        // Initialize the LocationManagerHelper
        locationManagerHelper = new LocationManagerHelper(this, null, null);

        // Call the location-related tasks
        locationManagerHelper.checkAndRequestLocationPermissions();
    }

    private void initBottomNavView() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    Intent x = new Intent(getApplicationContext(), AdminMain.class);
                    x.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x);
                    return true;
                } else if (item.getItemId() == R.id.nav_logout) {
                    makeOffline();
                    firebaseAuth.signOut();
                    startActivity(new Intent(AdminMain.this, SplashScreen.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.nav_prof) {
                    Intent x6 = new Intent(getApplicationContext(), UpdateProfile.class);
                //    x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x6);
                    return true;
                } else if (item.getItemId() == R.id.nav_viewproducts) {
                    Intent x5 = new Intent(getApplicationContext(), ViewMyItems.class);
                    x5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x5);
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

    @SuppressLint("NotifyDataSetChanged")
    private void fetchOrders() {
        // Assuming the "users" collection contains a document with the current user's ID as the document ID
        DocumentReference currentUserRef = firestore.collection("users").document(uid1);

        currentUserRef.collection("orders") // Assuming "orders" is the collection containing order documents
                .whereEqualTo("orderTo", firebaseAuth.getUid())
                .orderBy("orderTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot orderSnapshot : queryDocumentSnapshots) {
                        Order order = orderSnapshot.toObject(Order.class);
                        orderList.add(order);
                        //    Toast.makeText(getApplicationContext(),""+order.getOrderTo(),Toast.LENGTH_SHORT).show();
                    }
                    orderAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                });
    }


    @SuppressLint("NotifyDataSetChanged")
    private void initViews(ActivityAdminMainBinding activityAdminMainBinding) {
        bottomNavigationView = activityAdminMainBinding.bottomNavgation;
        floatingActionButton = activityAdminMainBinding.fab;
        recyclerView = activityAdminMainBinding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        shimmerFrameLayout = activityAdminMainBinding.shimmerLayout3;
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, getApplicationContext());
        recyclerView.setAdapter(orderAdapter);
        orderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Delegate the permission result to LocationManagerHelper
        locationManagerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showConfirmationDialog(RecyclerView.ViewHolder viewHolder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminMain.this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this order?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteOrderItem(viewHolder);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        orderAdapter.notifyDataSetChanged(); // Refresh the adapter to undo the swipe action
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteOrderItem(RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            Order order = orderList.get(position);
            OrderDeletionHandler.deleteOrder(this, uid1, order, new OnOrderDeletedListener() {

                @Override
                public void onOrderDeleted() {
                    Toast.makeText(AdminMain.this, "Order deleted", Toast.LENGTH_SHORT).show();
                    orderList.remove(position);
                    orderAdapter.notifyItemRemoved(position);
                }

                @Override
                public void onOrderDeletionFailed(String errorMessage) {
                    Toast.makeText(AdminMain.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}

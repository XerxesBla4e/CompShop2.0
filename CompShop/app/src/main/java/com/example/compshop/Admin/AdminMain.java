package com.example.compshop.Admin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.example.compshop.Interface.OnMoveToDetsListener;
import com.example.compshop.Models.Order;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityAdminMainBinding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(activityAdminMainBinding.getRoot());

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        checkgrantpermission();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);/*
           Is a system service that provides access to the device's location services
        */
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateUserLocation(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        initViews(activityAdminMainBinding);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    uid1 = user.getUid();
                    shimmerFrameLayout.startShimmer();
                    fetchOrders();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                } else {
                    startActivity(new Intent(AdminMain.this, LoginActivity.class));
                    finish();
                }
            }
        });

        initBottomNavView();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x0 = new Intent(getApplicationContext(), AddItem.class);
                x0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(x0);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
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
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.nav_prof) {
                    Intent x6 = new Intent(getApplicationContext(), UpdateProfile.class);
                    x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(x6);
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

    private void fetchOrders() {
        // Assuming the "users" collection contains a document with the current user's ID as the document ID
        DocumentReference currentUserRef = firestore.collection("users").document(uid1);

        currentUserRef.collection("orders") // Assuming "orders" is the collection containing order documents
                .whereEqualTo("orderTo", firebaseAuth.getUid())
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

    private void checkgrantpermission() {
        // Check and request location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with location updates
            requestLocationUpdates();
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission is required for the app to function correctly." +
                        " Please grant the permission in the app settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //associates the location listener(Callback listener) with the location manager
    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void updateUserLocation(double latitude, double longitude) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentRef = firestore.collection("users").document(uid1);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("latitude", "" + latitude);
        updateData.put("longitude", "" + longitude);

        documentRef.update(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                        // Toast.makeText(getApplicationContext(), "Location updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  Log.d(TAG, "" + e);
                        // Handle any errors
                        // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

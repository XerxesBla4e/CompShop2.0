package com.example.compshop.Admin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Adapters.ItemOrderA;
import com.example.compshop.Models.ItemOrder;
import com.example.compshop.Models.Order;
import com.example.compshop.Models.UserDets;
import com.example.compshop.R;
import com.example.compshop.Utils.FCMSend;
import com.example.compshop.databinding.ActivityClientAdminBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ClientDetails extends AppCompatActivity {
    ActivityClientAdminBinding activityClientAdminBinding;
    RecyclerView recyclerView;
    TextView studentname, location1, status1, totalprice;
    ImageView edit, delete;
    Order ordersModel;
    List<ItemOrder> orderList;
    String notstudenttoken;

    ItemOrderA OrdersAdapter;
    FirebaseFirestore firestore;
    double total = 0.0;
    FirebaseAuth firebaseAuth;
    String OrderID, OrderBy;
    FirebaseUser firebaseUser;
    String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityClientAdminBinding = ActivityClientAdminBinding.inflate(getLayoutInflater());
        setContentView(activityClientAdminBinding.getRoot());
        initViews(activityClientAdminBinding);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            id = firebaseUser.getUid();
        }

        Intent intent = getIntent();

        if (intent.hasExtra("")) {
            // Extract the Orders object from the intent's extra data
            ordersModel = intent.getParcelableExtra("ordersModel");
            OrderID = ordersModel.getOrderID();
            OrderBy = ordersModel.getOrderBy();
            status1.setText(ordersModel.getOrderStatus());
        }

        retrievePersonalDets(OrderBy);

        Toast.makeText(getApplicationContext(), "Order By" + OrderBy, Toast.LENGTH_SHORT).show();

        // Create a Firestore query to retrieve the item orders for the specific order and user
        CollectionReference userOrdersRef = FirebaseFirestore.getInstance().collection("users");

        // Modify the query to fetch orders from nested sub-collections
        CollectionReference itemOrdersRef = userOrdersRef
                .document(id)
                .collection("orders")
                .document(OrderID)
                .collection("itemOrders");

        // Perform the query
        itemOrdersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!querySnapshot.isEmpty()) {
                        total = 0.0;
                        orderList.clear();

                        for (DocumentSnapshot itemOrderSnapshot : querySnapshot.getDocuments()) {
                            // Use the ItemOrder class to map Firestore document to object
                            ItemOrder itemOrder = itemOrderSnapshot.toObject(ItemOrder.class);
                            if (itemOrder != null) {
                                String itemPriceString = String.valueOf(itemOrder.getItemTotal());
                                double itemPrice = 0.0;

                                try {
                                    itemPrice = Double.parseDouble(itemPriceString);
                                    total += itemPrice;
                                } catch (NumberFormatException e) {
                                    // Handle the NumberFormatException, such as logging an error or displaying an error message
                                    Log.e("ClientDetails1", "Error parsing item price: " + e.getMessage());
                                }

                                orderList.add(itemOrder);
                            }
                        }

                        // Notify the adapter of data change
                        OrdersAdapter.setItemOrderList(orderList);
                        OrdersAdapter.notifyDataSetChanged();

                        // Update the total price
                        totalprice.setText(String.format("%.2f", total));
                        totalprice.requestLayout();
                    }
                } else {
                    // Handle the error in fetching item orders
                    Exception exception = task.getException();
                    if (exception != null) {
                        // Log or display the error message
                    }
                }
            }
        });


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateOrderStatusDialog();
            }
        });
    }

    private void updateOrderStatusDialog() {
        final String[] status3 = {"In Progress", "Confirmed", "Cancelled"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ClientDetails.this);
        mBuilder.setTitle("Update Order Status");
        mBuilder.setSingleChoiceItems(status3, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int xer) {
                if (xer == 0) {
                    String Message = "In Progress";
                    updateOrderStatus(Message);
                    status1.setText(Message);
                    status1.setTextColor(getBaseContext().getResources().getColor(R.color.lightGreen));
                } else if (xer == 1) {
                    String Message = "Confirmed";
                    updateOrderStatus(Message);
                    status1.setText(Message);
                    status1.setTextColor(getBaseContext().getResources().getColor(R.color.teal_200));
                } else if (xer == 2) {
                    String Message = "Cancelled";
                    updateOrderStatus(Message);
                    status1.setText(Message);
                    status1.setTextColor(getBaseContext().getResources().getColor(R.color.colorRed));
                }
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mBuilder.show();
    }


    private void initViews(ActivityClientAdminBinding activityClientDetailsBinding) {
        recyclerView = activityClientDetailsBinding.recyclerView65;
        studentname = activityClientDetailsBinding.studentname;
        location1 = activityClientDetailsBinding.studentlocation;
        status1 = activityClientDetailsBinding.orderStatus;
        totalprice = activityClientDetailsBinding.totalprice;
        edit = activityClientDetailsBinding.editstatus;
        delete = activityClientDetailsBinding.editstatus;
        orderList = new ArrayList<>();
        OrdersAdapter = new ItemOrderA(getApplicationContext(), orderList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(OrdersAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateOrderStatus(String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the specific order document
        DocumentReference orderRef = db.collection("users")
                .document(id)
                .collection("orders")
                .document(OrderID);

        // Update the orderStatus field with the new message
        orderRef.update("orderStatus", message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Order status updated successfully
                        Toast.makeText(getApplicationContext(), "Order is now " + message, Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update order status
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void prepareNotificationMessage(String message) {
        if (notstudenttoken != null) {
            FCMSend.pushNotification(
                    ClientDetails.this,
                    notstudenttoken,
                    "Status Update",
                    message
            );
        }
    }

    private void retrievePersonalDets(String orderBy) {
        if (orderBy != null && !orderBy.isEmpty()) {
            CollectionReference usersCollectionRef = firestore.collection("users");

            usersCollectionRef.whereEqualTo("uid", orderBy).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            UserDets user = document.toObject(UserDets.class);

                            if (user != null) {
                                String name = user.getName();
                                studentname.setText(name);
                                String location = user.getLocation();
                                location1.setText(location);
                                notstudenttoken = user.getToken();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error: Unable to retrieve user data", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Student Doesn't Have Personal Info", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Exception exception = task.getException();
                        //   Toast.makeText(getApplicationContext(), "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        // Log the error for further investigation
                        Log.e("FirestoreError", "Error retrieving personal data", exception);
                    }
                }
            });
        } else {
            //   Toast.makeText(getApplicationContext(), "Error: Invalid OrderBy value", Toast.LENGTH_SHORT).show();
            // Log an error message for debugging
            Log.e("FirestoreError", "Invalid OrderBy value: " + OrderBy);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), AdminMain.class);
        startActivity(intent);
        // super.onBackPressed();
        // Finish the current activity
        finish();
    }

}

package com.example.compshop.Client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Adapters.ItemOrderAdapter;
import com.example.compshop.Models.Item;
import com.example.compshop.Models.ItemOrder;
import com.example.compshop.Models.Order;
import com.example.compshop.Models.UserDets;
import com.example.compshop.databinding.ActivityClientDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
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

public class ClientDetailsActivity extends AppCompatActivity {
    ActivityClientDetailsBinding activityClientDetailsBinding;
    RecyclerView recyclerView;
    TextView studentname, location1, status1, totalprice;
    ImageView edit, locate3;
    String notpatienttoken;
    Order ordersModel;
    List<ItemOrder> orderList;
    ItemOrderAdapter itemOrderAdapter;
    FirebaseFirestore firestore;
    double total = 0.0;
    FirebaseAuth firebaseAuth;
    String OrderID, OrderBy, OrderTo;
    FirebaseUser firebaseUser;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityClientDetailsBinding = ActivityClientDetailsBinding.inflate(getLayoutInflater());
        setContentView(activityClientDetailsBinding.getRoot());
        initViews(activityClientDetailsBinding);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            id = firebaseUser.getUid();
        }

        RetrievePersonalDets();

        Intent intent = getIntent();

        if (intent.hasExtra("ordersModel")) {
            // Extract the Orders object from the intent's extra data
            ordersModel = intent.getParcelableExtra("ordersModel");
            OrderID = ordersModel.getOrderID();
            OrderBy = ordersModel.getOrderBy();
            OrderTo = ordersModel.getOrderTo();
            status1.setText(ordersModel.getOrderStatus());
        }

        CollectionReference itemOrdersRef = FirebaseFirestore.getInstance().collection("users")
                .document(OrderTo)
                .collection("orders")
                .document(OrderID)
                .collection("itemOrders");

        itemOrdersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            ItemOrder itemOrder = document.toObject(ItemOrder.class);
                            int itemMPrice1 = itemOrder != null ? itemOrder.getItemTotal() : null;
                            double mPrice = 0.0;

                            try {
                                mPrice = itemMPrice1;
                                total += mPrice;
                            } catch (NumberFormatException e) {
                                Log.e("ClientDetails1", "Error parsing item price: " + e.getMessage());
                            }

                           orderList.add(itemOrder);

                        }
                        itemOrderAdapter.setItemModelList(orderList);
                        itemOrderAdapter.notifyDataSetChanged();
                        totalprice.setText(String.format("%.2f", total));
                        totalprice.requestLayout();
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        // Log or display the error message
                    }
                }
            }
        });
    }

    private void RetrievePersonalDets() {
        if (OrderBy != null) {
            DocumentReference userRef = firestore.collection("users").document(OrderBy);

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            UserDets user = document.toObject(UserDets.class);

                            String location = user.getLocation();
                            location1.setText(location);
                            notpatienttoken = user.getToken();

                        } else {
                            Toast.makeText(getApplicationContext(), "You Don't Have Personal Info", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Exception exception = task.getException();
                    }
                }
            });
        }
    }

    private void initViews(ActivityClientDetailsBinding activityClientDetailsBinding) {
        recyclerView = activityClientDetailsBinding.clientrec;
        studentname = activityClientDetailsBinding.patientname;
        location1 = activityClientDetailsBinding.patientlocation;
        status1 = activityClientDetailsBinding.orderStatus;
        totalprice = activityClientDetailsBinding.totalprice;
        edit = activityClientDetailsBinding.editstatus;
        locate3 = activityClientDetailsBinding.locateclient;
        orderList = new ArrayList<>();
        itemOrderAdapter = new ItemOrderAdapter(getApplicationContext(), orderList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(itemOrderAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ClientMain.class);
        startActivity(intent);
        finish();
    }
}

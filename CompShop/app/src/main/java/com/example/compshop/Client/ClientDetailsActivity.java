package com.example.compshop.Client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.compshop.R;
import com.example.compshop.Utils.LocationUtils;
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
    String notpatienttoken;
    Order ordersModel;
    List<ItemOrder> orderList;
    ItemOrderAdapter itemOrderAdapter;
    FirebaseFirestore firestore;
    double total = 0.0;
    FirebaseAuth firebaseAuth;
    String OrderID, OrderBy, OrderTo;
    FirebaseUser firebaseUser;
    String id, name, contact, Adminlatitude, Adminlongitude, Clientlatitude, Clientlongitude;
    ImageView call, track;

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
            retrieveUserDetails(id);
        }
        // RetrievePersonalDets();
        Intent intent = getIntent();

        if (intent.hasExtra("ordersModel")) {
            // Extract the Orders object from the intent's extra data
            ordersModel = intent.getParcelableExtra("ordersModel");
            OrderID = ordersModel.getOrderID();
            OrderBy = ordersModel.getOrderBy();
            OrderTo = ordersModel.getOrderTo();
            retrieveAdminDetails(OrderTo);

            String status3 = ordersModel.getOrderStatus();


            if (status3.equals("In Progress")) {
                status1.setText(ordersModel.getOrderStatus());
                status1.setTextColor(getBaseContext().getResources().getColor(R.color.lightGreen));
            } else if (status3.equals("Confirmed")) {
                status1.setText(ordersModel.getOrderStatus());
                status1.setTextColor(getBaseContext().getResources().getColor(R.color.teal_200));
            } else if (status3.equals("Cancelled")) {
                status1.setText(ordersModel.getOrderStatus());
                status1.setTextColor(getBaseContext().getResources().getColor(R.color.colorRed));
            }
        }

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = "tel:" + contact;
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
                startActivity(dialIntent);
            }
        });

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "http://maps.google.com/maps?saddr=" + Clientlatitude + "," +
                        Clientlongitude + "&daddr=" + Adminlatitude + "," + Adminlongitude;
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
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

    private void retrieveAdminDetails(String orderTo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(orderTo);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Adminlatitude = documentSnapshot.getString("latitude");
                        Adminlongitude = documentSnapshot.getString("longitude");
                        contact = documentSnapshot.getString("phonenumber");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure scenario if necessary
                });
    }

    private void retrieveUserDetails(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        name = documentSnapshot.getString("name");
                        studentname.setText("Hello " + name);
                       // contact = documentSnapshot.getString("phonenumber");

                        double latitude = Double.valueOf(documentSnapshot.getString("latitude"));
                        double longitude = Double.valueOf(documentSnapshot.getString("longitude"));
                        Clientlatitude = documentSnapshot.getString("latitude");
                        Clientlongitude = documentSnapshot.getString("longitude");

                        String address = LocationUtils.getAddressFromLatLng(getApplicationContext(), latitude, longitude);
                        Log.d("User Address", "Address: " + address);

                        location1.setText(address);
                        notpatienttoken = documentSnapshot.getString("token");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure scenario if necessary
                });
    }

    private void initViews(ActivityClientDetailsBinding activityClientDetailsBinding) {
        recyclerView = activityClientDetailsBinding.clientrec;
        studentname = activityClientDetailsBinding.patientname;
        location1 = activityClientDetailsBinding.patientlocation;
        status1 = activityClientDetailsBinding.orderStatus;
        totalprice = activityClientDetailsBinding.totalprice;
        orderList = new ArrayList<>();
        itemOrderAdapter = new ItemOrderAdapter(getApplicationContext(), orderList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(itemOrderAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        call = activityClientDetailsBinding.call;
        track = activityClientDetailsBinding.clientlocation2;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ClientMain.class);
        startActivity(intent);
        finish();
    }
}

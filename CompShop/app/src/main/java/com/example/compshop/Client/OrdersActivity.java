package com.example.compshop.Client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Adapters.OrderAdapter;
import com.example.compshop.Interface.OnMoveToDetsListener;
import com.example.compshop.Models.Order;
import com.example.compshop.databinding.FragmentCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private FragmentCartBinding fragmentCartBinding;
    private RecyclerView recyclerView;
    private List<Order> orderList;
    private OrderAdapter orderAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentCartBinding = FragmentCartBinding.inflate(getLayoutInflater());
        setContentView(fragmentCartBinding.getRoot());

        initViews();
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, getApplicationContext());
        recyclerView.setAdapter(orderAdapter);

        fetchOrders();

        orderAdapter.setOnMoveToDetsListener(new OnMoveToDetsListener() {
            @Override
            public void onMoveToDets(Order order) {
                Intent intent = new Intent(getApplicationContext(), ClientDetailsActivity.class);
                intent.putExtra("ordersModel", order);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        recyclerView = fragmentCartBinding.recyclerview11;
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchOrders() {
        CollectionReference usersCollectionRef = firestore.collection("users");

        usersCollectionRef
                .whereEqualTo("accounttype", "Admin")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot adminSnapshot : queryDocumentSnapshots) {
                       // String adminId = adminSnapshot.getId();

                        CollectionReference ordersCollectionRef = adminSnapshot.getReference().collection("orders");

                        ordersCollectionRef
                                .whereEqualTo("orderBy", firebaseAuth.getUid())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    if (!queryDocumentSnapshots2.isEmpty()) {
                                        for (QueryDocumentSnapshot orderSnapshot : queryDocumentSnapshots2) {
                                            Order order = orderSnapshot.toObject(Order.class);
                                            orderList.add(order);
                                        }
                                        orderAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), ClientMain.class));
    }
}

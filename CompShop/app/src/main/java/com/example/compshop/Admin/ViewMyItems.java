package com.example.compshop.Admin;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Adapters.ViewItemAdapter;
import com.example.compshop.Models.Item;
import com.example.compshop.databinding.ActivityViewMyItemsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewMyItems extends AppCompatActivity {
    ActivityViewMyItemsBinding activityViewMyItemsBinding;
    RecyclerView recyclerView;
    ViewItemAdapter viewItemAdapter;
    List<Item> itemList;
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String uid1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityViewMyItemsBinding = ActivityViewMyItemsBinding.inflate(getLayoutInflater()); // Updated import
        setContentView(activityViewMyItemsBinding.getRoot());

        initViews(activityViewMyItemsBinding);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            uid1 = firebaseUser.getUid();
            retrieveItems();
        }

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    showConfirmationDialog(viewHolder);
                } else if (direction == ItemTouchHelper.LEFT) {
                    // Retrieve the item data associated with the swiped position
                    int swipedPosition = viewHolder.getAdapterPosition();
                    if (swipedPosition != RecyclerView.NO_POSITION) {
                        Item swipedItem = viewItemAdapter.getItemAtPosition(swipedPosition);
                        Intent updateIntent = new Intent(getApplicationContext(), AddItem.class);
                        updateIntent.putExtra("UPDATE_ITEM", swipedItem);
                        updateIntent.putExtra("action", "update");
                        startActivity(updateIntent);
                    }
                } else {
                    viewItemAdapter.notifyDataSetChanged(); // Refresh the adapter to undo the swipe action
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void showConfirmationDialog(RecyclerView.ViewHolder viewHolder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewMyItems.this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteItem(viewHolder);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    public void onClick(DialogInterface dialog, int id) {
                        viewItemAdapter.notifyDataSetChanged();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteItem(RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            Item item = itemList.get(position);

            DocumentReference itemRef = firestore.collection("users")
                    .document(uid1)
                    .collection("Products")
                    .document(item.getItem_Id());

            itemRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ViewMyItems.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                            itemList.remove(position);
                            viewItemAdapter.notifyItemRemoved(position);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ViewMyItems.this, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void initViews(ActivityViewMyItemsBinding activityViewMyItemsBinding) {
        recyclerView = activityViewMyItemsBinding.recyclerView;
        itemList = new ArrayList<>(); // Updated type
        viewItemAdapter = new ViewItemAdapter(getApplicationContext(), itemList); // Updated import
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(viewItemAdapter);
    }

    private void retrieveItems() {
        CollectionReference itemRef = firestore.collection("users")
                .document(uid1)
                .collection("Products");
        itemRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Item item = documentSnapshot.toObject(Item.class);
                            itemList.add(item);
                        }

                        if (itemList.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "No items available", Toast.LENGTH_SHORT).show();
                        }

                        viewItemAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to retrieve items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), AdminMain.class));
    }
}

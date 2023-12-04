package com.example.compshop.Admin.Handler;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Admin.AdminMain;
import com.example.compshop.Admin.Interface.OnOrderDeletedListener;
import com.example.compshop.Models.Order;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderDeletionHandler {

    public static void deleteOrder(Context context, String uid, Order order, OnOrderDeletedListener listener) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        // Assuming orders are directly under the "users" collection
        DocumentReference orderRef = firestore.collection("users")
                .document(uid)
                .collection("orders")
                .document(order.getOrderID());

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting order...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            orderRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.onOrderDeleted();
                            deleteAssociatedItems(orderRef, progressDialog, listener);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            listener.onOrderDeletionFailed("Failed to delete order: " + e.getMessage());
                        }
                    });
        } finally {
            progressDialog.dismiss();
        }
    }

    private static void deleteAssociatedItems(DocumentReference orderRef, ProgressDialog progressDialog, OnOrderDeletedListener listener) {
        CollectionReference itemOrdersRef = orderRef.collection("itemOrders");
        itemOrdersRef.get().addOnSuccessListener(querySnapshot -> {
            List<Task<Void>> deleteTasks = new ArrayList<>();
            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                deleteTasks.add(documentSnapshot.getReference().delete());
            }
            Tasks.whenAll(deleteTasks)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            listener.onOrderDeleted();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            listener.onOrderDeletionFailed("Failed to delete associated items: " + e.getMessage());
                        }
                    });
        });

    }
}

package com.example.compshop.Client;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Adapters.ItemA;
import com.example.compshop.Cart.DatabaseManager;
import com.example.compshop.Cart.Interface.ItemCallback;
import com.example.compshop.Cart.Tasks.DatabaseTask;
import com.example.compshop.Interface.OnQuantityChangeListener;
import com.example.compshop.Models.DepositRequest;
import com.example.compshop.Models.DepositResponse;
import com.example.compshop.Models.Item;
import com.example.compshop.Models.UserDets;
import com.example.compshop.R;
import com.example.compshop.Utils.ApiClient;
import com.example.compshop.Utils.FCMSend;
import com.example.compshop.databinding.ActivityCartBinding;
import com.facebook.shimmer.ShimmerFrameLayout;
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

import java.sql.SQLDataException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    ActivityCartBinding activityCartBinding;
    ItemA adapter;
    RecyclerView recyclerView;
    Button button;
    TextView textView;
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    private static final String TAG = "error";
    String notadmintoken, phone_contact, name, email;
    // String email1 = "Mugashab@gmail.com";
    //String fName = "Mugasha";
    //String lName = "Bradley";
    //String narration = "payment for Products";
    String txRef;
    String country = "UG";
    String currency = "UGX";

    DatabaseManager databaseManager;
    List<Item> items;
    // ShimmerFrameLayout shimmerLayout;

    FirebaseUser user;
    String uid1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityCartBinding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(activityCartBinding.getRoot());

        initViews(activityCartBinding);

        databaseManager = new DatabaseManager(getApplicationContext());
        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            e.printStackTrace();
        }


        adapter = new ItemA();
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        //shimmerLayout = activityCartBinding.shimmerLayout3;
        //shimmerLayout.startShimmer();

        getAllItems();

        // shimmerLayout.hideShimmer();
        //shimmerLayout.setVisibility(View.GONE);

     //   computeTotalPrice();

        adapter.setOnQuantityChangeListener(new OnQuantityChangeListener() {
            @Override
            public void onAddButtonClick(Item item, int position) {
                int quantity = item.getQuantity();
                quantity++; // Increment the quantity

                // Update the quantity and total in the item object
                item.setQuantity(quantity);

                databaseManager.updateItem(item);

                // Notify the adapter of the data change for the specific item
                adapter.notifyItemChanged(position);

                computeTotalPrice();
            }

            @Override
            public void onRemoveButtonClick(Item item, int position) {
                int quantity = item.getQuantity();
                if (quantity > 1) {
                    quantity--; // Decrement the quantity

                    // Update the quantity and total in the item object
                    item.setQuantity(quantity);

                    // foodViewModel.update(item);
                    databaseManager.updateItem(item);

                    // Notify the adapter of the data change for the specific item
                    adapter.notifyItemChanged(position);

                    computeTotalPrice();
                }
            }
        });

        // Inside onClick method of CartActivity
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double totalCost = computeTotalPrice();
                if ((totalCost <= 0)) {
                    Toast.makeText(getApplicationContext(), "No items to charge", Toast.LENGTH_SHORT).show();
                } else {
                    // Show the payment options dialog
                    showPaymentOptionsDialog(totalCost);
                }
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
                    databaseManager.deleteItem(adapter.getItem(viewHolder.getAdapterPosition()).getId());
                    computeTotalPrice();
                    Toast.makeText(CartActivity.this, "Cart Item deleted", Toast.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(activityCartBinding.recyclerview11);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid1 = user.getUid();
            retrieveUserDetails(uid1);
        }

    }

    private void initViews(ActivityCartBinding activityCartBinding) {
        textView = activityCartBinding.textView8;
        button = activityCartBinding.button6;
        recyclerView = activityCartBinding.recyclerview11;
    }

    private void retrieveUserDetails(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        name = documentSnapshot.getString("name");
                        email = documentSnapshot.getString("email");
                        phone_contact = documentSnapshot.getString("phonenumber");
                        // location = documentSnapshot.getString("location");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure scenario if necessary
                });
    }

    public double computeTotalPrice() {
        List<Item> itemcarts = adapter.getCurrentList();
        double totalPrice1 = 0.0;
        if (itemcarts.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
        } else {
            for (Item item : itemcarts) {
                String itemPrice = String.valueOf(item.getTotal());
                // Remove any non-numeric characters from the string
                String priceWithoutCurrency = itemPrice.replaceAll("[^\\d.]", "");
                // Parse the price as a double
                double mPrice3 = Double.parseDouble(priceWithoutCurrency);
                totalPrice1 += mPrice3;
                textView.setText(String.format("TOTAL COST:UGX %s", totalPrice1));
                textView.requestLayout();
            }
        }
        return totalPrice1;
    }

    private void showPaymentOptionsDialog(double totalp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment_options, null);
        builder.setView(dialogView);

        // RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        RadioButton radioPayOnDelivery = dialogView.findViewById(R.id.radioPayOnDelivery);
        RadioButton radioPayOnline = dialogView.findViewById(R.id.radioPayOnline);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (radioPayOnDelivery.isChecked()) {
                processCartOrder();
            } else if (radioPayOnline.isChecked()) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CartActivity.this);
                builder.setTitle("Confirm Payment")
                        .setMessage("Make sure the registered phone number has enough funds,Proceed?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                processPayment(totalp);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showUIToast("Transaction successfully canceled");
                            }
                        });
                AlertDialog dialog1 = builder.create();
                dialog1.show();
            } else {
                Toast.makeText(getApplicationContext(), "Please select a payment option", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @SuppressLint("DefaultLocale")
    private void processCartOrder() {
        List<Item> itemCart = adapter.getCurrentList();
        if (itemCart.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
        } else {
            double totalPrice = 0.0;
            for (Item item : itemCart) {
                String desiredAccountType = "Admin";
                String desiredItemId = item.getItem_Id();
                String itemPrice = item.getPrice();

                // Remove any non-numeric characters from the string
                String priceWithoutCurrency = itemPrice.replaceAll("[^\\d.]", "");
                // Parse the price as a double
                double itemPriceValue = Double.parseDouble(priceWithoutCurrency);
                totalPrice += itemPriceValue;

                CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");

                usersRef.whereEqualTo("accounttype", desiredAccountType)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null) {
                                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                            String userId = document.getId();

                                            RetrieveAdminToken(userId);

                                            // Create the order data
                                            final String timestamp = "" + System.currentTimeMillis();
                                            HashMap<String, Object> orderDetails = new HashMap<>();
                                            orderDetails.put("orderID", "" + timestamp);
                                            orderDetails.put("orderTime", "" + timestamp);
                                            orderDetails.put("orderStatus", "In Progress");
                                            orderDetails.put("orderTo", "" + userId);
                                            orderDetails.put("orderBy", "" + firebaseAuth.getUid());

                                            // Create a new order for the user (assuming "Orders" is the collection name)
                                            CollectionReference ordersRef = usersRef
                                                    .document(userId)
                                                    .collection("orders");
                                            ordersRef.document(timestamp).set(orderDetails)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                            CollectionReference itemOrdersRef = ordersRef.document(timestamp)
                                                                    .collection("itemOrders");

                                                            // Create a HashMap to store the details of the item
                                                            HashMap<String, Object> itemDetails = new HashMap<>();
                                                            itemDetails.put("itemId", item.getItem_Id());
                                                            itemDetails.put("itemName", item.getName());
                                                            itemDetails.put("itemDescription", item.getDescription());
                                                            itemDetails.put("itemPrice", item.getPrice());
                                                            itemDetails.put("itemQuantity", item.getQuantity());
                                                            itemDetails.put("itemTotal", item.getTotal());
                                                            itemDetails.put("itemImage", item.getImage());

                                                            itemOrdersRef.add(itemDetails)
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentReference documentReference) {
                                                                            Toast.makeText(getApplicationContext(), "Item Order Placed Successfully", Toast.LENGTH_SHORT).show();
                                                                            databaseManager.deleteAll();
                                                                            adapter.clearCart();
                                                                            prepareNotificationMessage("New Item Order: ID" + timestamp);
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            // Error adding item order
                                                                            Toast.makeText(getApplicationContext(), e.getMessage() + "", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Handle the failure to create the order
                                                        }
                                                    });
                                        }
                                    }
                                } else {
                                    // Handle the error
                                    Exception exception = task.getException();
                                    if (exception != null) {
                                        Log.d(TAG, exception + "");
                                    }
                                }
                            }
                        });
            }
            textView.setText(String.format("TOTAL COST: UGX %s", totalPrice));
            textView.requestLayout();
        }
    }

    private void RetrieveAdminToken(String userId) {
        if (userId != null && !userId.isEmpty()) {
            CollectionReference usersCollectionRef = firestore.collection("users");

            usersCollectionRef.whereEqualTo("uid", userId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            UserDets user = document.toObject(UserDets.class);

                            if (user != null) {
                                notadmintoken = user.getToken();
                              //  Toast.makeText(CartActivity.this,"Token: "+notadmintoken,Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error: Unable to retrieve user data", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "You Don't Have Personal Info", Toast.LENGTH_SHORT).show();
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
            Log.e("FirestoreError", "Invalid Uid value: " + userId);
        }
    }

    private void prepareNotificationMessage(String message) {
        if (notadmintoken != null) {
            FCMSend.pushNotification(
                    CartActivity.this,
                    notadmintoken,
                    "New Order",
                    message
            );
        }
    }

    private void processPayment(double samount) {
        txRef = email + " " + UUID.randomUUID().toString();

        String authToken = getString(R.string.auth_token);
        DepositRequest depositRequest = new DepositRequest(
                getString(R.string.secret_keu),
                currency,
                phone_contact,
                String.valueOf(samount),
                email,
                txRef,
                null
        );

        ApiClient apiClient = ApiClient.getInstance();
        Call<DepositResponse> call = apiClient.getDepositApiservice().makeDeposit(
                "Bearer " + authToken, depositRequest
        );
        call.enqueue(new Callback<DepositResponse>() {
            @Override
            public void onResponse(@NonNull Call<DepositResponse> call, @NonNull Response<DepositResponse> response) {
                if (response.isSuccessful()) {
                    DepositResponse depositResponse = response.body();
                    assert depositResponse != null;
                    showUIToast("Payment successful: " + depositResponse.getMessage());
                } else {
                    showUIToast("Payment failed. Status code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DepositResponse> call, Throwable t) {
                showUIToast("Failed to make payment. Please try again.");
            }
        });
    }

    private void showUIToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    /*    new RaveUiManager(this).setAmount(samount)
                .setCurrency(currency)
                .setCountry(country)
                .setEmail(email1)
                .setfName(fName)
                .setlName(lName)
                .setNarration(narration)
                .setPublicKey(Constants.PUBLIC_KEY)
                .setEncryptionKey(Constants.ENCRYPTION_KEY)
                .setTxRef(txRef)
                .setPhoneNumber(sphone, true)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .acceptUgMobileMoneyPayments(true)
                .acceptBankTransferPayments(true)
                .acceptUssdPayments(true)
                .onStagingEnv(false)
                .isPreAuth(true)
                .shouldDisplayFee(true)
                .showStagingLabel(false)
                .withTheme(R.style.Theme_MomoTest)
                .initialize();

    }*/
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                Toast.makeText(this, "SUCCESS ", Toast.LENGTH_SHORT).show();
                processCartOrder();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR ", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED ", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
*/
    public void getAllItems() {
        new DatabaseTask(databaseManager, new ItemCallback() {
            @Override
            public void onDatabaseTaskComplete(List<Item> itemList) {
                if (itemList != null) {
                    adapter.submitList(itemList);

                    computeTotalPrice();
                }
            }
        }).execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), ClientMain.class));
    }

}
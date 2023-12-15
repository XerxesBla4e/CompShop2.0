package com.example.compshop.Authentication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.compshop.Admin.AdminMain;
import com.example.compshop.Client.ClientMain;

import com.example.compshop.Models.Users;
import com.example.compshop.R;
import com.example.compshop.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding activityLoginBinding;
    Animation fadeIn, bottom_down;
    LinearLayout linearLayout;
    CardView cardView, cardView2, cardView3, cardView4;
    TextView textView, textView2;
    ConstraintLayout registerLayout, majorlayout;
    //  ProgressBar progressBar;
    private static final int DURATION = 1000;
    private FirebaseAuth mAuth;
    private static final String TAG = "LOGIN";
    FirebaseFirestore db;
    DocumentReference userRef;
    EditText email1, password1;
    String email, password;
    AppCompatButton btnlogin;
    TextView signup, forgotpass;
    LottieAnimationView lottieAnimationView;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(activityLoginBinding.getRoot());

        //Initialize animations
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down);

        initViews(activityLoginBinding);

        lottieAnimationView.setAnimation("arrow_right_anim.json");

        //Create handler for other layouts
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cardView.startAnimation(fadeIn);
                cardView2.startAnimation(fadeIn);
                cardView3.startAnimation(fadeIn);
                cardView4.startAnimation(fadeIn);
                textView.startAnimation(fadeIn);
                textView2.startAnimation(fadeIn);
                registerLayout.startAnimation(fadeIn);
            }
        }, DURATION);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x = new Intent(getApplicationContext(), ClientSignup.class);
                x.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(x);
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x = new Intent(getApplicationContext(), RecoverPassword.class);
                x.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(x);
            }
        });

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth = FirebaseAuth.getInstance();
                db = FirebaseFirestore.getInstance();

                email = activityLoginBinding.edittextemail.getText().toString();
                password = activityLoginBinding.edittextpass.getText().toString();

                if (!validateFields()) {
                    // Handle validation errors
                    return;
                }

                // Start the progress bar
//                progressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String uid = mAuth.getCurrentUser().getUid();
                                makeOnline(uid);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //         progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initViews(ActivityLoginBinding activityLoginBinding) {
        //Setting the bottom down animation on top layout
        linearLayout = activityLoginBinding.topLinearLayout3;
        linearLayout.startAnimation(bottom_down);
        cardView = activityLoginBinding.cardView;
        cardView2 = activityLoginBinding.cardView2;
        cardView3 = activityLoginBinding.cardView3;
        cardView4 = activityLoginBinding.cardView4;
        textView = activityLoginBinding.textView;
        textView2 = activityLoginBinding.textView2;
        registerLayout = activityLoginBinding.registerLayout;

        majorlayout = activityLoginBinding.majorlayout;


        email1 = activityLoginBinding.edittextemail;
        password1 = activityLoginBinding.edittextpass;

        btnlogin = activityLoginBinding.btnlogin;
        signup = activityLoginBinding.textView2;
        forgotpass = activityLoginBinding.forgotpass;

        lottieAnimationView = activityLoginBinding.lottieLayerNam;
    }

    private boolean validateFields() {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

        if (TextUtils.isEmpty(email)) {
            activityLoginBinding.edittextemail.setError("Email field can't be empty");
            return false;
        } else if (!email.matches(emailRegex)) {
            activityLoginBinding.edittextemail.setError("Invalid email format");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            activityLoginBinding.edittextpass.setError("Password field can't be empty");
            return false;
        } else if (!password.matches(passwordRegex)) {
            activityLoginBinding.edittextpass.setError("Password must contain at least 8 characters including one uppercase letter, one lowercase letter, one digit, and one special character");
            return false;
        }

        return true;
    }

    private void makeOnline(String uid) {
        userRef = db.collection("users").document(uid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        userRef.update(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        checkUserType(uid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserType(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //       progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    //since we are retrieving a snapshot of a single document
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.exists()) {
                        Users userProfile = snapshot.toObject(Users.class);
                        if (userProfile != null) {
                            String accountType = userProfile.getAccounttype();

                            Intent mainIntent;
                            if (accountType.equals("Client")) {
                                mainIntent = new Intent(getApplicationContext(), ClientMain.class);
                            } else {
                                mainIntent = new Intent(getApplicationContext(), AdminMain.class);
                            }

                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish(); // Finish the LoginActivity here to remove it from the back stack
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
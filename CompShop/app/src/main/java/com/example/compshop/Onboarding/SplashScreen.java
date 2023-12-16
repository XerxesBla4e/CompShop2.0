package com.example.compshop.Onboarding;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.compshop.Admin.AdminMain;
import com.example.compshop.Authentication.LoginActivity;
import com.example.compshop.Client.ClientMain;
import com.example.compshop.Models.Users;
import com.example.compshop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {

    private static final int REQUEST_ENABLE_LOCATION = 1001;
    private static int SPLASH_TIMER = 3000;

    private TextView appname;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private DocumentReference userRef;
    LottieAnimationView lottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splashscreen);

        appname = findViewById(R.id.name);
        lottie = findViewById(R.id.logo);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        appname.animate().translationY(-750).setDuration(2700).setStartDelay(0);
        lottie.setAnimation("cartanim.json");

        if (!isConnected()) {
            showNoInternetDialog();
        } else {
            requestLocationPermission();
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showNoInternetDialog() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
                builder.setTitle("No Internet Connection");
                builder.setMessage("Please connect to the internet to use this app. Some features may not be available without an internet connection.");
                builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        }.execute();
    }

    private void requestLocationPermission() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                if (ContextCompat.checkSelfPermission(SplashScreen.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SplashScreen.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_LOCATION);
                } else {
                    enableLocation();
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                startNextActivity();
            }
        }
    }

    private void enableLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Disabled");
            builder.setMessage("Please enable GPS for accurate location detection.");
            builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, REQUEST_ENABLE_LOCATION);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startNextActivity();
                }
            });
            builder.setCancelable(false);
            builder.show();
        } else {
            startNextActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startNextActivity();
            } else {
                Toast.makeText(this, "Location is still disabled", Toast.LENGTH_SHORT).show();
                startNextActivity();
            }
        }
    }

    private void startNextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                } else {
                    checkUserType();
                }
                finish();
            }
        }, SPLASH_TIMER);
    }

    private void checkUserType() {
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userRef = firestore.collection("users").document(user.getUid());

            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (snapshot != null && snapshot.exists()) {
                        Users userProfile = snapshot.toObject(Users.class);
                        if (userProfile != null) {
                            String accountType = userProfile.getAccounttype();
                            startAppropriateActivity(accountType);
                        }
                    }
                }
            });
        }
    }

    private void startAppropriateActivity(String accountType) {

        if (accountType.equals("Client")) {
            startActivity(new Intent(SplashScreen.this, ClientMain.class));
        } else {
            startActivity(new Intent(getApplicationContext(), AdminMain.class));

        }
        finish();
    }
}

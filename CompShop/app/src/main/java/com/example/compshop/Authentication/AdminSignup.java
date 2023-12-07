package com.example.compshop.Authentication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.compshop.R;
import com.example.compshop.databinding.AdminSignupBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminSignup extends AppCompatActivity {
    AdminSignupBinding adminSignupBinding;

    Button pickLocation, submit;
    public static final int PERMISSION_REQUEST_CODE = 444;
    String date, name, email, phonenumber, location, password, repass, gender;
    String district, city, state, country, address;
    TextView login;
    Spinner spinner;
    Double latitude, longitude;

    ProgressBar progressBar;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    LinearLayout linearLayout;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    LocationManager locationManager;

    public LocationListener locationListener;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static final int MY_PERMISSION_REQUEST_CODE = 71;
    public final static String specialk = "GRACE543";
    private static final String TAG = "dd";
    String token;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminSignupBinding = AdminSignupBinding.inflate(getLayoutInflater());
        setContentView(adminSignupBinding.getRoot());

        initViews(adminSignupBinding);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setInterval(UPDATE_INTERVAL);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                gender = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFields()) {
                    return;
                } else {
                    showSpecialKeyDialog();
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x = new Intent(getApplicationContext(), LoginActivity.class);
                x.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(x);
            }
        });

    /*    pickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //displayLocation();
            }
        });*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        token = task.getResult();
                        Log.d(TAG, "XerToken:" + token);
                    }
                });


    }

    private void uploadData(String uid) {
        final String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("phonenumber", phonenumber);
        user.put("location", location);
        user.put("city", "" + city);
        user.put("state", "" + state);
        user.put("country", "" + country);
        user.put("district", "" + district);
        user.put("gender", gender);
        user.put("DOB", date);
        user.put("timestamp", "" + timestamp);
        user.put("latitude", "" + longitude);
        user.put("longitude", "" + latitude);
        user.put("accounttype", "Admin");
        user.put("online", "true");
        user.put("token", token);

        db.collection("users")
                .document(uid)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Account created successfully", Toast.LENGTH_SHORT).show();
                        Intent x6 = new Intent(getApplicationContext(), LoginActivity.class);
                        x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x6);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Error while creating account: " + e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSpecialKeyDialog() {
        LayoutInflater inflater = LayoutInflater.from(AdminSignup.this);
        View dialogView = inflater.inflate(R.layout.special_key_popup, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminSignup.this);
        builder.setTitle("Enter Special Key");
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.editTextSpecialKey);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String specialKey = input.getText().toString().trim();
                if (TextUtils.isEmpty(specialKey)) {
                    Toast.makeText(AdminSignup.this, "Please enter a special key", Toast.LENGTH_SHORT).show();
                    showSpecialKeyDialog(); // Show dialog again if the special key is empty
                } else {
                    // Proceed with the special key
                    // Call a method or perform the desired action with the special key
                    if (specialKey.equals(specialk)) {
                        processData(); // Call the processData method to handle user creation
                    } else {
                        Toast.makeText(AdminSignup.this, "Invalid special key", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void processData() {
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (progressBar.getParent() != null) {
            ((ViewGroup) progressBar.getParent()).removeView(progressBar);
        }

        linearLayout.addView(progressBar, layoutParams);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String uid = authResult.getUser().getUid();
                        uploadData(uid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), e + "", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void stopLocationUpdates() {
        //fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayLocation();
    }

    private void initViews(AdminSignupBinding activitySignupBinding) {
        progressBar = new ProgressBar(getApplicationContext(), null, android.R.attr.progressBarStyleLarge);
        spinner = activitySignupBinding.spinnerGender;
        submit = activitySignupBinding.signupButton;
        linearLayout = activitySignupBinding.adminlinlayout;
        login = activitySignupBinding.textViewLogin1;
        pickLocation = activitySignupBinding.btnPickLocation;
    }

    private boolean validateFields() {
        // String noSpecialChars = "\\A[A-Za-z]{4,20}\\z";
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        //String phoneRegex = "^[+]?[0-9]{10,15}$";
        // String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

        name = adminSignupBinding.fnameEdit.getText().toString();
        email = adminSignupBinding.mailEdit.getText().toString();
        phonenumber = adminSignupBinding.phoneEdit.getText().toString();
        location = adminSignupBinding.locationEdit.getText().toString();
        password = adminSignupBinding.passwordEdit.getText().toString();
        repass = adminSignupBinding.passwordEdit1.getText().toString();

        if (TextUtils.isEmpty(name)) {
            adminSignupBinding.fnameEdit.setError("Username field can't be empty");
            adminSignupBinding.fnameEdit.requestFocus();
            return false;
        }
        /*else if (!name.matches(noSpecialChars)) {
            activitySignupBinding.fnameEdit.setError("Only letters are allowed!");
            return false;
        }*/

        if (TextUtils.isEmpty(email)) {
            adminSignupBinding.mailEdit.setError("Email field can't be empty");
            adminSignupBinding.mailEdit.requestFocus();
            return false;
        } else if (!email.matches(emailRegex)) {
            adminSignupBinding.mailEdit.setError("Invalid email format");
            adminSignupBinding.mailEdit.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phonenumber)) {
            adminSignupBinding.phoneEdit.setError("Phone number field can't be empty");
            adminSignupBinding.phoneEdit.requestFocus();
            return false;
        }/* else if (!phonenumber.matches(phoneRegex)) {
            activitySignupBinding.phoneEdit.setError("Invalid phone number format");
            activitySignupBinding.phoneEdit.requestFocus();
            return false;
        }*/

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Password field can't be empty", Toast.LENGTH_SHORT).show();
            adminSignupBinding.passwordEdit.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            Toast.makeText(getApplicationContext(), "Password Should be Longer than or equal to 8 characters", Toast.LENGTH_SHORT).show();
            adminSignupBinding.passwordEdit.requestFocus();
            return false;
        }
        /*else if (!password.matches(passwordRegex)) {
            Toast.makeText(getApplicationContext(),"Password must contain at least 8 characters including one uppercase letter," +
                    " one lowercase letter, one digit, and one special character",Toast.LENGTH_SHORT);
            activitySignupBinding.passwordEdit.requestFocus();
            return false;
        }*/

        if (TextUtils.isEmpty(repass)) {
            adminSignupBinding.passwordEdit1.setError("Confirm Password field can't be empty");
            adminSignupBinding.passwordEdit1.requestFocus();
            return false;
        } else if (!repass.equals(password)) {
            adminSignupBinding.passwordEdit1.setError("Passwords do not match");
            adminSignupBinding.passwordEdit1.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(getApplicationContext(), "Select Appropriate Gender Please", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startLocationUpdates();
        } else {
            startLegacyLocationUpdates();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();

                    getLocationAddress(longitude, latitude);
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void startLegacyLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    getLocationAddress(longitude, latitude);
                }
            };

            // Request location updates
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Request the missing permissions from the user
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
                return;
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else {
            Toast.makeText(getApplicationContext(), "Location Services Are Disabled\nPlease enable GPS or network provider", Toast.LENGTH_LONG).show();
        }
    }

    // Override the onRequestPermissionsResult() method to handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location updates again
                startLegacyLocationUpdates();
            } else {
                // Permission denied, handle it accordingly (e.g., show a message to the user)
                Toast.makeText(getApplicationContext(), "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getLocationAddress(Double longitude, Double latitude) {
        try {
            Geocoder geocoder = new Geocoder(AdminSignup.this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            address = addressList.get(0).getAddressLine(0);

            //  activitySignupBinding.locationEdit.setText(address);
            city = addressList.get(0).getLocality(); // city
            state = addressList.get(0).getAdminArea(); // region
            country = addressList.get(0).getCountryName(); // country
            district = addressList.get(0).getSubAdminArea(); // district

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

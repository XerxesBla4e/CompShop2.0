package com.example.compshop.Location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LocationManagerHelper {

    private Context context;
    private LocationListener locationListener;
    private LocationManager locationManager;

   // private String uid;
    FirebaseAuth firebaseAuth;

    FirebaseFirestore firestore;
    FirebaseUser user;
    String uid1;

    public LocationManagerHelper(Context context, LocationListener locationListener, LocationManager locationManager) {
        this.context = context;
        this.locationListener = locationListener;
        this.locationManager = locationManager;
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid1 = user.getUid();
        }

        initializeLocationManager();
    }

    public void checkAndRequestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    public void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Check if location services are enabled
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Request location updates
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else {
                // Prompt user to enable location services
                Toast.makeText(context, "Please enable location services", Toast.LENGTH_SHORT).show();
                Log.e("LocationManagerHelper", "locationManager is null");
                initializeLocationManager();
            }
        }
    }

    private void initializeLocationManager() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateUserLocation(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    // Handle status changes as needed
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // Handle provider enabled as needed
                }

                @Override
                public void onProviderDisabled(String provider) {
                    // Handle provider disabled as needed
                }
            };
            requestLocationUpdates();
        } else {
            Log.e("LocationManagerHelper", "Failed to initialize locationManager");
            // Handle the failure to initialize locationManager
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(context, "Location permission is required for the app to function correctly." +
                        " Please grant the permission in the app settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    public void updateUserLocation(double latitude, double longitude) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
       DocumentReference documentRef = firestore.collection("users").document(uid1);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("latitude", "" + latitude);
        updateData.put("longitude", "" + longitude);

        documentRef.update(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                        // Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        // Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


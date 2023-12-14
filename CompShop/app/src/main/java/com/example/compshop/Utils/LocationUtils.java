package com.example.compshop.Utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    public static String getAddressFromLatLng(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String addressText = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Construct the address text
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append(", ");
                }
                sb.deleteCharAt(sb.length() - 2); // Remove the last comma and space
                addressText = sb.toString();
            }
        } catch (IOException e) {
            Log.e("LocationUtils", "Error getting address from location", e);
        }

        return addressText;
    }
}

package com.dianaszczepankowska.compass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import androidx.core.app.ActivityCompat;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationClass {

    static final int REQ_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;
    private final LocationRequest locationRequest;

    LocationClass(Context context) {
        fusedLocationClient = getFusedLocationProviderClient(context);
        locationRequest = createLocationRequest();
        locationCallback = getLocationCallback();
    }


    interface LocationListener {
        void findLocation(CoordinatesModel myLocation);
    }

    private LocationClass.LocationListener locationListener;

    void setListener(LocationListener listener) {
        this.locationListener = listener;
    }


    static void requestPermissions(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_CODE);
        }
    }


    void checkLocationSettingsAndStartLocationUpdates(Context context) {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        //If location is on -> start location updates
        task.addOnSuccessListener((Activity) context, locationSettingsResponse -> startLocationUpdates());

        //If location is off -> show dialog with settings
        task.addOnFailureListener((Activity) context, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult((Activity) context,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        });
    }

    LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    //Permissions are already checked
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    LocationCallback getLocationCallback() {
        return new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    if (locationListener != null) {
                        locationListener.findLocation(new CoordinatesModel((float) location.getLatitude(), (float) location.getLongitude(), (float) location.getAltitude()));
                    }
                }
            }
        };
    }

}

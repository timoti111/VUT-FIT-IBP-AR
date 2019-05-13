package cz.vutbr.fit.xhalas10.bp.android.implementation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.IDeviceLocation;
import cz.vutbr.fit.xhalas10.bp.earth.wgs84.GeoidUndulation;

public class DeviceLocation implements IDeviceLocation, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final int REQUEST_CHECK_SETTINGS = 8465;
    private static final long UPDATE_INTERVAL = 500, FASTEST_INTERVAL = 100;
    private static final float SMALLEST_DISPLACEMENT = 0.5f;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean requestingLocationUpdates = false;
    private LocationCallback locationCallback;
    private Activity activity;
    private cz.vutbr.fit.xhalas10.bp.earth.wgs84.Location actualLocation;
    private double altitude;
    private double hAccuracy;
    private double vAccuracy;

    public DeviceLocation(Activity activity) {
        this.activity = activity;
        actualLocation = new cz.vutbr.fit.xhalas10.bp.earth.wgs84.Location();

        googleApiClient = new GoogleApiClient.Builder(activity).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    updateLocation(location);
                }
            }
        };
    }

    private void updateLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if (location.hasAltitude())
                altitude = location.getAltitude() - GeoidUndulation.getInstance().getUndulation(latitude, longitude);
            if (location.hasAccuracy())
                hAccuracy = location.getAccuracy();
            if (location.hasVerticalAccuracy())
                vAccuracy = location.getVerticalAccuracyMeters();
            actualLocation.set(latitude, longitude, altitude);
        }
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED
                && ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED;
    }

    @Override
    public cz.vutbr.fit.xhalas10.bp.earth.wgs84.Location getLocation() {
        return actualLocation;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!checkPermissions())
            return;
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, this::updateLocation);

        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(activity, locationSettingsResponse -> startLocationUpdates());
        task.addOnFailureListener(activity, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(activity,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void onResume() {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
        startLocationUpdates();
    }

    public void onPause() {
        stopLocationUpdates();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (!requestingLocationUpdates && checkPermissions() && locationRequest != null) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            requestingLocationUpdates = true;
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
    }

    private void stopLocationUpdates() {
        if (requestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            requestingLocationUpdates = false;
        }
    }
}

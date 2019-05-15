/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Objects;

import cz.vutbr.fit.xhalas10.bp.android.implementation.CameraPreview;
import cz.vutbr.fit.xhalas10.bp.android.implementation.DeviceLocation;
import cz.vutbr.fit.xhalas10.bp.android.implementation.MotionSensors;
import cz.vutbr.fit.xhalas10.bp.android.implementation.Utils;

public class AndroidLauncher extends AndroidApplication implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private MotionSensors motionSensors;
    private CameraPreview cameraPreview;
    private DeviceLocation androidLocation;
    private ArrayList<String> permissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkPlayServices())
            finish();

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.CAMERA);

        ArrayList<String> permissionsToRequest = permissionsToRequest(permissions);

        if (permissionsToRequest.size() > 0) {
            requestPermissions(permissionsToRequest.toArray(new String[0]), ALL_PERMISSIONS_RESULT);
        }

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useRotationVectorSensor = false;
        config.useGyroscope = false;
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useImmersiveMode = true;
        config.useWakelock = true;
        config.disableAudio = true;
        config.useGLSurfaceView20API18 = true;
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;

        motionSensors = new MotionSensors((SensorManager) Objects.requireNonNull(this.getContext().getSystemService(SENSOR_SERVICE)));
        cameraPreview = new CameraPreview((CameraManager) this.getContext().getSystemService(CAMERA_SERVICE));
        androidLocation = new DeviceLocation(this);
        Utils utils = new Utils(this, cameraPreview, androidLocation, motionSensors);
        initialize(new ARNature(utils), config);
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        motionSensors.onPause();
        cameraPreview.onPause();
        androidLocation.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        motionSensors.onResume();
        cameraPreview.onResume();
        androidLocation.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DeviceLocation.REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK)
                androidLocation.startLocationUpdates();
            else
                finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED)
                    finish();
            }
        }
    }
}

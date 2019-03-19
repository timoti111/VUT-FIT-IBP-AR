package cz.vutbr.fit.xhalas10.bp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

public class AndroidLauncher extends AndroidApplication implements ActivityCompat.OnRequestPermissionsResultCallback {
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    SensorManager sensorManager;
    CameraManager cameraManager;
    LocationManager locationManager;
    AndroidSensorManager androidSensorManager;
    AndroidHardwareCamera androidHardwareCamera;
    AndroidPersonLocation androidLocation;
    private ArrayList<String> permissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkPlayServices())
            finish();
        // we add permissions we need to request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.CAMERA);

        // lists for permissions
        ArrayList<String> permissionsToRequest = permissionsToRequest(permissions);

        if (permissionsToRequest.size() > 0) {
            requestPermissions(permissionsToRequest.toArray(new String[0]), ALL_PERMISSIONS_RESULT);
        }

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useRotationVectorSensor = true;
        config.useGyroscope = true;
        config.useAccelerometer = false;
        config.sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
        config.useImmersiveMode = true;
        //config.useWakelock = true;
        config.disableAudio = true;
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;
        sensorManager = (SensorManager) this.getContext().getSystemService(SENSOR_SERVICE);
        cameraManager = (CameraManager) this.getContext().getSystemService(CAMERA_SERVICE);
        locationManager = (LocationManager) this.getContext().getSystemService(LOCATION_SERVICE);

        androidSensorManager = new AndroidSensorManager(sensorManager);
        androidHardwareCamera = new AndroidHardwareCamera(cameraManager);
        androidLocation = new AndroidPersonLocation(this);
        initialize(new MyGdxGame(androidSensorManager, androidHardwareCamera, androidLocation), config);
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        androidSensorManager.onPause();
        androidHardwareCamera.onPause();
        androidLocation.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        androidSensorManager.onResume();
        androidHardwareCamera.onResume();
        androidLocation.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AndroidPersonLocation.REQUEST_CHECK_SETTINGS) {
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

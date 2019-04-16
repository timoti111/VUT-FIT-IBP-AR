package cz.vutbr.fit.xhalas10.bp;

import android.content.Context;
import android.widget.Toast;

import java.util.concurrent.CopyOnWriteArrayList;

public class AndroidUtils implements Utils {
    AndroidLauncher context;
    AndroidHardwareCamera androidHardwareCamera;
    AndroidPersonLocation androidPersonLocation;
    AndroidSensorManager androidSensorManager;

    public AndroidUtils(AndroidLauncher context, AndroidHardwareCamera androidHardwareCamera, AndroidPersonLocation androidPersonLocation, AndroidSensorManager androidSensorManager) {
        this.context = context;
        this.androidHardwareCamera = androidHardwareCamera;
        this.androidPersonLocation = androidPersonLocation;
        this.androidSensorManager = androidSensorManager;
    }

    @Override
    public void showToast(String string) {
        new Thread() {
            public void run() {
                context.runOnUiThread(() -> Toast.makeText(context, string, Toast.LENGTH_SHORT).show());
            }
        }.start();
    }

    @Override
    public SensorManager getSensorManager() {
        return androidSensorManager;
    }

    @Override
    public HardwareCamera getHardwareCamera() {
        return androidHardwareCamera;
    }

    @Override
    public PersonLocation getPersonLocation() {
        return androidPersonLocation;
    }
}

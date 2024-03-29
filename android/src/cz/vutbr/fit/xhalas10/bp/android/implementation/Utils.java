/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.android.implementation;

import android.widget.Toast;

import cz.vutbr.fit.xhalas10.bp.AndroidLauncher;
import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.ICameraPreview;
import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.IDeviceLocation;
import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.IMotionSensors;
import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.IUtils;

/**
 * This class serves for communication with module core.
 * It also implements IUtils interface from module core and package
 * cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces
 */
public class Utils implements IUtils {
    private AndroidLauncher context;
    private CameraPreview cameraPreview;
    private DeviceLocation deviceLocation;
    private MotionSensors motionSensors;

    public Utils(AndroidLauncher context, CameraPreview hardwareCamera, DeviceLocation deviceLocation, MotionSensors motionSensors) {
        this.context = context;
        this.cameraPreview = hardwareCamera;
        this.deviceLocation = deviceLocation;
        this.motionSensors = motionSensors;
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
    public IMotionSensors getSensorManager() {
        return motionSensors;
    }

    @Override
    public ICameraPreview getCameraPreview() {
        return cameraPreview;
    }

    @Override
    public IDeviceLocation getPersonLocation() {
        return deviceLocation;
    }
}

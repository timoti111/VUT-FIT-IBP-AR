/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces;

public interface IUtils {
    void showToast(String string);

    IMotionSensors getSensorManager();

    ICameraPreview getCameraPreview();

    IDeviceLocation getPersonLocation();
}

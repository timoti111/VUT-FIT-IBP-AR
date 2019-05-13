package cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces;

public interface IUtils {
    void showToast(String string);

    IMotionSensors getSensorManager();

    ICameraPreview getCameraPreview();

    IDeviceLocation getPersonLocation();
}

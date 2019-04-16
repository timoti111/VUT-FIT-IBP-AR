package cz.vutbr.fit.xhalas10.bp;

public interface Utils {
    void showToast(String string);

    SensorManager getSensorManager();

    HardwareCamera getHardwareCamera();

    PersonLocation getPersonLocation();
}

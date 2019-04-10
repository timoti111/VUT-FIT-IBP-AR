package cz.vutbr.fit.xhalas10.bp;

public interface HardwareCamera {
    void init();

    void renderBackground();

    void destroy();

    float[] getCameraSensorSize();

    float getCameraFocalLength();
}

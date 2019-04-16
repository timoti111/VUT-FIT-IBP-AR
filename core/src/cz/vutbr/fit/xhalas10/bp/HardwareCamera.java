package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.utils.Disposable;

public interface HardwareCamera extends Disposable {
    void init();

    void renderBackground();

    float[] getCameraSensorSize();

    float getCameraFocalLength();
}

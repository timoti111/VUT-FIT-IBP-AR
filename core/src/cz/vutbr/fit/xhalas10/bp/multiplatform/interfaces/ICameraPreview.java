package cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces;

import com.badlogic.gdx.utils.Disposable;

public interface ICameraPreview extends Disposable {
    void init();

    void renderBackground();

    float[] getCameraSensorSize();

    float getCameraFocalLength();
}

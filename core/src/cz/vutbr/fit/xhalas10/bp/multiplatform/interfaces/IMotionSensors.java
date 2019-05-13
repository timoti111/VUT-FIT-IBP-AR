package cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces;

import com.badlogic.gdx.math.Quaternion;

public interface IMotionSensors {
    void useCompass(boolean useCompass);
    Quaternion getQuaternion();
}

package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.math.Quaternion;

public interface SensorManager {
    void useCompass(boolean useCompass);
    Quaternion getQuaternion();
}

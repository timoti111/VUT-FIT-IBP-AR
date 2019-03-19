package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.math.Matrix4;

public interface SensorManager {
    public Matrix4 getRotationMatrix();
    public float getAzimuth();
    public void reset();
    public void useCompass(boolean useCompass);
    public float getCorrection();
}

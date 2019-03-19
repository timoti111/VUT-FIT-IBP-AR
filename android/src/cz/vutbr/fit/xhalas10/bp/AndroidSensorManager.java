package cz.vutbr.fit.xhalas10.bp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.badlogic.gdx.math.Matrix4;

public class AndroidSensorManager implements SensorEventListener, cz.vutbr.fit.xhalas10.bp.SensorManager {
    private SensorManager sensorManager;
    private Sensor actualRotationSensor;
    private int actualSamplingRate;
    private Sensor secondaryRotationSensor;
    private int secondarySamplingRate;
    private Sensor gameRotationVectorSensor;
    private Sensor rotationVectorSensor;
    private Matrix4 rotationMatrix;
    private float[] test = new float[4];
    private float[] mRotationMatrix = new float[16];
    private float[] orientation = new float[3];
    private boolean compass = false;


    public AndroidSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        gameRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        actualRotationSensor = gameRotationVectorSensor;
        secondaryRotationSensor = rotationVectorSensor;
        actualSamplingRate = SensorManager.SENSOR_DELAY_FASTEST;
        secondarySamplingRate = SensorManager.SENSOR_DELAY_NORMAL;
        rotationMatrix = new Matrix4();
    }

    public void onResume() {
        sensorManager.registerListener(this, actualRotationSensor, actualSamplingRate);
        sensorManager.registerListener(this, secondaryRotationSensor, secondarySamplingRate);
    }

    public void onPause() {
        // make sure to turn our sensor off when the activity is paused
        sensorManager.unregisterListener(this);
    }

    public void reset() {
        sensorManager.unregisterListener(this);
        onResume();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR && !compass) {
            invertQuat(event.values, test);
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, test);
            SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_MINUS_X, mRotationMatrix);
            rotationMatrix.set(mRotationMatrix);
        }

        if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
            invertQuat(event.values, test);
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, test);
            SensorManager.getOrientation(mRotationMatrix, orientation);
            if (compass) {
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_MINUS_X, mRotationMatrix);
                rotationMatrix.set(mRotationMatrix);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void invertQuat(float[] inQuat, float[] outQuat)
    {
        float denominator = 1.0f / (inQuat[0] * inQuat[0] + inQuat[1] * inQuat[1] + inQuat[2] * inQuat[2] + inQuat[3] * inQuat[3]);
        outQuat[3] = inQuat[3] * denominator;
        outQuat[0] = -inQuat[0] * denominator;
        outQuat[1] = -inQuat[1] * denominator;
        outQuat[2] = -inQuat[2] * denominator;
    }

    public Matrix4 getRotationMatrix()
    {
        return rotationMatrix;
    }

    public float getAzimuth() {
        return (float)Math.toDegrees(orientation[0]);
    }

    public void useCompass(boolean useCompass) {
        compass = useCompass;
        if (useCompass) {
            actualRotationSensor = rotationVectorSensor;
            secondaryRotationSensor = gameRotationVectorSensor;
        }
        else {
            actualRotationSensor = gameRotationVectorSensor;
            secondaryRotationSensor = rotationVectorSensor;
        }
        reset();
    }

    public float getCorrection() {
        if (actualRotationSensor.equals(rotationVectorSensor))
            return -90.0f;
        return 0.0f;
    }
}

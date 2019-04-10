package cz.vutbr.fit.xhalas10.bp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.badlogic.gdx.math.Quaternion;

public class AndroidSensorManager implements SensorEventListener, cz.vutbr.fit.xhalas10.bp.SensorManager {
    private SensorManager sensorManager;
    private Sensor actualRotationSensor;
    private int samplingRate;
    private Sensor gameRotationVectorSensor;
    private Sensor rotationVectorSensor;
    private Quaternion quaternion;


    AndroidSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        gameRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        actualRotationSensor = gameRotationVectorSensor;
        samplingRate = SensorManager.SENSOR_DELAY_FASTEST;
        quaternion = new Quaternion();
    }

    void onResume() {
        sensorManager.registerListener(this, actualRotationSensor, samplingRate);
    }

    void onPause() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR || sensorType == Sensor.TYPE_ROTATION_VECTOR)
            quaternion.set(event.values[1], -event.values[0], -event.values[2], -event.values[3]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public Quaternion getQuaternion() {
        return quaternion;
    }

    public void useCompass(boolean useCompass) {
        if (useCompass) {
            actualRotationSensor = rotationVectorSensor;
            sensorManager.unregisterListener(this);
            sensorManager.registerListener(this, rotationVectorSensor, samplingRate);
        }
        else {
            actualRotationSensor = gameRotationVectorSensor;
            sensorManager.unregisterListener(this);
            sensorManager.registerListener(this, gameRotationVectorSensor, samplingRate);
        }
    }
}

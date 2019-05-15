/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.android.implementation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.IMotionSensors;

public class MotionSensors implements SensorEventListener, IMotionSensors {
    private static final Quaternion correctionQuaternion = new Quaternion(Vector3.X, -90.0f).mulLeft(new Quaternion(Vector3.Y, -90.0f));
    private static final Quaternion tmpQuat = new Quaternion();
    private SensorManager sensorManager;
    private Sensor actualRotationSensor;
    private int samplingRate;
    private Sensor gameRotationVectorSensor;
    private Sensor rotationVectorSensor;
    private Quaternion quaternion;

    public MotionSensors(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        gameRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        actualRotationSensor = gameRotationVectorSensor;
        samplingRate = (int) (1000000.0 / (double) CameraPreview.FPS);
        quaternion = new Quaternion();
    }

    public void onResume() {
        sensorManager.registerListener(this, actualRotationSensor, samplingRate);
    }

    public void onPause() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ROTATION_VECTOR || sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            quaternion.set(tmpQuat.set(event.values[1], -event.values[0], -event.values[2], -event.values[3]).mulLeft(correctionQuaternion));
        }
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
        } else {
            actualRotationSensor = gameRotationVectorSensor;
            sensorManager.unregisterListener(this);
            sensorManager.registerListener(this, gameRotationVectorSensor, samplingRate);
        }
    }
}

/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces;

import com.badlogic.gdx.math.Quaternion;

/**
 * This is interface for core module for communication with platform specific implementation
 * of motion sensors.
 */
public interface IMotionSensors {
    void useCompass(boolean useCompass);

    Quaternion getQuaternion();
}

/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.scene.interfaces;

import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

/**
 * This is interface for SceneManager for working with any object in scene. It is mainly for
 * positioning of object in scene.
 */
public interface ISceneObject {
    void calculateOriginRelativePosition();

    Vector3 getOriginRelativePosition();

    Vector3d getScenePosition();

    void update();
}

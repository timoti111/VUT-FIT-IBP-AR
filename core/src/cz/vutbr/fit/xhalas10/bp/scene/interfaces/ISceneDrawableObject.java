/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.scene.interfaces;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Disposable;

/**
 * This is interface for SceneManager for working with drawable object in scene.
 */
public interface ISceneDrawableObject extends ISceneObject, Disposable {
    RenderableProvider getRenderableProvider();

    float getMaximumDrawableDistance();

    void create();

    boolean isAlive();
}

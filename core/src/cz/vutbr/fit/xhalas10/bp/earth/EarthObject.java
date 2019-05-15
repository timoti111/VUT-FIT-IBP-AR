/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.earth;

import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.earth.wgs84.Location;
import cz.vutbr.fit.xhalas10.bp.scene.SceneManager;
import cz.vutbr.fit.xhalas10.bp.scene.interfaces.ISceneObject;
import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

/**
 * This class implements methods from interface ISceneObject.
 * Implements it as object which positioning is based on geographic location.
 */
public abstract class EarthObject implements ISceneObject {
    Vector3 originRelativePosition;
    private Vector3d scenePosition;
    private Location location;

    EarthObject() {
        originRelativePosition = new Vector3();
        scenePosition = new Vector3d();
        location = new Location();
    }

    void setPosition(double latitude, double longitude, double altitude) {
        location.set(latitude, longitude, altitude);
        scenePosition.set(location.toCartesian());
    }

    public void setPosition(Location location) {
        this.location.set(location);
        scenePosition.set(this.location.toCartesian());
    }

    @Override
    public void calculateOriginRelativePosition() {
        originRelativePosition.set(scenePosition.cpy().sub(SceneManager.getInstance().getOrigin()).toVector3());
        originRelativePosition.mul(SceneManager.getInstance().getCorrectionMatrix());
        originRelativePosition.scl((float) SceneManager.getScale());
    }

    @Override
    public Vector3 getOriginRelativePosition() {
        return originRelativePosition;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public Vector3d getScenePosition() {
        return scenePosition;
    }
}

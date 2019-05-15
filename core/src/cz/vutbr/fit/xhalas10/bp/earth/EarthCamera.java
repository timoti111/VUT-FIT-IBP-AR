/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.earth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.ICameraPreview;
import cz.vutbr.fit.xhalas10.bp.scene.interfaces.ISceneCamera;

public class EarthCamera extends EarthObject implements ISceneCamera {
    private static Quaternion tmpQuat = new Quaternion();
    private Camera camera;
    private float height;
    private Quaternion sensorQuaternion;
    private Quaternion angleCorrectionQuaternion;

    public EarthCamera(ICameraPreview hardwareCamera) {
        double ratio = (double) Gdx.graphics.getWidth() / (double) Gdx.graphics.getHeight();
        double fovy = Math.toDegrees(2.0 * Math.atan((hardwareCamera.getCameraSensorSize()[0] * (1.0 / ratio)) / (2.0 * (double) hardwareCamera.getCameraFocalLength())));
        camera = new PerspectiveCamera((float) fovy, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.01f;
        camera.far = 20000f;
        angleCorrectionQuaternion = new Quaternion();
        setCorrectionAngle(0.0f);
    }

    @Override
    public void update() {
        camera.position.set(originRelativePosition);
        camera.position.add(0.0f, height, 0.0f);
        camera.direction.set(0.0f, 0.0f, -1.0f);
        camera.up.set(Vector3.Y);
        camera.rotate(tmpQuat.set(sensorQuaternion).mulLeft(angleCorrectionQuaternion));
        camera.update();
    }

    public void setCorrectionAngle(float angle) {
        angleCorrectionQuaternion.setFromAxis(0.0f, 1.0f, 0.0f, angle);
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setSensorQuaternion(Quaternion sensorQuaternion) {
        this.sensorQuaternion = sensorQuaternion;
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public Vector3 getUpVector() {
        return getLocation().getUpPointingVector();
    }

    @Override
    public Vector3 getSouthVector() {
        return getLocation().getSouthPointingVector();
    }

    @Override
    public Vector3 getEastVector() {
        return getLocation().getEastPointingVector();
    }
}

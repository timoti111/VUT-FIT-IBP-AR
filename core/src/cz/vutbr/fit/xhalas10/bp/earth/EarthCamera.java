package cz.vutbr.fit.xhalas10.bp.earth;

import com.badlogic.gdx.graphics.Camera;

import cz.vutbr.fit.xhalas10.bp.scene.IWorldCamera;
import cz.vutbr.fit.xhalas10.bp.scene.WorldManager;

public class EarthCamera extends EarthObject implements IWorldCamera {
    Camera camera;

    public EarthCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void setPosition(double latitude, double longitude, double altitude) {
        super.setPosition(latitude, longitude, altitude);
    }

    @Override
    public void update() {
        calculateOriginRelativePosition(WorldManager.getInstance().getOrigin(), WorldManager.getInstance().getCorrectionQuaternion());
        camera.position.set(originRelativePosition);
        camera.update();
    }

    @Override
    public Camera getCamera() {
        return camera;
    }
}

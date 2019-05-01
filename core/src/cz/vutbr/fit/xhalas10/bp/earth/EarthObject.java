package cz.vutbr.fit.xhalas10.bp.earth;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.scene.IWorldObject;
import cz.vutbr.fit.xhalas10.bp.scene.WorldManager;
import cz.vutbr.fit.xhalas10.bp.scene.WorldObjectFilter;
import cz.vutbr.fit.xhalas10.bp.utils.Location;
import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

public abstract class EarthObject implements IWorldObject {
    protected Vector3 originRelativePosition;
    protected Vector3d worldPosition;
    Location location;

    public EarthObject() {
        originRelativePosition = new Vector3();
        worldPosition = new Vector3d();
        location = new Location();
    }

    public void setPosition(double latitude, double longitude, double altitude) {
        location.set(latitude, longitude, altitude);
        worldPosition.set(location.toCartesian());
    }

    public void setPosition(Location location) {
        this.location.set(location);
        worldPosition.set(this.location.toCartesian());
    }

    @Override
    public void calculateOriginRelativePosition() {
        originRelativePosition.set(worldPosition.cpy().sub(WorldManager.getInstance().getOrigin()).toVector3());
        originRelativePosition.mul(WorldManager.getInstance().getCorrectionMatrix());
    }

    @Override
    public Vector3 getOriginRelativePosition() {
        return originRelativePosition;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public Vector3d getWorldPosition() {
        return worldPosition;
    }

    @Override
    public Vector3 getUpVector() {
        return location.getUpPointingVector();
    }

    @Override
    public Vector3 getNorthVector() {
        return location.getNorthPointingVector();
    }

    @Override
    public Vector3 getEastVector() {
        return location.getEastPointingVector();
    }
}

package cz.vutbr.fit.xhalas10.bp.scene.interfaces;

import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

public interface ISceneObject {
    void calculateOriginRelativePosition();

    Vector3 getOriginRelativePosition();

    Vector3d getScenePosition();

    void update();
}
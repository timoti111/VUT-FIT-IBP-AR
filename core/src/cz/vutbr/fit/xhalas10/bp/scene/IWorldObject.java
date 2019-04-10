package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

public interface IWorldObject {
    void calculateOriginRelativePosition(Vector3d origin, Quaternion correctionQuaternion);

    Vector3d getWorldPosition();

    Vector3 getUpVector();

    void update();

    Vector3 getNorthVector();

    Vector3 getEastVector();
}

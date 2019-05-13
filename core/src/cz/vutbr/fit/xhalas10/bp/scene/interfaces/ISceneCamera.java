package cz.vutbr.fit.xhalas10.bp.scene.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public interface ISceneCamera extends ISceneObject {
    Camera getCamera();

    Vector3 getUpVector();

    Vector3 getSouthVector();

    Vector3 getEastVector();
}

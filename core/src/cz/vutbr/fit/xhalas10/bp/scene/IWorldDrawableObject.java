package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public interface IWorldDrawableObject extends IWorldObject, Disposable {
    RenderableProvider getRenderableProvider();

    int getPriority();
}

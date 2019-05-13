package cz.vutbr.fit.xhalas10.bp.scene.interfaces;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Disposable;

public interface ISceneDrawableObject extends ISceneObject, Disposable {
    RenderableProvider getRenderableProvider();

    float getMaximumDrawableDistance();

    public void create();

    public boolean isAlive();

    public String getName();
}

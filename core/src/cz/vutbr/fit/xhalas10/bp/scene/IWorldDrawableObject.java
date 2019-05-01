package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

public interface IWorldDrawableObject extends IWorldObject, Disposable {
    RenderableProvider getRenderableProvider();

    int getPriority();

    public void create();

    public boolean isAlive();
}

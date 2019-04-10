package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;

public interface IWorldDrawableObject extends IWorldObject {
    RenderableProvider getRenderableProvider();
}

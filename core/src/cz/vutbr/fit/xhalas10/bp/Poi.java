package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;

public class Poi extends Decal {
    public Poi() {
        super();
        Texture texture = new Texture(Gdx.files.internal("poi.png"));
        TextureRegion textureRegion = new TextureRegion(texture);
        setTextureRegion(textureRegion);
        setBlending(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        dimensions.x = textureRegion.getRegionWidth();
        dimensions.y = textureRegion.getRegionHeight();
        setColor(1, 1, 1, 1);
        transformationOffset = new Vector2(0,-20);
    }
}

package cz.vutbr.fit.xhalas10.bp.earth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import cz.vutbr.fit.xhalas10.bp.scene.IWorldDrawableObject;

public class Compass extends EarthObject implements IWorldDrawableObject {
    private static final float size = 0.6f;
    private static Model model = buildPoiModel();
    private ModelInstance modelInstance;
    private Camera camera;
    public Compass(Camera camera) {
        this.camera = camera;
        modelInstance = new ModelInstance(model);
    }

    private static Model buildPoiModel() {
        Texture texture = new Texture(Gdx.files.internal("compass_grey2icent.png"));
        Material material = new Material(TextureAttribute.createDiffuse(texture));
        material.set(new BlendingAttribute(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA));
        ModelBuilder modelBuilder = new ModelBuilder();
        return modelBuilder.createRect(
                -size / 2.0f, -1.5f, size / 2.0f,
                size / 2.0f, -1.5f, size / 2.0f,
                size / 2.0f, -1.5f, -size / 2.0f,
                -size / 2.0f, -1.5f, -size / 2.0f,
                0.0f, 1.0f, 0.0f,
                Gdx.gl.GL_TRIANGLES,
                material,
                VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal |
                        VertexAttributes.Usage.TextureCoordinates
        );
    }

    @Override
    public void update() {
        modelInstance.transform.setTranslation(camera.position);
    }

    @Override
    public RenderableProvider getRenderableProvider() {
        return modelInstance;
    }
}

package cz.vutbr.fit.xhalas10.bp.earth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.MySkin;
import cz.vutbr.fit.xhalas10.bp.OSMData;
import cz.vutbr.fit.xhalas10.bp.scene.IWorldDrawableObject;
import cz.vutbr.fit.xhalas10.bp.scene.WorldManager;
import cz.vutbr.fit.xhalas10.bp.utils.TextureTextGenerator;

public class Poi extends EarthObject implements IWorldDrawableObject {
    private static final float POI_HEIGHT_AT_1M = 0.05f;
    private static final float FONT_HEIGHT_AT_1M = 0.02f;
    private static final Quaternion billboardQuaternion = new Quaternion();
    private static final Vector3 objToCam = new Vector3();
    private static final Vector3 defaultLookAt = new Vector3(Vector3.Z);
    private static final Vector3 scale = new Vector3();
    private static Model model = buildPoiModel();
    ModelInstance modelInstance;
    private OSMData osmData;

    static public Poi newBasicPoi(String text, double latitude, double longitude, double altitude) {
        Texture texture = MySkin.getInstance().get("basicPoi", Texture.class);
        Poi poi = new Poi(text, texture, latitude, longitude, altitude);
        poi.setPosition(latitude, longitude, altitude);
        return poi;
    }

    public Poi(String text, Texture texture) {
        modelInstance = new ModelInstance(model);
        Texture textTexture = TextureTextGenerator.generateTexture(text, 50);
        modelInstance.getMaterial("TextureHolder").set(TextureAttribute.createDiffuse(texture));
        modelInstance.getMaterial("TextHolder").set(TextureAttribute.createDiffuse(textTexture));
        modelInstance.getNode("poi").scale.set((float) texture.getWidth() / (float) texture.getHeight(), 1.0f, 1.0f);
        modelInstance.getNode("text").scale.set((float) textTexture.getWidth() / (float) textTexture.getHeight(), 1.0f, 1.0f);
        modelInstance.calculateTransforms();
    }

    public Poi(String text, Texture texture, double latitude, double longitude, double altitude) {
        this(text, texture);
        setPosition(latitude, longitude, altitude);
    }

    private static Model buildPoiModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Node node = modelBuilder.node();
        node.id = "poi";
        node.translation.add(0.0f, POI_HEIGHT_AT_1M / 2.0f, 0.0f);
        Material material = new Material("TextureHolder");
        material.set(new BlendingAttribute(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA));
        MeshPartBuilder meshBuilder = modelBuilder.part("poi", Gdx.gl.GL_TRIANGLES,
                VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal |
                        VertexAttributes.Usage.TextureCoordinates, material);
        meshBuilder.rect(
                -POI_HEIGHT_AT_1M / 2.0f, -POI_HEIGHT_AT_1M / 2.0f, 0,
                POI_HEIGHT_AT_1M / 2.0f, -POI_HEIGHT_AT_1M / 2.0f, 0,
                POI_HEIGHT_AT_1M / 2.0f, POI_HEIGHT_AT_1M / 2.0f, 0,
                -POI_HEIGHT_AT_1M / 2.0f, POI_HEIGHT_AT_1M / 2.0f, 0,
                0, 0, 1);

        node = modelBuilder.node();
        node.id = "text";
        node.translation.add(0.0f, POI_HEIGHT_AT_1M + FONT_HEIGHT_AT_1M * 0.8f, 0.0f);
        material = new Material("TextHolder");
        material.set(new BlendingAttribute(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA));
        meshBuilder = modelBuilder.part("text", Gdx.gl.GL_TRIANGLES,
                VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal |
                        VertexAttributes.Usage.TextureCoordinates, material);
        meshBuilder.rect(
                -FONT_HEIGHT_AT_1M / 2.0f, -FONT_HEIGHT_AT_1M / 2.0f, 0,
                FONT_HEIGHT_AT_1M / 2.0f, -FONT_HEIGHT_AT_1M / 2.0f, 0,
                FONT_HEIGHT_AT_1M / 2.0f, FONT_HEIGHT_AT_1M / 2.0f, 0,
                -FONT_HEIGHT_AT_1M / 2.0f, FONT_HEIGHT_AT_1M / 2.0f, 0,
                0, 0, 1);

        return modelBuilder.end();
    }

    @Override
    public RenderableProvider getRenderableProvider() {
        return modelInstance;
    }

    public void setPositionLookAtTarget(Vector3 target) {
        objToCam.set(target).sub(originRelativePosition);
        float size = objToCam.len();
        objToCam.y = 0.0f;
        objToCam.nor();
        billboardQuaternion.setFromCross(defaultLookAt, objToCam);
        modelInstance.transform.set(originRelativePosition, billboardQuaternion, scale.set(size, size, size));
    }

    @Override
    public void update() {
        setPositionLookAtTarget(WorldManager.getInstance().getWorldCamera().getCamera().position);
    }
}

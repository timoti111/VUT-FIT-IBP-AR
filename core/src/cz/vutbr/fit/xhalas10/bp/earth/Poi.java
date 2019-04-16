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
import cz.vutbr.fit.xhalas10.bp.osm.OSMData;
import cz.vutbr.fit.xhalas10.bp.scene.IWorldDrawableObject;
import cz.vutbr.fit.xhalas10.bp.scene.WorldManager;
import cz.vutbr.fit.xhalas10.bp.utils.TextureTextGenerator;

public class Poi extends EarthObject implements IWorldDrawableObject {
    private static final float POI_HEIGHT_AT_1M = 0.04f;
    private static final float FONT_HEIGHT_AT_1M = 0.02f;
    private static final Quaternion billboardQuaternion = new Quaternion();
    private static final Vector3 objToCam = new Vector3();
    private static final Vector3 defaultLookAt = new Vector3(Vector3.Z);
    private static final Vector3 scale = new Vector3();
    private static Model model = buildPoiModel();
    ModelInstance modelInstance;
    Texture textTexture;
    private OSMData osmData;
    private int priority = 0;

    public static Poi newBasicPoi(String text, double latitude, double longitude, double altitude) {
        Texture texture = MySkin.getInstance().get("basicPoi", Texture.class);
        return new Poi(text, texture, latitude, longitude, altitude);
    }


    public static Poi fromOSMNode(cz.vutbr.fit.xhalas10.bp.osm.model.Node node) {
        Poi poi;
        String name = node.getTags().get("name");
        Texture texture;

        if (node.getTags().containsValue("viewpoint") || node.getTags().containsValue("peak")) {
            texture = MySkin.getInstance().get(name == null ? "viewPoint" : "peak", Texture.class);
            poi = new Poi(name == null ? "View Point" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(100);
            return poi;
        }

        if (node.getTags().containsValue("volcano")) {
            texture = MySkin.getInstance().get("volcano", Texture.class);
            poi = new Poi(name == null ? "Volcano" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(100);
            return poi;
        }

        if (node.getTags().containsValue("cave_entrance")) {
            texture = MySkin.getInstance().get("cave", Texture.class);
            poi = new Poi(name == null ? "Cave" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(50);
            return poi;
        }

        if (node.getTags().containsValue("waterfalls")) {
            texture = MySkin.getInstance().get("waterfall", Texture.class);
            poi = new Poi(name == null ? "Waterfall" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(50);
            return poi;
        }

        if (node.getTags().containsValue("spring") || node.getTags().containsValue("hot_spring")) {
            texture = MySkin.getInstance().get("spring", Texture.class);
            poi = new Poi(name == null ? "Spring" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(40);
            return poi;
        }

        if (node.getTags().containsValue("rock") || node.getTags().containsValue("stone")) {
            texture = MySkin.getInstance().get("rock", Texture.class);
            poi = new Poi(name == null ? "Rock" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(40);
            return poi;
        }

        if (node.getTags().containsValue("wilderness_hut") || node.getTags().containsValue("alpine_hut")) {
            texture = MySkin.getInstance().get("cottage", Texture.class);
            poi = new Poi(name == null ? "Hut" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(30);
            return poi;
        }

        if (node.getTags().containsValue("map") || node.getTags().containsValue("board")) {
            texture = MySkin.getInstance().get("information", Texture.class);
            poi = new Poi(name == null ? "Information Board" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(10);
            return poi;
        }

        if (node.getTags().containsValue("guidepost")) {
            texture = MySkin.getInstance().get("signPost", Texture.class);
            poi = new Poi(name == null ? "Guide Post" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setPriority(10);
            return poi;
        }

        texture = MySkin.getInstance().get("basicPoi", Texture.class);
        poi = new Poi(name == null ? "Other" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
        return poi;
    }

    public Poi(String text, Texture texture) {
        modelInstance = new ModelInstance(model);
        textTexture = TextureTextGenerator.generateTexture(text, 50);
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
        node.translation.add(0.0f, POI_HEIGHT_AT_1M + POI_HEIGHT_AT_1M * 0.25f, 0.0f);
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

    private void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
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

    @Override
    public void dispose() {
        textTexture.dispose();
        model.dispose();
    }
}

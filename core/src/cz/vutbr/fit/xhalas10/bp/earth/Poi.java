package cz.vutbr.fit.xhalas10.bp.earth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import cz.vutbr.fit.xhalas10.bp.gui.MySkin;
import cz.vutbr.fit.xhalas10.bp.scene.interfaces.ISceneDrawableObject;
import cz.vutbr.fit.xhalas10.bp.scene.SceneManager;
import cz.vutbr.fit.xhalas10.bp.utils.TextureTextGenerator;

public class Poi extends EarthObject implements ISceneDrawableObject {
    private static final float POI_HEIGHT_AT_1M = 0.04f;
    private static final float FONT_HEIGHT_AT_1M = 0.025f;
    private static final Quaternion billboardQuaternion = new Quaternion();
    private static final Vector3 objToCam = new Vector3();
    private static final Vector3 defaultLookAt = new Vector3(Vector3.Z);
    private static final Vector3 scale = new Vector3();
    private static Model model = buildPoiModel();
    private static NumberFormat numberFormat;
    private String name;
    private ModelInstance modelInstance;
    private FrameBuffer nameTexture;
    private FrameBuffer distanceTexture;
    private float maximumDrawableDistance = 0.0f;
    private static Color fontColor = Color.GOLDENROD.cpy();
    private boolean alive = false;

    public static Poi fromOSMNode(cz.vutbr.fit.xhalas10.bp.osm.model.Node node) {
        Poi poi;
        String name = node.getTags().get("name");
        Texture texture;

        if (node.getTags().containsValue("viewpoint") || node.getTags().containsValue("peak")) {
            texture = MySkin.getInstance().get(name == null ? "viewPoint" : "peak", Texture.class);
            name = name == null ? "View Point" : name + " (" + (int)Math.round(node.getElevation()) + " m)";
            poi = new Poi(name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            if (name.equals("View Point"))
                poi.setMaximumDrawableDistance(5000);
            else
                poi.setMaximumDrawableDistance(Float.POSITIVE_INFINITY);
            return poi;
        }

        if (node.getTags().containsValue("volcano")) {
            texture = MySkin.getInstance().get("volcano", Texture.class);
            poi = new Poi(name == null ? "Volcano" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setMaximumDrawableDistance(Float.POSITIVE_INFINITY);
            return poi;
        }

        if (node.getTags().containsValue("cave_entrance")) {
            texture = MySkin.getInstance().get("cave", Texture.class);
            poi = new Poi(name == null ? "Cave" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setMaximumDrawableDistance(5000);
            return poi;
        }

        if (node.getTags().containsValue("waterfalls")) {
            texture = MySkin.getInstance().get("waterfall", Texture.class);
            poi = new Poi(name == null ? "Waterfall" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setMaximumDrawableDistance(5000);
            return poi;
        }

        if (node.getTags().containsValue("spring") || node.getTags().containsValue("hot_spring")) {
            texture = MySkin.getInstance().get("spring", Texture.class);
            poi = new Poi(name == null ? "Spring" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setMaximumDrawableDistance(2000.0f);
            return poi;
        }

        if (node.getTags().containsValue("rock") || node.getTags().containsValue("stone")) {
            texture = MySkin.getInstance().get("rock", Texture.class);
            poi = new Poi(name == null ? "Rock" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setMaximumDrawableDistance(2000.0f);
            return poi;
        }

        if (node.getTags().containsValue("wilderness_hut") || node.getTags().containsValue("alpine_hut")) {
            texture = MySkin.getInstance().get("cottage", Texture.class);
            poi = new Poi(name == null ? "Hut" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setMaximumDrawableDistance(2000.0f);
            return poi;
        }

        if (node.getTags().containsValue("map") || node.getTags().containsValue("board")) {
            texture = MySkin.getInstance().get("information", Texture.class);
            poi = new Poi(name == null ? "Information Board" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setMaximumDrawableDistance(1000.0f);
            return poi;
        }

        if (node.getTags().containsValue("guidepost")) {
            texture = MySkin.getInstance().get("signPost", Texture.class);
            poi = new Poi(name == null ? "Guide Post" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
            poi.setMaximumDrawableDistance(1000.0f);
            return poi;
        }

        texture = MySkin.getInstance().get("basicPoi", Texture.class);
        poi = new Poi(name == null ? "Other" : name, texture, node.getLocation().lat, node.getLocation().lng, node.getElevation());
        return poi;
    }

    private Poi(String text, Texture texture) {
        if (numberFormat == null) {
            numberFormat = new DecimalFormat();
            numberFormat.setMaximumFractionDigits(1);
            numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        }

        this.name = text;
        modelInstance = new ModelInstance(model);
        modelInstance.getMaterial("TextureHolder").set(TextureAttribute.createDiffuse(texture));
        modelInstance.getNode("poi").scale.set((float) texture.getWidth() / (float) texture.getHeight(), 1.0f, 1.0f);
    }

    @Override
    public void create() {
        nameTexture = TextureTextGenerator.generateTexture(name, 50, fontColor, true);
        modelInstance.getMaterial("NameHolder").set(TextureAttribute.createDiffuse(nameTexture.getColorBufferTexture()));
        modelInstance.getNode("name").scale.set((float) nameTexture.getWidth() / (float) nameTexture.getHeight(), 1.0f, 1.0f);
        alive = true;
    }

    private Poi(String text, Texture texture, double latitude, double longitude, double altitude) {
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
        node.id = "distance";
        node.translation.add(0.0f, POI_HEIGHT_AT_1M * 1.25f, 0.0f);
        material = new Material("DistanceHolder");
        material.set(new BlendingAttribute(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA));
        meshBuilder = modelBuilder.part("distance", Gdx.gl.GL_TRIANGLES,
                VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal |
                        VertexAttributes.Usage.TextureCoordinates, material);
        meshBuilder.rect(
                -FONT_HEIGHT_AT_1M / 2.0f, -FONT_HEIGHT_AT_1M / 2.0f, 0,
                FONT_HEIGHT_AT_1M / 2.0f, -FONT_HEIGHT_AT_1M / 2.0f, 0,
                FONT_HEIGHT_AT_1M / 2.0f, FONT_HEIGHT_AT_1M / 2.0f, 0,
                -FONT_HEIGHT_AT_1M / 2.0f, FONT_HEIGHT_AT_1M / 2.0f, 0,
                0, 0, 1);

        node = modelBuilder.node();
        node.id = "name";
        node.translation.add(0.0f, POI_HEIGHT_AT_1M * 1.25f + FONT_HEIGHT_AT_1M * 1.1f, 0.0f);
        material = new Material("NameHolder");
        material.set(new BlendingAttribute(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA));
        meshBuilder = modelBuilder.part("name", Gdx.gl.GL_TRIANGLES,
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

    private void setMaximumDrawableDistance(float maximumDrawableDistance) {
        this.maximumDrawableDistance = maximumDrawableDistance;
    }

    @Override
    public float getMaximumDrawableDistance() {
        return maximumDrawableDistance;
    }

    private void setPositionLookAtTarget(Vector3 target) {
        objToCam.set(target).sub(originRelativePosition);
        float size = objToCam.len();
        objToCam.y = 0.0f;
        objToCam.nor();
        billboardQuaternion.setFromCross(defaultLookAt, objToCam);
        modelInstance.transform.set(originRelativePosition, billboardQuaternion, scale.set(size, size, size));
    }

    @Override
    public void update() {
        float distance = originRelativePosition.dst(SceneManager.getInstance().getSceneCamera().getOriginRelativePosition()) / (float) SceneManager.getScale();
        if (distanceTexture != null)
            distanceTexture.dispose();
        distanceTexture = TextureTextGenerator.generateTexture(distance > 999.999f ? numberFormat.format(distance / 1000.0f) + " km" : numberFormat.format(distance) + " m", 50, fontColor, true);
        modelInstance.getMaterial("DistanceHolder").set(TextureAttribute.createDiffuse(distanceTexture.getColorBufferTexture()));
        modelInstance.getNode("distance").scale.set((float)distanceTexture.getWidth() / (float)distanceTexture.getHeight(), 1.0f, 1.0f);
        modelInstance.calculateTransforms();
        setPositionLookAtTarget(SceneManager.getInstance().getSceneCamera().getOriginRelativePosition());
    }

    @Override
    public void dispose() {
        if (nameTexture != null)
            nameTexture.dispose();
        if (distanceTexture != null)
            distanceTexture.dispose();
        alive = false;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public String getName() {
        return name;
    }

    public static void disposeModel() {
        model.dispose();
    }
}

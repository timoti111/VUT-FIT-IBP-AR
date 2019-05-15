/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import cz.vutbr.fit.xhalas10.bp.gui.MySkin;
import cz.vutbr.fit.xhalas10.bp.scene.SceneManager;
import cz.vutbr.fit.xhalas10.bp.scene.interfaces.ISceneDrawableObject;

public class Compass extends EarthObject implements ISceneDrawableObject {
    private static final float size = 0.6f;
    private static Model model = buildPoiModel();
    private ModelInstance modelInstance;

    public Compass() {
        modelInstance = new ModelInstance(model);
    }

    private static Model buildPoiModel() {
        Texture texture = MySkin.getInstance().get("compass", Texture.class);
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
        modelInstance.transform.setTranslation(SceneManager.getInstance().getSceneCamera().getCamera().position);
    }

    @Override
    public RenderableProvider getRenderableProvider() {
        return modelInstance;
    }

    @Override
    public float getMaximumDrawableDistance() {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public void create() {

    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public String getName() {
        return "compass";
    }

    @Override
    public void dispose() {
        model.dispose();
    }
}

/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;

import cz.vutbr.fit.xhalas10.bp.gui.MySkin;

public class TextureTextGenerator {
    private static GlyphLayout glyphLayout = new GlyphLayout();
    private static SpriteBatch batch = new SpriteBatch();
    private static Matrix4 matrix4 = new Matrix4();

    public static FrameBuffer generateTexture(String text, int size, Color color, boolean bold) {
        BitmapFont font = MySkin.getInstance().getFont(bold ? "default-bold" : "default");
        font.setColor(color);

        glyphLayout.setText(font, text);
        int width = (int) Math.ceil(glyphLayout.width);
        int height = MySkin.getInstance().get("fontSize", Integer.class) + 10;
        FrameBuffer frameBuffer = new FrameBuffer(Pixmap.Format.RGBA4444, width, height, false);
        frameBuffer.begin();

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
        matrix4.setToOrtho2D(0, height, width, -height);
        batch.setProjectionMatrix(matrix4);
        batch.begin();
        font.draw(batch, text, 0, height - (height - MySkin.getInstance().get("fontSize", Integer.class)) / 2.0f);
        batch.end();
        frameBuffer.end();
        return frameBuffer;
    }

    public static void dispose() {
        batch.dispose();
    }
}

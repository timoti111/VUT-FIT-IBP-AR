package cz.vutbr.fit.xhalas10.bp.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import cz.vutbr.fit.xhalas10.bp.MySkin;

public class TextureTextGenerator {
    private static GlyphLayout glyphLayout = new GlyphLayout();
    private static SpriteBatch batch = new SpriteBatch();
    private static Matrix4 lm = new Matrix4();
    public static Texture generateTexture(String text, int size, Color color) {
        BitmapFont font = MySkin.getInstance().getFont("default");
        font.setColor(color);

        glyphLayout.setText(font, text);
        int width = (int)Math.ceil(glyphLayout.width);
        int height = MySkin.getInstance().get("fontSize", Integer.class);

        FrameBuffer lFb = new FrameBuffer(Pixmap.Format.RGBA4444, width, height, false);
        lFb.begin();
        //Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        lm.setToOrtho2D(0, height, width, -height);
        batch.setProjectionMatrix(lm);
        batch.begin();
        font.draw(batch, text, 0, height);
        batch.end();
        lFb.end();
        return lFb.getColorBufferTexture();
    }
}

package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.utils.FontGenerator;
import com.badlogic.gdx.utils.Align;

public class Poi {
    private static final float POI_SCALE = 0.05f;
    private static final float FONT_SCALE = 0.0005f;

    private static final int FONT_SIZE = 50;
    private Decal poi;
    private BitmapFontCache bitmapFontCache;
    private Vector3 tmpVec3 = new Vector3();
    private Matrix4 tmpMat4 = new Matrix4();
    private Vector3 tmp = new Vector3();
    private Vector3 tmp2 = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 textPosition = new Vector3();
    private Matrix4 textTransform = new Matrix4();
    private Camera camera;
    private Vector3 up = new Vector3(0.0f, 1.0f, 0.0f);
    private BitmapFont font = FontGenerator.generateFont("fonts/OpenSans-Regular.ttf", FONT_SIZE);

    public Poi(String text, Camera camera) {
        super();
        this.camera = camera;
        Texture texture = new Texture(Gdx.files.internal("poi.png"));
        TextureRegion textureRegion = new TextureRegion(texture);
        poi = Decal.newDecal(textureRegion);
        poi.setBlending(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        poi.setColor(1, 1, 1, 1);
        bitmapFontCache = new BitmapFontCache(font);
        bitmapFontCache.setText(text, -200.0f, FONT_SIZE / 2.0f, 400, Align.center, false);
    }

    public void testUpdate() {
        float size = poi.getPosition().len();
        float poiScale = size * POI_SCALE;
        float fontScale = size * FONT_SCALE;
        poi.setWidth(poiScale);
        poi.setHeight(poiScale);
        poi.translate(0, poiScale / 2.0f, 0);
        poi.lookAt(camera.position, up);

        textPosition.add(0, poiScale + FONT_SIZE * fontScale / 2.0f, 0);
        tmpVec3.set(camera.position).sub(textPosition).nor();
        tmp.set(up).crs(tmpVec3).nor();
        tmp2.set(tmpVec3).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, tmpVec3.x, tmp.y, tmp2.y, tmpVec3.y, tmp.z, tmp2.z, tmpVec3.z);
        textTransform.setToTranslation(textPosition).scale(fontScale, fontScale, fontScale).rotate(rotation);
    }

    public void renderDecal(DecalBatch decalBatch) {
        decalBatch.add(poi);
    }

    public void renderText(Batch spriteBatch) {
        spriteBatch.setProjectionMatrix(tmpMat4.set(camera.combined).mul(textTransform));
        bitmapFontCache.draw(spriteBatch);
    }

    public void setPosition(Vector3 position) {
        poi.setPosition(position);
        textPosition.set(position);
    }
}

package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectSet;

import cz.vutbr.fit.xhalas10.bp.earth.Poi;
import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

public class WorldManager {
    private IWorldCamera worldCamera;
    private ModelCache modelCache;
    private ModelBatch modelBatch;
    private Vector3d origin;
    private Quaternion correctionQuaternion;
    public static Matrix4 correctionMatrix;
    private ObjectSet<IWorldDrawableObject> worldObjects;
    private static final double MAXIMUM_CAMERA_DISTANCE = 1000.0f;
    private static final Vector3 UP = new Vector3(0.0f, 1.0f, 0.0f);
    private static final Vector3 NORTH = new Vector3(0.0f, 0.0f, 1.0f);

    private static WorldManager instance = new WorldManager();

    public static WorldManager getInstance() {
        return instance;
    }

    private WorldManager() {
        modelCache = new ModelCache();
        modelBatch = new ModelBatch();
        origin = new Vector3d();
        correctionQuaternion = new Quaternion();
        correctionMatrix = new Matrix4();
        worldObjects = new ObjectSet<IWorldDrawableObject>();
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public Quaternion getCorrectionQuaternion() {
        return correctionQuaternion;
    }

    public void setWorldCamera(IWorldCamera worldCamera) {
        this.worldCamera = worldCamera;
    }

    public void addWorldObject(IWorldDrawableObject worldObject) {
        worldObjects.add(worldObject);
    }

    public void addWorldObjects(Iterable<IWorldDrawableObject> worldObjects) {
        for (IWorldDrawableObject worldObject : worldObjects) {
            addWorldObject(worldObject);
        }
    }

    public void renderWorld() {
        update();
        modelBatch.begin(worldCamera.getCamera());
        modelBatch.render(modelCache);
        modelBatch.end();
    }

    public static final Quaternion upCorrection = new Quaternion();
    public static final Quaternion northCorrection = new Quaternion();

    private void update() {
        if (origin.dst(worldCamera.getWorldPosition()) > MAXIMUM_CAMERA_DISTANCE) {
            origin.set(worldCamera.getWorldPosition());
            correctionMatrix.set(worldCamera.getEastVector(), worldCamera.getUpVector(), worldCamera.getNorthVector().scl(-1.0f), Vector3.Zero);
            setWorldOrigin();
        }
    }

    public void updateObjectsAndCache() {
        modelCache.begin();
        for (IWorldDrawableObject worldObject : worldObjects) {
            worldObject.update();
            modelCache.add(worldObject.getRenderableProvider());
        }
        modelCache.end();
    }

    private void setWorldOrigin() {
        modelCache.begin();
        worldCamera.calculateOriginRelativePosition(origin, correctionQuaternion);
        worldCamera.update();
        for (IWorldDrawableObject worldObject : worldObjects) {
            if (worldObject.getWorldPosition().dst(origin) < worldCamera.getCamera().far) {
                worldObject.calculateOriginRelativePosition(origin, correctionQuaternion);
                worldObject.update();
                modelCache.add(worldObject.getRenderableProvider());
            }
        }
        modelCache.end();
    }

    public IWorldCamera getWorldCamera() {
        return worldCamera;
    }
}


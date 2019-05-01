package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectSet;

import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

public class WorldManager implements Disposable {
    private IWorldCamera worldCamera;
    private static ModelCache modelCache;
    private ModelBatch modelBatch;
    private Vector3d origin;
    private Matrix4 correctionMatrix;
    private Array<IWorldDrawableObject> unmanagedObjects;
    private ObjectSet<IWorldDrawableObject> allObjects;
    private Iterable<IWorldDrawableObject> objectsToDraw;
    private WorldObjectFilter worldObjectFilter;

    private static final double MAXIMUM_CAMERA_DISTANCE = 100.0f;

    private static WorldManager instance = new WorldManager();

    public static WorldManager getInstance() {
        return instance;
    }

    private WorldManager() {
        modelCache = new ModelCache();
        modelBatch = new ModelBatch();
        origin = new Vector3d();
        correctionMatrix = new Matrix4();
        unmanagedObjects = new Array<>();
        allObjects = new ObjectSet<>();
        worldObjectFilter = new WorldObjectFilter();
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public Matrix4 getCorrectionMatrix() {
        return correctionMatrix;
    }

    public void setWorldCamera(IWorldCamera worldCamera) {
        this.worldCamera = worldCamera;
    }

    public void addWorldObject(IWorldDrawableObject worldObject, boolean managed) {
        if(managed)
            allObjects.add(worldObject);
        else
            unmanagedObjects.add(worldObject);
    }

    public void addWorldObjects(Iterable<IWorldDrawableObject> worldObjects, boolean managed) {
        worldObjects.forEach(worldObject -> addWorldObject(worldObject, managed));
    }

    public void clearManagedObjects() {
        allObjects.forEach(Disposable::dispose);
        allObjects.clear();
    }

    public void clearUnmanagedObjects() {
        unmanagedObjects.forEach(Disposable::dispose);
        unmanagedObjects.clear();
    }

    public void renderWorld() {
        update();
        modelBatch.begin(worldCamera.getCamera());

        modelBatch.render(modelCache);
        for (IWorldDrawableObject drawableObject : unmanagedObjects) {
            drawableObject.update();
            modelBatch.render(drawableObject.getRenderableProvider());
        }

        modelBatch.end();
    }

    private void update() {
        if (origin.dst(worldCamera.getWorldPosition()) > MAXIMUM_CAMERA_DISTANCE) {
            origin.set(worldCamera.getWorldPosition());
            correctionMatrix.set(worldCamera.getEastVector(), worldCamera.getUpVector(), worldCamera.getNorthVector().scl(-1.0f), Vector3.Zero);
            setWorldOrigin();
        }
        worldCamera.update();
    }

    public void updateObjectsAndCache() {
        if (objectsToDraw != null) {
            modelCache.begin();
            for (IWorldDrawableObject worldObject : objectsToDraw) {
                worldObject.update();
                modelCache.add(worldObject.getRenderableProvider());
            }
            modelCache.end();
        }
    }

    private void setWorldOrigin() {
        worldCamera.calculateOriginRelativePosition();
        updateAll();
    }

    public void updateAll() {
        worldObjectFilter.update(allObjects);
        objectsToDraw = worldObjectFilter.getFilteredObjects();
        updateObjectsAndCache();
    }


    public IWorldCamera getWorldCamera() {
        return worldCamera;
    }

    @Override
    public void dispose() {
        allObjects.forEach(Disposable::dispose);
        unmanagedObjects.forEach(Disposable::dispose);
        modelCache.dispose();
        modelBatch.dispose();
    }
}


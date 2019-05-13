package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectSet;

import cz.vutbr.fit.xhalas10.bp.scene.interfaces.ISceneCamera;
import cz.vutbr.fit.xhalas10.bp.scene.interfaces.ISceneDrawableObject;
import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

public class SceneManager implements Disposable {
    private ISceneCamera sceneCamera;
    private static ModelCache modelCache;
    private ModelBatch modelBatch;
    private Vector3d origin;
    private Matrix4 correctionMatrix;
    private Array<ISceneDrawableObject> unmanagedObjects;
    private ObjectSet<ISceneDrawableObject> allObjects;
    private Iterable<ISceneDrawableObject> objectsToDraw;
    private SceneObjectFilter sceneObjectFilter;

    private static final float SCALE = 0.1f;
    private static final double MAXIMUM_CAMERA_DISTANCE = 20.0f;

    private static SceneManager instance = new SceneManager();

    public static SceneManager getInstance() {
        return instance;
    }

    private SceneManager() {
        modelCache = new ModelCache();
        modelBatch = new ModelBatch();
        origin = new Vector3d();
        correctionMatrix = new Matrix4();
        unmanagedObjects = new Array<>();
        allObjects = new ObjectSet<>();
        sceneObjectFilter = new SceneObjectFilter();
    }

    public static double getMaximumCameraDistance() {
        return MAXIMUM_CAMERA_DISTANCE;
    }
    public static double getScale() {
        return SCALE;
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public Matrix4 getCorrectionMatrix() {
        return correctionMatrix;
    }

    public void setSceneCamera(ISceneCamera sceneCamera) {
        this.sceneCamera = sceneCamera;
    }

    public void addSceneObject(ISceneDrawableObject sceneObject, boolean managed) {
        if(managed)
            allObjects.add(sceneObject);
        else
            unmanagedObjects.add(sceneObject);
    }

    public void addSceneObjects(Iterable<ISceneDrawableObject> sceneObjects, boolean managed) {
        sceneObjects.forEach(sceneObject -> addSceneObject(sceneObject, managed));
    }

    public void clearManagedObjects() {
        allObjects.forEach(Disposable::dispose);
        allObjects.clear();
    }

    public void clearUnmanagedObjects() {
        unmanagedObjects.forEach(Disposable::dispose);
        unmanagedObjects.clear();
    }

    public void renderScene() {
        update();
        modelBatch.begin(sceneCamera.getCamera());

        modelBatch.render(modelCache);
        for (ISceneDrawableObject drawableObject : unmanagedObjects) {
            drawableObject.update();
            modelBatch.render(drawableObject.getRenderableProvider());
        }

        modelBatch.end();
    }

    private void update() {
        if (origin.dst(sceneCamera.getScenePosition()) > MAXIMUM_CAMERA_DISTANCE) {
            updateSceneOriginAndObjects();
        }
        sceneCamera.update();
    }

    public void updateObjectsAndCache() {
        if (objectsToDraw != null) {
            modelCache.begin();
            for (ISceneDrawableObject sceneObject : objectsToDraw) {
                sceneObject.update();
                modelCache.add(sceneObject.getRenderableProvider());
            }
            modelCache.end();
        }
    }

    public void updateSceneOriginAndObjects() {
        origin.set(sceneCamera.getScenePosition());
        correctionMatrix.set(sceneCamera.getEastVector(), sceneCamera.getUpVector(), sceneCamera.getSouthVector(), Vector3.Zero);
        sceneCamera.calculateOriginRelativePosition();
        sceneObjectFilter.update(allObjects);
        objectsToDraw = sceneObjectFilter.getFilteredObjects();
        updateObjectsAndCache();
    }


    public ISceneCamera getSceneCamera() {
        return sceneCamera;
    }

    @Override
    public void dispose() {
        allObjects.forEach(Disposable::dispose);
        unmanagedObjects.forEach(Disposable::dispose);
        modelCache.dispose();
        modelBatch.dispose();
    }
}


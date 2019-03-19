package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class Globe {
    private double scale = 1;
    private double radius;
    private DecalBatch decalBatch;
    private ModelBatch modelBatch;
    private PerspectiveCamera cam;
    private Environment environment;
    protected ArrayList<Decal> decalArray = new ArrayList<Decal>();
    protected ArrayList<ModelInstance> modelArray = new ArrayList<ModelInstance>();
    protected ArrayList<GlobeObject> objectArray = new ArrayList<GlobeObject>();
    private Matrix4 toLocalMatrix;


    public Globe(PerspectiveCamera cam, Environment environment) {
        radius = 0;
        decalBatch = new DecalBatch(new CameraGroupStrategy(cam));
        modelBatch = new ModelBatch();
        this.cam = cam;
        this.environment = environment;
        toLocalMatrix = new Matrix4();
    }

    public void add(Decal decal, double latitude, double longitude, double altitude) {
        GlobeObject object = new GlobeObject(decal, latitude, longitude, altitude);
        object.updateObject(toLocalMatrix);
        objectArray.add(object);
        decalArray.add(decal);
    }

    public void add(ModelInstance model, double latitude, double longitude, double altitude) {
        GlobeObject object = new GlobeObject(model, latitude, longitude, altitude);
        object.updateObject(toLocalMatrix);
        objectArray.add(object);
        modelArray.add(model);
    }

    public void setRadius(double radius)
    {
        this.radius = radius;
    }

    public double getRadius()
    {
        return radius;
    }

    public void render() {
        for (Decal decal : decalArray) {
            decalBatch.add(decal);
        }
        decalBatch.flush();

        modelBatch.begin(cam);
        for (ModelInstance instance : modelArray) {
            modelBatch.render(instance, environment);
        }
		modelBatch.end();
    }

    public void setPersonPosition(double latitude, double longitude, double altitude)
    {
        double perAlt = radius + altitude / scale;

        Matrix4 rotation = new Matrix4();
        rotation.rotate(0.0f, 0.0f, 1.0f, (float)-longitude).rotate(1.0f, 0.0f, 0.0f, (float)-latitude);

        Matrix4 translation = new Matrix4();
        translation.trn(0.0f, (float) perAlt, 0.0f);

        toLocalMatrix.set(rotation).mul(translation).inv();
        for (GlobeObject object : objectArray) {
            object.updateObject(toLocalMatrix);
        }
    }

    private class GlobeObject {
        Object object;
        Vector3 worldPosition;
        Vector3 localPosition;

        public GlobeObject(Decal decal, double latitude, double longitude, double altitude) {
            object = decal;
            decal.setWidth(3f);
            decal.setHeight(3f);
            calculatePosition(latitude, longitude, altitude);
        }

        public GlobeObject(ModelInstance modelInstance, double latitude, double longitude, double altitude) {
            object = modelInstance;
            calculatePosition(latitude, longitude, altitude);
        }

        private void calculatePosition(double latitude, double longitude, double altitude) {
            worldPosition = new Vector3(0.0f, (float)radius + (float)altitude, 0.0f);
            worldPosition.rotate((float)-latitude, 1.0f, 0.0f, 0.0f);
            worldPosition.rotate((float)-longitude, 0.0f, 0.0f, 1.0f);
        }

        public void updateObject(Matrix4 transform) {
            localPosition = worldPosition.cpy();
            localPosition.mul(transform);
            if (object instanceof Decal) {
                Decal decal = (Decal)object;
                decal.getPosition().set(localPosition);
                decal.lookAt(cam.position, cam.up);
            }
            else {
                ModelInstance modelInstance = (ModelInstance)object;
                modelInstance.transform.setTranslation(localPosition);
            }
        }
    }
}

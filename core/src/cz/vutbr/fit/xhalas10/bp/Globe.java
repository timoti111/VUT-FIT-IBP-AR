package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Collection;

public class Globe {
    private double scale = 1;
    private double radius;
    private DecalBatch decalBatch;
    private ModelBatch modelBatch;
    private SpriteBatch spriteBatch;
    private PerspectiveCamera cam;
    private Environment environment;
    private ArrayList<Poi> poiArray = new ArrayList<Poi>();
    private ArrayList<ModelInstance> modelArray = new ArrayList<ModelInstance>();
    private ArrayList<GlobeObject> objectArray = new ArrayList<GlobeObject>();
    private Matrix4 toLocalMatrix;

    private double personLatitude;
    private double personLongitude;
    private double personAltitude;


    Globe(PerspectiveCamera cam, Environment environment) {
        radius = 0;
        decalBatch = new DecalBatch(new CameraGroupStrategy(cam));
        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();
        this.cam = cam;
        this.environment = environment;
        toLocalMatrix = new Matrix4();
    }

    public void add(Poi poi, double latitude, double longitude, double altitude) {
        GlobeObject object = new GlobeObject(poi, latitude, longitude, altitude);
        object.updateObject(toLocalMatrix);
        objectArray.add(object);
        poiArray.add(poi);
    }

    public void add(Collection<OSMNode> nodes) {
        for (OSMNode node : nodes) {
            if (node.hasElevation())
                this.add(new Poi(node, cam), node.getLocation().lat, node.getLocation().lng, node.getElevation());
        }
    }

    public void set(Collection<OSMNode> nodes) {
        poiArray.clear();
        objectArray.clear();
        add(nodes);
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
        for (Poi poi : poiArray) {
            poi.renderDecal(decalBatch);
        }
        decalBatch.flush();

        spriteBatch.begin();
        for (Poi poi : poiArray) {
            poi.renderText(spriteBatch);
        }
        spriteBatch.end();

        modelBatch.begin(cam);
        for (ModelInstance instance : modelArray) {
            modelBatch.render(instance, environment);
        }
		modelBatch.end();
    }

    public void setPersonPosition(double latitude, double longitude, double altitude)
    {
        if (latitude == personLatitude && longitude == personLongitude && altitude == personAltitude) {
            return;
        }

        personLatitude = latitude;
        personLongitude = longitude;
        personAltitude = altitude;

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

        GlobeObject(Poi poi, double latitude, double longitude, double altitude) {
            object = poi;
            calculatePosition(latitude, longitude, altitude);
        }

        GlobeObject(ModelInstance modelInstance, double latitude, double longitude, double altitude) {
            object = modelInstance;
            calculatePosition(latitude, longitude, altitude);
        }

        private void calculatePosition(double latitude, double longitude, double altitude) {
            worldPosition = new Vector3(0.0f, (float)radius + (float)altitude, 0.0f);
            worldPosition.rotate((float)-latitude, 1.0f, 0.0f, 0.0f);
            worldPosition.rotate((float)-longitude, 0.0f, 0.0f, 1.0f);
        }

        void updateObject(Matrix4 transform) {
            localPosition = worldPosition.cpy();
            localPosition.mul(transform);
            if (object instanceof Poi) {
                Poi poi = (Poi)object;
                poi.setPosition(localPosition);
                poi.testUpdate();
            }
            else {
                ModelInstance modelInstance = (ModelInstance)object;
                modelInstance.transform.setTranslation(localPosition);
            }
        }
    }
}

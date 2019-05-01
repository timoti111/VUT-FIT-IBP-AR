package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class WorldObjectFilter {
    private static final int quadrantHorizontalParts = 8;
    private Array<Sector> sectors;
    private Array<Float> sectorConstants;
    private Array<IWorldDrawableObject> filteredObjects;

    public WorldObjectFilter() {
        sectors = new Array<>(quadrantHorizontalParts);
        filteredObjects = new Array<>();

        for (int i = 0; i < quadrantHorizontalParts * 4; i++) {
            sectors.add(new Sector());
        }

        float angle = 90.0f / quadrantHorizontalParts;
        sectorConstants = new Array<>();

        for (int i = 0; i < quadrantHorizontalParts - 1; i++) {
            sectorConstants.add(MathUtils.cos(MathUtils.degreesToRadians * angle * i));
        }

        sectorConstants.add(0.0f);

        for (int i = sectorConstants.size - 1; i > 0; i--) {
            sectorConstants.add(-sectorConstants.get(i - 1));
        }

        sectorConstants.add(Float.NEGATIVE_INFINITY);
    }

    public void update(Iterable<IWorldDrawableObject> worldDrawableObjects) {
        reset();
        for (IWorldDrawableObject worldDrawableObject : worldDrawableObjects) {
            if (worldDrawableObject.isAlive())
                worldDrawableObject.dispose();
            worldDrawableObject.calculateOriginRelativePosition();
            int index = determineSectorIndex(worldDrawableObject.getOriginRelativePosition());
            sectors.get(index).add(worldDrawableObject);
        }
    }

    public Iterable<IWorldDrawableObject> getFilteredObjects() {
        filteredObjects.clear();
        for (Sector sector : sectors) {
            for (IWorldDrawableObject visibleObject : sector.getVisibleObjects()) {
                if (visibleObject != null) {
                    if (!visibleObject.isAlive())
                        visibleObject.create();
                    filteredObjects.add(visibleObject);
                }
            }
        }
        return filteredObjects;
    }

    private int determineSectorIndex(Vector3 objectPosition) {
        Vector2 vector2 = new Vector2(objectPosition.x, objectPosition.z).nor();
        int sector = 0;
        for (int i = 0; i < sectorConstants.size; i++) {
            if (vector2.x > sectorConstants.get(i)) {
                sector = i;
                break;
            }
        }
        return vector2.y > 0.0f ? sectors.size - 1 - sector : sector;
    }

    private void reset() {
        for (Sector sector : sectors) {
            sector.reset();
        }
    }
}

class Sector {
    private static final int quadrantVerticalParts = 5;
    private Array<IWorldDrawableObject> visibleObjects;
    private Array<Float> heightConstants;


    Sector() {
        visibleObjects = new Array<>(quadrantVerticalParts * 2);
        visibleObjects.size = quadrantVerticalParts * 2;

        float angle = 90.0f / quadrantVerticalParts;
        heightConstants = new Array<>();

        for (int i = 0; i < quadrantVerticalParts - 1; i++) {
            heightConstants.add(MathUtils.cos(MathUtils.degreesToRadians * angle * i));
        }

        heightConstants.add(0.0f);

        for (int i = heightConstants.size - 1; i > 0; i--) {
            heightConstants.add(-heightConstants.get(i - 1));
        }

        heightConstants.add(Float.NEGATIVE_INFINITY);
    }

    void add(IWorldDrawableObject newWorldDrawableObject) {
        int index = determineHeightIndex(newWorldDrawableObject.getOriginRelativePosition());
        IWorldDrawableObject actualWorldDrawableObject = visibleObjects.get(index);
        if (actualWorldDrawableObject == null)
            visibleObjects.set(index, newWorldDrawableObject);
        else {
            Vector3 position = actualWorldDrawableObject.getOriginRelativePosition();
            float newWorldDrawableObjectDistance = newWorldDrawableObject.getOriginRelativePosition().len();
            float newWorldDrawableObjectDistanceFromCamera = newWorldDrawableObject.getOriginRelativePosition().dst(WorldManager.getInstance().getWorldCamera().getOriginRelativePosition());
            if (newWorldDrawableObjectDistanceFromCamera < WorldManager.getInstance().getWorldCamera().getCamera().far) {
                if (newWorldDrawableObjectDistance < position.len()) {
                    visibleObjects.set(index, newWorldDrawableObject);
                }
//            else if (newWorldDrawableObject.getPriority() > actualWorldDrawableObject.getPriority()) {
//                visibleObjects.set(index, newWorldDrawableObject);
//            }
            }
        }
    }

    private int determineHeightIndex(Vector3 objectPosition) {
        float y = objectPosition.cpy().nor().y;
        int index = 0;
        for (int i = 0; i < heightConstants.size; i++) {
            if (y > heightConstants.get(i)) {
                index = i;
                break;
            }
        }
        return index;
    }

    void reset() {
        int lastCapacity = visibleObjects.size;
        visibleObjects.clear();
        visibleObjects.size = lastCapacity;
    }

    Array<IWorldDrawableObject> getVisibleObjects() {
        return visibleObjects;
    }
}

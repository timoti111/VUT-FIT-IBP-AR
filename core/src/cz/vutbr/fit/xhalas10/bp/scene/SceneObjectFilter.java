/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.scene;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import cz.vutbr.fit.xhalas10.bp.scene.interfaces.ISceneDrawableObject;

/**
 * This class filters objects. First it chooses horizontal sector where object is positioned from
 * camera view and then Sector class chooses vertical part where object is positioned. So view is
 * now split to parts and in each part only one object can be set from all possible objects for
 * this part. This selection is done by class Sector.
 */
class SceneObjectFilter {
    private static final int quadrantHorizontalParts = 5;
    private Array<Sector> sectors;
    private Array<Float> sectorConstants;
    private Array<ISceneDrawableObject> filteredObjects;

    SceneObjectFilter() {
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

    void update(Iterable<ISceneDrawableObject> sceneDrawableObjects) {
        reset();
        for (ISceneDrawableObject sceneDrawableObject : sceneDrawableObjects) {
            if (sceneDrawableObject.isAlive())
                sceneDrawableObject.dispose();
            sceneDrawableObject.calculateOriginRelativePosition();
            int index = determineSectorIndex(sceneDrawableObject.getOriginRelativePosition());
            sectors.get(index).add(sceneDrawableObject);
        }
    }

    Iterable<ISceneDrawableObject> getFilteredObjects() {
        filteredObjects.clear();
        for (Sector sector : sectors) {
            for (ISceneDrawableObject visibleObject : sector.getVisibleObjects()) {
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

/**
 * This class firstly selects vertical part where object can be placed and then looks if object
 * can be placed there. This is accomplished by comparing priority, distance from camera and angle
 * of view to object between object which is actually in this part and object which is to be
 * or not to be set here.
 */
class Sector {
    private static final int quadrantVerticalParts = 5;
    private Array<ISceneDrawableObject> visibleObjects;
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

    void add(ISceneDrawableObject newSceneDrawableObject) {
        float newSceneDrawableObjectDistance = newSceneDrawableObject.getOriginRelativePosition().len() / (float) SceneManager.getScale();
        int index = determineHeightIndex(newSceneDrawableObject.getOriginRelativePosition());
        ISceneDrawableObject actualSceneDrawableObject = visibleObjects.get(index);

        if (actualSceneDrawableObject == null) {
            if (newSceneDrawableObjectDistance < newSceneDrawableObject.getMaximumDrawableDistance())
                if (newSceneDrawableObjectDistance < SceneManager.getInstance().getSceneCamera().getCamera().far)
                    visibleObjects.set(index, newSceneDrawableObject);
        } else {
            float actualSceneDrawableObjectDistance = actualSceneDrawableObject.getOriginRelativePosition().len() / (float) SceneManager.getScale();
            float actualSceneDrawableObjectAngle = (float) Math.asin((actualSceneDrawableObject.getOriginRelativePosition().y / SceneManager.getScale()) / actualSceneDrawableObjectDistance);
            float newSceneDrawableObjectAngle = (float) Math.asin((newSceneDrawableObject.getOriginRelativePosition().y / SceneManager.getScale()) / newSceneDrawableObjectDistance);
            if (newSceneDrawableObjectDistance < SceneManager.getInstance().getSceneCamera().getCamera().far) {
                if (newSceneDrawableObjectDistance > 10.0f) {
                    if (newSceneDrawableObject.getMaximumDrawableDistance() > actualSceneDrawableObject.getMaximumDrawableDistance()) {
                        if (newSceneDrawableObjectDistance > actualSceneDrawableObjectDistance)
                            if (newSceneDrawableObjectAngle < actualSceneDrawableObjectAngle)
                                return;
                        visibleObjects.set(index, newSceneDrawableObject);
                    } else if (Float.compare(newSceneDrawableObject.getMaximumDrawableDistance(), actualSceneDrawableObject.getMaximumDrawableDistance()) == 0) {
                        if (newSceneDrawableObjectAngle > actualSceneDrawableObjectAngle)
                            visibleObjects.set(index, newSceneDrawableObject);
                    } else {
                        if (newSceneDrawableObjectDistance < newSceneDrawableObject.getMaximumDrawableDistance())
                            if (newSceneDrawableObjectDistance < actualSceneDrawableObjectDistance)
                                visibleObjects.set(index, newSceneDrawableObject);
                    }
                }
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

    Array<ISceneDrawableObject> getVisibleObjects() {
        return visibleObjects;
    }
}

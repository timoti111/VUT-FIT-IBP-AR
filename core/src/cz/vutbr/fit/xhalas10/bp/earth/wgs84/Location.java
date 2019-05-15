/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.earth.wgs84;

import com.badlogic.gdx.math.Vector3;

import cz.vutbr.fit.xhalas10.bp.utils.Vector3d;

/**
 * This class serves for storing geographic. It also computes geodetic altitude from WGS84 system.
 * It calculates direction vectors based on location.
 */
public class Location {
    private static final double A = 6378137.0;
    private static final double B_SQ_DIV_A_SQ = 0.99330562;
    private static final double E_SQ = 0.00669437999014;
    private double latitude, longitude, altitude, WGS84altitude;
    private Vector3d cartesian;
    private Vector3 normalVector, southVector, eastVector;
    private boolean updateCartesian, updateLocalSpaceVectors, updateCosSin;
    private double cosLat, cosLon, sinLat, sinLon;

    public Location(double latitude, double longitude, double altitude) {
        this.cartesian = new Vector3d();
        this.normalVector = new Vector3();
        this.southVector = new Vector3();
        this.eastVector = new Vector3();
        set(latitude, longitude, altitude);
    }

    public Location(Location location) {
        set(location);
    }

    public Location() {
    }

    public void set(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        updateWGS84altitude();
        setUpdate();
    }

    public void set(Location location) {
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.altitude = location.altitude;
        this.WGS84altitude = location.WGS84altitude;
        this.cartesian = location.cartesian.cpy();
        this.normalVector = location.normalVector.cpy();
        this.southVector = location.southVector.cpy();
        this.eastVector = location.eastVector.cpy();
        this.updateCartesian = location.updateCartesian;
        this.updateLocalSpaceVectors = location.updateLocalSpaceVectors;
        this.updateCosSin = location.updateCosSin;
        this.cosLat = location.cosLat;
        this.cosLon = location.cosLon;
        this.sinLat = location.sinLat;
        this.sinLon = location.sinLon;
    }

    private void updateWGS84altitude() {
        WGS84altitude = altitude + GeoidUndulation.getInstance().getUndulation(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private double getAltitude() {
        return altitude;
    }

    private void setUpdate() {
        updateLocalSpaceVectors = true;
        updateCartesian = true;
        updateCosSin = true;
    }

    private void updateCosSin() {
        if (updateCosSin) {
            cosLat = Math.cos(Math.toRadians(latitude));
            cosLon = Math.cos(Math.toRadians(longitude));
            sinLat = Math.sin(Math.toRadians(latitude));
            sinLon = Math.sin(Math.toRadians(longitude));
            updateCosSin = false;
        }
    }

    private void updateLocalSpaceVectors() {
        updateCosSin();
        if (updateLocalSpaceVectors) {
            normalVector.set((float) (cosLat * sinLon), (float) (cosLat * cosLon), (float) -sinLat).nor();
            southVector.set((float) (sinLat * sinLon), (float) (sinLat * cosLon), (float) cosLat).nor();
            eastVector.set((float) cosLon, (float) -sinLon, 0.0f).nor();
            updateLocalSpaceVectors = false;
        }
    }

    public Vector3d toCartesian() {
        updateCosSin();
        if (updateCartesian) {
            double N = A / Math.sqrt(1.0 - E_SQ * sinLat * sinLat);
            double x = (N + WGS84altitude) * cosLat * sinLon;
            double y = (N + WGS84altitude) * cosLat * cosLon;
            double z = -(B_SQ_DIV_A_SQ * N + WGS84altitude) * sinLat;
            cartesian.set(x, y, z);
            updateCartesian = false;
        }
        return cartesian;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Location) {
            Location location = (Location) o;
            return Double.compare(location.getLatitude(), latitude) == 0 &&
                    Double.compare(location.getLongitude(), longitude) == 0 &&
                    Double.compare(location.getAltitude(), altitude) == 0;
        }
        return false;
    }

    public Vector3 getSouthPointingVector() {
        updateLocalSpaceVectors();
        return southVector;
    }

    public Vector3 getEastPointingVector() {
        updateLocalSpaceVectors();
        return eastVector;
    }

    public Vector3 getUpPointingVector() {
        updateLocalSpaceVectors();
        return normalVector;
    }
}

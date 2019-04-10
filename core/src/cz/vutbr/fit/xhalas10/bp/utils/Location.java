package cz.vutbr.fit.xhalas10.bp.utils;

import com.badlogic.gdx.math.Vector3;

public class Location {
    private double latitude, longitude, altitude;
    private static final double A = 6378137.0;
    private static final double B = 6356752.314245;
    private static final double F = (A - B) / A;
    private static final double E_SQ = 2.0 * F - F * F;
    private Vector3d cartesian = new Vector3d();
    private Vector3 normalVector = new Vector3();
    private Vector3 northVector = new Vector3();
    private Vector3 eastVector = new Vector3();
    private boolean updateCartesian = true;
    private boolean updateLocalSpaceVectors = true;
    private boolean updateCosSin = true;
    private double cosLat, cosLon, sinLat, sinLon;

    public Location(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        setUpdate();
    }

    public Location(Location location) {
        this(location.latitude, location.longitude, location.altitude);
    }

    public Location() {

    }

    public void set(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        setUpdate();
    }

    public void set(Location location) {
        set(location.latitude, location.longitude, location.altitude);
        setUpdate();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        setUpdate();
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        setUpdate();
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
        setUpdate();
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
            normalVector.set((float)(cosLat * sinLon), (float)(cosLat * cosLon), (float)-sinLat).nor();
            northVector.set((float)(-sinLat * sinLon), (float)(-sinLat * cosLon), (float)-cosLat).nor();
            eastVector.set((float)cosLon, (float)-sinLon, 0.0f).nor();
            updateLocalSpaceVectors = false;
        }
    }

    public Vector3d toCartesian() {
        updateCosSin();
        if (updateCartesian) {
            double N = A / Math.sqrt(1.0 - E_SQ * sinLat * sinLat);
            double x = (N + altitude) * cosLat * sinLon;
            double y = (N + altitude) * cosLat * cosLon;
            double z = -(N * (1.0 - E_SQ) + altitude) * sinLat;
            cartesian.set(x, y, z);
            updateCartesian = false;
        }
        return cartesian;
    }

    public Vector3 getNorthPointingVector() {
        updateLocalSpaceVectors();
        return northVector.cpy();
    }

    public Vector3 getEastPointingVector() {
        updateLocalSpaceVectors();
        return eastVector.cpy();
    }

    public Vector3 getUpPointingVector() {
        updateLocalSpaceVectors();
        return normalVector.cpy();
    }
}

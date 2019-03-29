package cz.vutbr.fit.xhalas10.bp.osm.model;

import com.google.maps.model.LatLng;

public class Node {
    long id;
    LatLng location;
    private double elevation;
    private String name;

    public Node(long id, LatLng location, String name) {
        this.id = id;
        this.location = location;
        this.elevation = Double.NaN;
        this.name = name;
    }

    public Node(long id, LatLng location, double elevation, String name) {
        this.location = location;
        this.elevation = elevation;
        this.name = name;
    }

    public boolean hasElevation() {
        return !Double.isNaN(elevation);
    }

    public LatLng getLocation() {
        return location;
    }

    public double getElevation() {
        return elevation;
    }

    public String getName() {
        return name;
    }

    void setElevation(double elevation) {
        this.elevation = elevation;
    }
}

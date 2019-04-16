package cz.vutbr.fit.xhalas10.bp.osm.model;

import com.google.maps.model.LatLng;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.HashMap;

public class Node implements Serializable {
    private Long id;
    private LatLng location;
    private HashMap<String, String> tags;
    private double elevation;

    public Node (Element node) {
        this.tags = new HashMap<>();
        id = Long.parseLong(node.getAttribute("id"));
        location = new LatLng(Double.parseDouble(node.getAttribute("lat")), Double.parseDouble(node.getAttribute("lon")));
        NodeList tags = node.getElementsByTagName("tag");
        for (int i = 0; i < tags.getLength(); i++) {
            Element tag = (Element)tags.item(i);
            this.tags.put(tag.getAttribute("k"), tag.getAttribute("v"));
        }
        elevation = Double.NaN;
    }

    public Long getId() {
        return id;
    }

    public LatLng getLocation() {
        return location;
    }

    public HashMap<String, String> getTags() {
        return tags;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }


    public boolean hasElevation() {
        return !Double.isNaN(elevation);
    }
}

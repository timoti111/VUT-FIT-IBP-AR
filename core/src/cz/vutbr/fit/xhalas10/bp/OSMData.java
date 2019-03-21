package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.Gdx;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import cz.vutbr.fit.xhalas10.bp.utils.OSMWrapperAPI;

public class OSMData {
    private HashMap<LatLng, OSMNode> nodes;
    private GeoApiContext context;
    private final static String DEFAULT_MAP = "/storage/emulated/0/Android/data/cz.vutbr.fit.xhalas10.bp/files/maps/default.map";

    private static final OSMData ourInstance = new OSMData();
    public static OSMData getInstance() {
        return ourInstance;
    }

    private OSMData() {
        if (Gdx.files.absolute(DEFAULT_MAP).exists()) {
            InputStream inputStream = Gdx.files.absolute(DEFAULT_MAP).read();
            try {
                ObjectInputStream is = new ObjectInputStream(inputStream);
                nodes = (HashMap)is.readObject();
                is.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        nodes = new HashMap<LatLng, OSMNode>();
    }

    public void setGeoApiContext(GeoApiContext context) {
        this.context = context;
    }
    
    public void getSurroundingData(LatLng location, double vicinityRange) {
        DecimalFormat format = new DecimalFormat("##0.0000000", DecimalFormatSymbols.getInstance(Locale.ENGLISH)); //$NON-NLS-1$
        String south = format.format(location.lat - vicinityRange);
        String west = format.format(location.lng - vicinityRange);
        String north = format.format(location.lat + vicinityRange);
        String east = format.format(location.lng + vicinityRange);
        String query = "node[name](" + south + "," + west + "," + north + "," + east + ");out;";

        Document doc;
        try {
            doc = OSMWrapperAPI.getNodesViaOverpass(query);
            //doc = OSMWrapperAPI.getNodesViaOverpass("node[name](49.2250000,16.5640000,49.2370000,16.5790000);out;");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ArrayList<LatLng> toDetermineElevation = new ArrayList<LatLng>();

        NodeList nodeList = doc.getElementsByTagName("node");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element)nodeList.item(i);
            //long id = Long.parseLong(element.getAttribute("id"));
            double latitude = Double.parseDouble(element.getAttribute("lat"));
            double longitude = Double.parseDouble(element.getAttribute("lon"));
            NodeList tags = element.getElementsByTagName("tag");
            String name = "";
            for (int j = 0; j < tags.getLength(); j++) {
                Element tag = (Element)tags.item(j);
                if (tag.getAttribute("k").equals("name")) {
                    name = tag.getAttribute("v");
                    break;
                }
            }

            if (!name.isEmpty()) {
                LatLng nodeLocation = new LatLng(latitude, longitude);
                if (!nodes.containsKey(nodeLocation)) {
                    OSMNode node = new OSMNode(nodeLocation, name);
                    nodes.put(node.location, node);
                    toDetermineElevation.add(nodeLocation);
                }
            }
        }
        getElevations(toDetermineElevation);
    }

    public Collection<OSMNode> getOSMNodes() {
        return nodes.values();
    }

    public void checkNodeElevations() {
        ArrayList<LatLng> toDetermineElevation = new ArrayList<LatLng>();
        for (OSMNode node : nodes.values()) {
            if (!node.hasElevation()) {
                toDetermineElevation.add(node.location);
            }
        }
        getElevations(toDetermineElevation);
    }


    private void getElevations(ArrayList<LatLng> toDetermineElevation) {
        if (toDetermineElevation.isEmpty())
            return;

        EncodedPolyline encodedPolyline = new EncodedPolyline(toDetermineElevation);
        ElevationResult[] results;
        try {
            results = ElevationApi.getByPoints(context, encodedPolyline).await();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (results.length != toDetermineElevation.size()) {
            return;
        }

        for (int i = 0; i < toDetermineElevation.size(); i++) {
            OSMNode node = nodes.get(toDetermineElevation.get(i));
            if (node != null)
                node.setElevation(results[i].elevation);
        }

        updateMap();
    }

    private void updateMap() {
        OutputStream outputStream = Gdx.files.absolute(DEFAULT_MAP).write(false);
        try {
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(nodes);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class OSMNode implements Serializable {
    LatLng location;
    private double elevation;
    private String name;

    public OSMNode(LatLng location, String name) {
        this.location = location;
        this.elevation = Double.NaN;
        this.name = name;
    }

    public OSMNode(LatLng location, double elevation, String name) {
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
package cz.vutbr.fit.xhalas10.bp.osm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import cz.vutbr.fit.xhalas10.bp.osm.model.Node;
import cz.vutbr.fit.xhalas10.bp.utils.Location;

public class OSMData {
    private HashMap<Long, Node> nodes;
    private GeoApiContext context;
    private final static String DEFAULT_MAP = "maps/default.map";

    private static final OSMData ourInstance = new OSMData();
    public static OSMData getInstance() {
        return ourInstance;
    }

    private OSMData() {
        if (Gdx.files.local(DEFAULT_MAP).exists()) {
            InputStream inputStream = Gdx.files.local(DEFAULT_MAP).read();
            try {
                ObjectInputStream is = new ObjectInputStream(inputStream);
                nodes = (HashMap<Long, Node>)is.readObject();
                is.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        nodes = new HashMap<Long, Node>();
    }
    
    public void getSurroundingData(Location location, double vicinityRange) {
        DecimalFormat format = new DecimalFormat("##0.0000000", DecimalFormatSymbols.getInstance(Locale.ENGLISH)); //$NON-NLS-1$
        String south = format.format(location.getLatitude() - vicinityRange);
        String west = format.format(location.getLongitude() - vicinityRange);
        String north = format.format(location.getLatitude() + vicinityRange);
        String east = format.format(location.getLongitude() + vicinityRange);
        String bbox = "(" + south + "," + west + "," + north + "," + east + ")";
        String query = "node[\"waterway\"~\"waterfall\"]" + bbox + ";out;\n" +
                "node[\"information\"~\"^(guidepost|map|board)$\"][\"tourism\"=\"information\"]" + bbox + ";out;\n" +
                "node[\"tourism\"~\"^(viewpoint|wilderness_hut|alpine_hut)$\"]" + bbox + ";out;\n" +
                "node[\"natural\"~\"^(peak|spring|hot_spring|volcano|rock|saddle|stone|cave_entrance)$\"]" + bbox + ";out;";

        Document doc;
        try {
            doc = OSMWrapperAPI.getNodesViaOverpass(query);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ArrayList<Node> toDetermineElevation = new ArrayList<Node>();
        NodeList nodeList = doc.getElementsByTagName("node");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element)nodeList.item(i);
            if (!nodes.containsKey(Long.parseLong(element.getAttribute("id"))))
                toDetermineElevation.add(new Node(element));
        }

        ArrayList<Node> toDetermineElevationSubset = new ArrayList<Node>();

        for (Node node : toDetermineElevation) {
            toDetermineElevationSubset.add(node);
            if (toDetermineElevationSubset.size() > 200) {
                getElevations(toDetermineElevationSubset);
                toDetermineElevationSubset.clear();
            }
        }
        getElevations(toDetermineElevationSubset);
        updateMap();
    }

    public Collection<Node> getOSMNodes() {
        return nodes.values();
    }

    private void getElevations(ArrayList<Node> toDetermineElevation) {
        ArrayList<LatLng> locations = new ArrayList<LatLng>();
        for (Node node : toDetermineElevation) {
            locations.add(node.getLocation());
        }

        if (locations.isEmpty())
            return;

        EncodedPolyline encodedPolyline = new EncodedPolyline(locations);
        ElevationResult[] results;
        try {
            if (context == null) {
                this.context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBQaK3OYcPfdtMaVZUbzjVLfegmOOc7K-E")
                    .build();
            }

            results = ElevationApi.getByPoints(context, encodedPolyline).await();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (results.length != toDetermineElevation.size()) {
            return;
        }

        for (int i = 0; i < locations.size(); i++) {
            toDetermineElevation.get(i).setElevation(results[i].elevation);
            nodes.putIfAbsent(toDetermineElevation.get(i).getId(), toDetermineElevation.get(i));
        }
    }

    private void updateMap() {
        OutputStream outputStream = Gdx.files.local(DEFAULT_MAP).write(false);
        try {
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(nodes);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
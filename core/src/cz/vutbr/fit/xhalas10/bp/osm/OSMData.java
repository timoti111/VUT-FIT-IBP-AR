/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.osm;

import com.badlogic.gdx.Gdx;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.vutbr.fit.xhalas10.bp.earth.wgs84.Location;
import cz.vutbr.fit.xhalas10.bp.osm.model.Node;

/**
 * This class is for downloading surrounding data via Overpass API. When data is downloaded Google
 * Elevation API is used for getting missing height data. After everything is downloaded all is
 * saved to storage of device. After app is loaded all data is restored.
 */
public class OSMData {
    private final static String DEFAULT_MAP = "maps/default.map";
    private static final OSMData instance = new OSMData();
    private HashMap<Long, Node> nodes;
    private GeoApiContext context;

    private OSMData() {
        if (Gdx.files.local(DEFAULT_MAP).exists()) {
            InputStream inputStream = Gdx.files.local(DEFAULT_MAP).read();
            try {
                ObjectInputStream is = new ObjectInputStream(inputStream);
                //noinspection unchecked
                nodes = (HashMap<Long, Node>) is.readObject();
                is.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        nodes = new HashMap<>();
    }

    public static OSMData getInstance() {
        return instance;
    }

    public void getSurroundingData(Location location, double vicinityRangeMeters) {
        DecimalFormat format = new DecimalFormat("##0.0000000", DecimalFormatSymbols.getInstance(Locale.ENGLISH)); //$NON-NLS-1$
        String south = format.format(location.getLatitude() - vicinityRangeMeters);
        String west = format.format(location.getLongitude() - vicinityRangeMeters);
        String north = format.format(location.getLatitude() + vicinityRangeMeters);
        String east = format.format(location.getLongitude() + vicinityRangeMeters);
        String bbox = "(around:" + format.format(vicinityRangeMeters) + "," + format.format(location.getLatitude()) + "," + format.format(location.getLongitude()) + ")";
        String query = "node[\"waterway\"~\"waterfall\"]" + bbox + ";out;\n" +
                "node[\"information\"~\"^(guidepost|map|board)$\"][\"tourism\"=\"information\"]" + bbox + ";out;\n" +
                "node[\"tourism\"~\"^(viewpoint|wilderness_hut|alpine_hut)$\"]" + bbox + ";out;\n" +
                "node[\"natural\"~\"^(peak|spring|hot_spring|volcano|rock|saddle|stone|cave_entrance)$\"]" + bbox + ";out;";

        Document doc;
        try {
            doc = OverPassWrapperAPI.getNodesViaOverpass(query);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ArrayList<Node> toDetermineElevation = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("node");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            if (!nodes.containsKey(Long.parseLong(element.getAttribute("id")))) {
                Node node = new Node(element);
                if (!node.hasElevation()) {
                    toDetermineElevation.add(node);
                } else {
                    nodes.put(node.getId(), node);
                }
            }
        }

        ArrayList<Node> toDetermineElevationSubset = new ArrayList<>();

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
        ArrayList<LatLng> locations = new ArrayList<>();
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

    /**
     * This class only serves to send query to Overpass API server and returns parsed XML response
     * as Document class.
     */
    static class OverPassWrapperAPI {
        private static final String OVERPASS_API = "https://overpass.kumi.systems/api/interpreter";

        static Document getNodesViaOverpass(String query) throws IOException, ParserConfigurationException, SAXException {
            URL osm = new URL(OVERPASS_API);
            HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
            printout.writeBytes("data=" + URLEncoder.encode(query, "utf-8"));
            printout.flush();
            printout.close();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(connection.getInputStream());
        }
    }
}
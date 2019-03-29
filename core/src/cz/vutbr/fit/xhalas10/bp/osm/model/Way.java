package cz.vutbr.fit.xhalas10.bp.osm.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Way {
    private long id;
    private ArrayList<Node> nodes;
    private HashMap<String, String> tags;
}

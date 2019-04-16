package cz.vutbr.fit.xhalas10.bp;

import cz.vutbr.fit.xhalas10.bp.utils.Location;

public interface PersonLocation {
    Location getLocation();
    double getVerticalAccuracy();
    double getHorizontalAccuracy();
}

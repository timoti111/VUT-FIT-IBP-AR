/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces;

import cz.vutbr.fit.xhalas10.bp.earth.wgs84.Location;

/**
 * This is interface for core module for communication with platform specific location
 * services.
 */
public interface IDeviceLocation {
    Location getLocation();
}

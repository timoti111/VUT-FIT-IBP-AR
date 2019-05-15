/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.utils;

import com.badlogic.gdx.math.Vector3;

public class Vector3d {
    private double x;
    private double y;
    private double z;

    private Vector3d(Vector3d vector3d) {
        this.x = vector3d.x;
        this.y = vector3d.y;
        this.z = vector3d.z;
    }

    public Vector3d(Vector3 vector3) {
        this.x = vector3.x;
        this.y = vector3.y;
        this.z = vector3.z;
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d() {
    }

    public double len() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3d cpy() {
        return new Vector3d(this);
    }

    public double dst(final Vector3d vector) {
        final double a = vector.x - x;
        final double b = vector.y - y;
        final double c = vector.z - z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public Vector3d set(Vector3d vector3d) {
        this.x = vector3d.x;
        this.y = vector3d.y;
        this.z = vector3d.z;
        return this;
    }

    public Vector3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3d sub(final Vector3d a_vec) {
        return this.sub(a_vec.x, a_vec.y, a_vec.z);
    }

    private Vector3d sub(double x, double y, double z) {
        return this.set(this.x - x, this.y - y, this.z - z);
    }

    public Vector3 toVector3() {
        return new Vector3((float) this.x, (float) this.y, (float) this.z);
    }


}

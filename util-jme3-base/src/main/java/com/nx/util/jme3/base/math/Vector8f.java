package com.nx.util.jme3.base.math;

/**
 * TODO: This class should be named differently
 * Created by NemesisMate on 23/03/17.
 */
public class Vector8f {

    public int x, y;

    public float z, w;

    public int r, g, b, a;

    public Vector8f() {}

    public Vector8f(int x, int y, float z, float w, int r, int g, int b, int a) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }
}

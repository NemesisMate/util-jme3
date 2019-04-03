package com.nx.util.jme3.base;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public final class VectorUtil {

    private VectorUtil() { }

    /**
     * Took from: https://stackoverflow.com/a/8839181.
     * TODO: check if it is normalized and doc it
     * @param store
     * @return
     */
    public static Vector3f randomVectorUniform(Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        }

        float z = FastMath.nextRandomFloat() * 2 - 1;

        float rxy = FastMath.sqrt(1 - z * z);
        float phi = FastMath.nextRandomFloat() * FastMath.TWO_PI;

        float x = rxy * FastMath.cos(phi);
        float y = rxy * FastMath.sin(phi);

        return store.set(x, y, z);
    }

    public static Vector3f randomVector(Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        }

        return store.set(FastMath.nextRandomFloat() * 2 - 1, FastMath.nextRandomFloat() * 2 - 1, FastMath.nextRandomFloat() * 2 - 1);
    }

    public static Vector3f get90DegreeVector(Vector3f vector, Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        }

        Vector3f aux = vector.equals(Vector3f.UNIT_X) ? Vector3f.UNIT_Y : Vector3f.UNIT_X;

        return vector.cross(aux, store).normalizeLocal();
    }

    public static Vector3f clamp(Vector3f input, Vector3f min, Vector3f max) {
        input.x = FastMath.clamp(input.x, min.x, max.x);
        input.y = FastMath.clamp(input.y, min.y, max.y);
        input.z = FastMath.clamp(input.z, min.z, max.z);

        return input;
    }

}

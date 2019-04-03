package com.nx.util.jme3.base.math;

import com.jme3.math.Vector3f;

import java.io.Serializable;

/**
 * TODO: Add to utils
 */
public final class Vector3i implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public int x;
	public int y;
	public int z;

	public Vector3i(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3i() {
		x = 0;
		y = 0;
		z = 0;
	}

	public Vector3i(Vector3i vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}

	public Vector3i(Vector3f vec) {
		x = (int) vec.x;
		y = (int) vec.y;
		z = (int) vec.z;
	}

	public Vector3i(float x, float y, float z) {
		this.x = (int) x;
		this.y = (int) y;
		this.z = (int) z;
	}

    public Vector3i set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

	public Vector3i set(Vector3i other) {
		x = other.x;
		y = other.y;
		z = other.z;
		return this;
	}

	public Vector3i set(Vector3f other) {
		x = (int) other.x;
		y = (int) other.y;
		z = (int) other.z;
		return this;
	}

    public Vector3i addLocal(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

	public Vector3i addLocal(Vector3i other) {
		x += other.x;
		y += other.y;
		z += other.z;
		return this;
	}

	public Vector3i subtractLocal(Vector3i other) {
		x -= other.x;
		y -= other.y;
		z -= other.z;
		return this;
	}

	public Vector3i multLocal(int scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	public Vector3i divideLocal(int scalar) {
		x /= scalar;
		y /= scalar;
		z /= scalar;
		return this;
	}

	public Vector3i add(Vector3i other) {
		return new Vector3i(x + other.x, y + other.y, z + other.z);
	}

	public Vector3i subtract(Vector3i other) {
		return new Vector3i(x - other.x, y - other.y, z - other.z);
	}

	public Vector3i mult(int scalar) {
		return new Vector3i(x * scalar, y * scalar, z * scalar);
	}

	public Vector3i divide(int scalar) {
		return new Vector3i(x / scalar, y / scalar, z / scalar);
	}

	@Override
	public Vector3i clone() {
		return new Vector3i(x, y, z);
	}

	public Vector3f toFloat()
	{
		return new Vector3f(x, y, z);
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + "," + z + ")";
	}

    @Override
    public int hashCode() {
        int hash = 37;
        hash += 37 * hash + (x);
        hash += 37 * hash + (y);
        hash += 37 * hash + (z);
        return hash;
    }

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof Vector3i)) {
			return false;
		}

		final Vector3i vec = (Vector3i) other;

		return vec.x == x && vec.y == y && vec.z == z;
	}
}
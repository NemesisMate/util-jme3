package com.nx.util.jme3.base.math;

import com.jme3.math.Vector2f;

import java.io.Serializable;

/**
 * TODO: Add to utils
 */
public final class Vector2i implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public int x;
	public int y;

	public Vector2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vector2i() {
		x = 0;
		y = 0;
	}

	public Vector2i(Vector2i vec) {
		x = vec.x;
		y = vec.y;
	}

	public Vector2i(Vector2f vec) {
		x = (int) vec.x;
		y = (int) vec.y;
	}

	public Vector2i(float x, float y, float z) {
		this.x = (int) x;
		this.y = (int) y;
	}

    public Vector2i set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

	public Vector2i set(Vector2i other) {
		x = other.x;
		y = other.y;
		return this;
	}

	public Vector2i set(Vector2f other) {
		x = (int) other.x;
		y = (int) other.y;
		return this;
	}

    public Vector2i addLocal(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

	public Vector2i addLocal(Vector2i other) {
		x += other.x;
		y += other.y;
		return this;
	}

	public Vector2i subtractLocal(Vector2i other) {
		x -= other.x;
		y -= other.y;
		return this;
	}

	public Vector2i multLocal(int scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}

	public Vector2i divideLocal(int scalar) {
		x /= scalar;
		y /= scalar;
		return this;
	}

	public Vector2i add(Vector2i other) {
		return new Vector2i(x + other.x, y + other.y);
	}

	public Vector2i subtract(Vector2i other) {
		return new Vector2i(x - other.x, y - other.y);
	}

	public Vector2i mult(int scalar) {
		return new Vector2i(x * scalar, y * scalar);
	}

	public Vector2i divide(int scalar) {
		return new Vector2i(x / scalar, y / scalar);
	}

	@Override
	public Vector2i clone() {
		return new Vector2i(x, y);
	}

	public Vector2f toFloat() {
		return new Vector2f(x, y);
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

    @Override
    public int hashCode() {
        int hash = 37;
        hash += 37 * hash + (x);
        hash += 37 * hash + (y);
        return hash;
    }

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof Vector2i)) {
			return false;
		}

		final Vector2i vec = (Vector2i) other;

		return vec.x == x && vec.y == y;
	}
}
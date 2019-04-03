package com.nx.util.jme3.lemur.component;

import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.nx.util.jme3.base.math.Vector8f;

/**
 * @author NemesisMate, based on Paul Speed's TbtQuad.
 *
 * Coords in Vector8f are taken like:
 *
 * x -> x offset of bottom-left region's corner (offset origin: image bottom-left)
 * y -> y offset of bottom-left region's corner (offset origin: image bottom-left)
 * z -> image region's width.
 * w -> image region's height.
 * r == x1 -> x offset of bottom-left region's inner corner (offset origin: region bottom-left)
 * g == y1 -> y offset of bottom-left region's inner corner (offset origin: region bottom-left)
 * b == x2 -> x offset of top-right region's inner corner (offset origin: region bottom-left)
 * a == y2 -> y offset of top-right region's inner corner (offset origin: region bottom-left)
 *
 * @see com.simsilica.lemur.geom.TbtQuad
 */
public class AutoTbtQuad extends Mesh
        implements Cloneable {

    private Vector2f size;
    private Vector2f imageSize;
    private float[] horzTexCoords;
    private float[] vertTexCoords;

    private float[] horzFolds;
    private float[] vertFolds;

    public AutoTbtQuad(float width, float height) {
        this.size = new Vector2f(width, height);
        this.imageSize = new Vector2f(width, height);

        this.horzFolds = new float[] { width/3f, 2 * width/3f };
        this.vertFolds = new float[] { height/3f, 2 * height/3f };

        this.horzTexCoords = new float[]{0, 1 / 3f, 2 / 3f, 1};
        this.vertTexCoords = new float[]{0, 1 / 3f, 2 / 3f, 1};

        refreshGeometry();
    }

    public AutoTbtQuad(Vector8f values, int imageWidth, int imageHeight) {
        this(values, imageWidth, imageHeight, 1f);
    }

    public AutoTbtQuad(Vector8f values, int imageWidth, int imageHeight, float scale) {
        this(values.x, values.y, values.z, values.w, values.r, values.g, values.b, values.a, imageWidth, imageHeight, scale);
    }

    public AutoTbtQuad(int regionOffsetX, int regionOffsetY, float regionWidth, float regionHeight, int x1, int y1, int x2, int y2, int imageWidth, int imageHeight) {
        this(regionOffsetX, regionOffsetY, regionWidth, regionHeight, x1, y1, x2, y2, imageWidth, imageHeight, 1);
    }

    public AutoTbtQuad(int regionOffsetX, int regionOffsetY, float regionWidth, float regionHeight, int x1, int y1, int x2, int y2, int imageWidth, int imageHeight, float imageScale) {
        this.imageSize = new Vector2f(imageWidth, imageHeight);

        setTexCoords(regionOffsetX, regionOffsetY, regionWidth, regionHeight, x1, y1, x2, y2, imageScale);
//        this.size = new Vector2f(regionWidth, regionHeight);
//
//        float offsetX = regionOffsetX / (float) imageWidth;
//        float offsetY = regionOffsetY / (float) imageHeight;
//
////        horzFolds = new float[] { imageScale * x1, imageScale * x2 };
////        vertFolds = new float[] { imageScale * y1, imageScale * y2 };
//
//        horzFolds = new float[] { imageScale * x1, (regionWidth - x2) * imageScale };
//        vertFolds = new float[] { imageScale * y1, (regionHeight - y2) * imageScale };
//
//        this.horzTexCoords = new float[]{ offsetX, (float) x1 / imageWidth + offsetX, (float) x2 / imageWidth + offsetX, offsetX + regionWidth / imageWidth };
//        this.vertTexCoords = new float[]{ offsetY, (float) y1 / imageHeight + offsetY, (float) y2 / imageHeight + offsetY, offsetY + regionHeight / imageHeight };
//
//
//        refreshGeometry();


    }

    public void setTexCoords(Vector8f values) {
        setTexCoords(values, 1);
    }

    public void setTexCoords(Vector8f values, float imageScale) {
        setTexCoords(values.x, values.y, values.z, values.w, values.r, values.g, values.b, values.a, imageScale);
    }

    public void setTexCoords(int regionOffsetX, int regionOffsetY, float regionWidth, float regionHeight, int x1, int y1, int x2, int y2, float imageScale) {
        this.size = new Vector2f(regionWidth, regionHeight);

        float imageWidth = imageSize.x;
        float imageHeight = imageSize.y;

        float offsetX = regionOffsetX / (float) imageWidth;
        float offsetY = regionOffsetY / (float) imageHeight;

//        horzFolds = new float[] { imageScale * x1, imageScale * x2 };
//        vertFolds = new float[] { imageScale * y1, imageScale * y2 };

        horzFolds = new float[] { imageScale * x1, (regionWidth - x2) * imageScale };
        vertFolds = new float[] { imageScale * y1, (regionHeight - y2) * imageScale };

        this.horzTexCoords = new float[]{ offsetX, (float) x1 / imageWidth + offsetX, (float) x2 / imageWidth + offsetX, offsetX + regionWidth / imageWidth };
        this.vertTexCoords = new float[]{ offsetY, (float) y1 / imageHeight + offsetY, (float) y2 / imageHeight + offsetY, offsetY + regionHeight / imageHeight };

        refreshGeometry();
    }

    public AutoTbtQuad clone() {
        AutoTbtQuad result = (AutoTbtQuad) super.deepClone();

        result.size = size.clone();
        result.imageSize = imageSize.clone();
        result.horzTexCoords = horzTexCoords.clone();
        result.vertTexCoords = vertTexCoords.clone();

        return result;
    }

    public Vector2f getSize() {
        return size;
    }

    public void updateSize(float width, float height) {
        if (size.x == width && size.y == height) {
            return;
        }

        size.set(width, height);
        refreshGeometry();
    }

    protected void refreshGeometry() {
        // Vertexes are arranged as:
        //
        //  9 -- 8 -- 7 -- 6
        //  | \  | /  | /  |
        // 10 --15 --14 -- 5
        //  | /  | /  | /  |
        // 11 --12 --13 -- 4
        //  | /  | /  | \  |
        //  0 -- 1 -- 2 -- 3
        //
        // Note: some of the corners are flipped to better support extrusion
        // if the caller desires to pull up the center quad.

        setBuffer(VertexBuffer.Type.Index, 3, new short[]{
                0, 1, 12,
                0, 12, 11,
                1, 2, 13,
                1, 13, 12,
                2, 3, 13,
                3, 4, 13,
                13, 4, 5,
                13, 5, 14,
                14, 5, 6,
                14, 6, 7,
                15, 14, 7,
                15, 7, 8,
                10, 15, 9,
                15, 8, 9,
                11, 12, 15,
                11, 15, 10,

                // The center
                12, 13, 14,
                12, 14, 15
        });

        float x0 = horzFolds[0];
        float x1 = size.x - horzFolds[1];
        float y0 = vertFolds[0];
        float y1 = size.y - vertFolds[1];

        setBuffer(VertexBuffer.Type.Position, 3, new float[] {
                0, 0, 0,
                x0, 0, 0,
                x1, 0, 0,
                size.x, 0, 0,
                size.x, y0, 0,
                size.x, y1, 0,
                size.x, size.y, 0,
                x1, size.y, 0,
                x0, size.y, 0,
                0, size.y, 0,
                0, y1, 0,
                0, y0, 0,
                // The center
                x0, y0, 0,
                x1, y0, 0,
                x1, y1, 0,
                x0, y1, 0
        });
        setBuffer(VertexBuffer.Type.TexCoord, 2, new float[]{
                horzTexCoords[0], vertTexCoords[0],
                horzTexCoords[1], vertTexCoords[0],
                horzTexCoords[2], vertTexCoords[0],
                horzTexCoords[3], vertTexCoords[0],
                horzTexCoords[3], vertTexCoords[1],
                horzTexCoords[3], vertTexCoords[2],
                horzTexCoords[3], vertTexCoords[3],
                horzTexCoords[2], vertTexCoords[3],
                horzTexCoords[1], vertTexCoords[3],
                horzTexCoords[0], vertTexCoords[3],
                horzTexCoords[0], vertTexCoords[2],
                horzTexCoords[0], vertTexCoords[1],
                // The center
                horzTexCoords[1], vertTexCoords[1],
                horzTexCoords[2], vertTexCoords[1],
                horzTexCoords[2], vertTexCoords[2],
                horzTexCoords[1], vertTexCoords[2]
        });

        setBuffer(VertexBuffer.Type.Normal, 3, new float[]{
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                // The center
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1
        });

        updateBound();
    }
}
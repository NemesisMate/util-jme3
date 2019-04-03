package com.nx.util.jme3.lemur.component;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

import java.io.IOException;

public class CoordedQuad extends Mesh {

    private float width;
    private float height;

    /**
     * Serialization only. Do not use.
     */
    public CoordedQuad(){
    }

    /**
     * Create a quad with the given width and height. The quad
     * is always created in the XY plane.
     *
     * @param width The X extent or width
     * @param height The Y extent or width
     */
    public CoordedQuad(float width, float height){
        this(width, height, null);
    }

    /**
     * Create a quad with the given width and height. The quad
     * is always created in the XY plane.
     *
     * @param width The X extent or width
     * @param height The Y extent or width
     * along the Y axis.
     */
    public CoordedQuad(float width, float height, float[] texCoords){
//        setBuffer(VertexBuffer.Type.Normal, 3, new float[] {
//                0, 0, 1,
//                0, 0, 1,
//                0, 0, 1,
//                0, 0, 1
//        });


        updateGeometry(width, height, texCoords);
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public void updateGeometry(float width, float height){
        updateGeometry(width, height, null);
    }

    public void updateGeometry(float width, float height, float[] texCoords) {
        this.width = width;
        this.height = height;

        setBuffer(VertexBuffer.Type.Position, 3, new float[] {
                                                                        0,      0,      0,
                                                                        width,  0,      0,
                                                                        width,  height, 0,
                                                                        0,      height, 0
        });


        if(texCoords != null) {
            setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
        }



        if (height < 0){
            setBuffer(VertexBuffer.Type.Index, 3, new short[] {
                                                                            0, 2, 1,
                                                                            0, 3, 2
            });
        } else{
            setBuffer(VertexBuffer.Type.Index, 3, new short[] {
                                                                            0, 1, 2,
                                                                            0, 2, 3
            });
        }

        updateBound();
        setStatic();
    }

    @Override
    public void read(JmeImporter e) throws IOException {
        super.read(e);
        InputCapsule capsule = e.getCapsule(this);
        width = capsule.readFloat("width", 0);
        height = capsule.readFloat("height", 0);
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        super.write(e);
        OutputCapsule capsule = e.getCapsule(this);
        capsule.write(width, "width", 0);
        capsule.write(height, "height", 0);
    }
}

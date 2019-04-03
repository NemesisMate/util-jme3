package com.nx.util.jme3.lemur.layout;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.core.GuiControl;

/**
 * Created by inaki on 13/01/17.
 */
public class SmoothAnimatedLayout extends SingleChildLayout {

    //TODO: Add speed

    Vector3f newPrefSize = new Vector3f();
    float interpolateAmount;

    long lastTime;


    @Override
    public void calculatePreferredSize(Vector3f size, Node child) {
        if(child instanceof Panel) {

            long time = System.nanoTime();

            Vector3f childPrefSize = ((Panel) child).getPreferredSize();
//            if(newPrefSize == null) {
//                newPrefSize = new Vector3f(childPrefSize);
//            } else {
                if(interpolateAmount != 0f || newPrefSize.distanceSquared(childPrefSize) > FastMath.ZERO_TOLERANCE) {
                    float tpf = (time - lastTime) / 1000000000f;

                    if(interpolateAmount != 0) {
                        interpolateAmount += tpf;
                    } else {
                        // Each time time this layout starts to change, the lastTime was last called a long time ago (so the tpf would always be 1)
                        interpolateAmount += FastMath.ZERO_TOLERANCE;
                    }

                    if (interpolateAmount >= 1f) {
                        interpolateAmount = 1f;
                    } else {
                        invalidate();
                    }

//                    System.out.println("SIZE IS: " + newPrefSize + ", interpolateAmount: " + interpolateAmount + ", childPref: " + childPrefSize);

                    newPrefSize.interpolateLocal(childPrefSize, interpolateAmount);

                    if (Float.compare(interpolateAmount, 1f) == 0f) {
//                        System.out.println("Interporlate set to: 0");
                        interpolateAmount = 0f;
                    }
                }
//            }

            lastTime = time;

            size.set(newPrefSize);
        }
    }

    @Override
    public void reshape(Vector3f pos, Vector3f size, Node child) {
        child.setLocalTranslation(pos);
        child.getControl(GuiControl.class).setSize(size);
    }
}

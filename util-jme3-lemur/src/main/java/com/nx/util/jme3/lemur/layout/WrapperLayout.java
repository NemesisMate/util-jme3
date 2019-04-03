package com.nx.util.jme3.lemur.layout;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.core.GuiControl;

/**
 * Created by inaki on 13/01/17.
 */
public class WrapperLayout extends SingleChildLayout {

    @Override
    public void calculatePreferredSize(Vector3f size, Node child) {
        if(child instanceof Panel) {
            size.set(((Panel) child).getPreferredSize());
        }
    }

    @Override
    public void reshape(Vector3f pos, Vector3f size, Node child) {
        child.setLocalTranslation(pos);
        child.getControl(GuiControl.class).setSize(size);
    }
}

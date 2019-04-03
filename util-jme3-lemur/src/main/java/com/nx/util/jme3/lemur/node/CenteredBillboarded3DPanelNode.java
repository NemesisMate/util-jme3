package com.nx.util.jme3.lemur.node;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.simsilica.lemur.core.AbstractGuiControlListener;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiControlListener;

/**
 * Created by NemesisMate on 16/06/17.
 */
public class CenteredBillboarded3DPanelNode extends Node {

    private static final GuiControlListener listener = new AbstractGuiControlListener() {
        @Override
        public void reshape(GuiControl source, Vector3f pos, Vector3f size) {
            source.getNode().move(-size.x / 2f, size.y / 2f, 0);
        }
    };

    public CenteredBillboarded3DPanelNode() {
        this(null);
    }

    public CenteredBillboarded3DPanelNode(String name) {
        super(name);

        this.addControl(new BillboardControl());
    }

    @Override
    public int attachChildAt(Spatial child, int index) {
        checkAttachedChild(child);

        return super.attachChildAt(child, index);
    }

    @Override
    public Spatial detachChildAt(int index) {
        Spatial removed = super.detachChildAt(index);
        checkDettachedChild(removed);
        return removed;
    }

    private void checkDettachedChild(Spatial child) {
        if(child == null) {
            return;
        }

        GuiControl guiControl = child.getControl(GuiControl.class);
        if(guiControl == null) {
            return;
        }

        guiControl.removeListener(listener);
    }

    private void checkAttachedChild(Spatial child) {
        GuiControl guiControl = child.getControl(GuiControl.class);
        if(guiControl == null) {
            return;
        }

        guiControl.addListener(listener);
    }
}

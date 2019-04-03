package com.nx.util.jme3.lemur.layout;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.core.GuiControl;

/**
 * Created by NemesisMate on 27/01/17.
 */
public class CenterAlignLayout extends SingleChildLayout {

    HAlignment hAlignment;
    VAlignment vAlignment;

    //TODO: is this necessary and fine?, couldn't just make it non-proportional and add a proportional-springgridlayout container inside?

    FillMode fillModeX = FillMode.Proportional;
    FillMode fillModeY = FillMode.Proportional;


//    boolean proportional = true;
//    boolean expandX =


    public CenterAlignLayout() {
        this(HAlignment.Center, VAlignment.Center, FillMode.Proportional, FillMode.Proportional);
    }

    public CenterAlignLayout(HAlignment hAlignment) {
        this(hAlignment, VAlignment.Center, FillMode.Proportional, FillMode.Proportional);
    }

    public CenterAlignLayout(VAlignment vAlignment) {
        this(HAlignment.Center, vAlignment, FillMode.Proportional, FillMode.Proportional);
    }

    public CenterAlignLayout(HAlignment hAlignment, FillMode fillModeX, FillMode fillModeY) {
        this(hAlignment, VAlignment.Center, fillModeX, fillModeY);
    }

    public CenterAlignLayout(VAlignment vAlignment, FillMode fillModeX, FillMode fillModeY) {
        this(HAlignment.Center, vAlignment, fillModeX, fillModeY);
    }

    public CenterAlignLayout(HAlignment hAlignment, VAlignment vAlignment) {
        this(hAlignment, vAlignment, FillMode.Proportional, FillMode.Proportional);
    }

    public CenterAlignLayout(FillMode fillModeX, FillMode fillModeY) {
        this(HAlignment.Center, VAlignment.Center);

        this.fillModeX = fillModeX;
        this.fillModeY = fillModeY;
    }

    public CenterAlignLayout(HAlignment hAlignment, VAlignment vAlignment, FillMode fillModeX, FillMode fillModeY) {
        this.hAlignment = hAlignment;
        this.vAlignment = vAlignment;
        this.fillModeX = fillModeX;
        this.fillModeY = fillModeY;
    }

    public HAlignment gethAlignment() {
        return hAlignment;
    }

    public void sethAlignment(HAlignment hAlignment) {
        if(this.hAlignment != hAlignment) {
            this.hAlignment = hAlignment;

            invalidate();
        }
    }

    public VAlignment getvAlignment() {
        return vAlignment;
    }

    public void setvAlignment(VAlignment vAlignment) {
        if(this.vAlignment != vAlignment) {
            this.vAlignment = vAlignment;

            invalidate();
        }
    }

    public void setAlignment(HAlignment hAlignment, VAlignment vAlignment) {
        sethAlignment(hAlignment);
        setvAlignment(vAlignment);
    }

    @Override
    public CenterAlignLayout clone() {
        // Easier and better to just instantiate with the proper
        // settings
        CenterAlignLayout result = new CenterAlignLayout();
        return result;
    }


    @Override
    protected void calculatePreferredSize(Vector3f size, Node child) {
        if(child instanceof Panel) {
            size.set(((Panel) child).getPreferredSize());
        }
    }

    @Override
    protected void reshape(Vector3f pos, Vector3f size, Node child) {
        if(child != null) {
            Vector3f prefSize = ((Panel)child).getPreferredSize();

            GuiControl guiControl = child.getControl(GuiControl.class);
            Vector3f applySize = guiControl.getSize(); // WARNING!: If the implementation changes, this could fail, if is the case, instantiate here a new vector.

            switch (fillModeX) {
                case Proportional:
                    applySize.x = size.x * prefSize.x / 100f;
                    break;
                case None:
                    applySize.x = prefSize.x;
                    break;
                default:
                    applySize.x = size.x;
                    break;

            }

            switch (fillModeY) {
                case Proportional:
                    applySize.y = size.y * prefSize.y / 100f;
                    break;
                case None:
                    applySize.y = prefSize.y;
                    break;
                default:
                    applySize.y = size.y;
                    break;
            }

            guiControl.setSize(applySize);

            float x = 0;
            float y = 0;

            switch (hAlignment) {
                case Center:
                    x = (size.x - applySize.x) / 2f;
                    break;
                case Right:
                    x = size.x - applySize.x;
                    break;
            }

            switch (vAlignment) {
                case Center:
                    y = (size.y - applySize.y) / -2f;
                    break;
                case Bottom:
                    y = -(size.y - applySize.y);
                    break;
            }

            // WARNING!: If the implementation changes, this could fail, if is the case, instantiate here a new vector.
//            child.setLocalTranslation(child.getLocalTranslation().set(pos).addLocal(
//                                                                            (size.x - applySize.x) / 2f,
//                                                                            (size.y - applySize.y) / -2f,
//                                                                            0));
            child.setLocalTranslation(child.getLocalTranslation().set(pos).addLocal(x, y, 0));
        }
    }

    public FillMode getFillModeX() {
        return fillModeX;
    }

    public void setFillModeX(FillMode fillModeX) {
        this.fillModeX = fillModeX;
    }

    public FillMode getFillModeY() {
        return fillModeY;
    }

    public void setFillModeY(FillMode fillModeY) {
        this.fillModeY = fillModeY;
    }
}

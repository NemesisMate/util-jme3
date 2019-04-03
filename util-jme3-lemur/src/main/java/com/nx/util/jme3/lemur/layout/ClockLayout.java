package com.nx.util.jme3.lemur.layout;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.component.AbstractGuiComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TODO: when only one child, show it in the center.
 * TODO: there is much more optimization to do here. Just take a look xD.
 * Created by NemesisMate on 16/01/17.
 */
public class ClockLayout extends AbstractGuiComponent
        implements GuiLayout, Cloneable {

    private GuiControl parent;
//    private Axis axis;
//    private float rotation;
    private FillMode fill;

    private List<Node> children = new ArrayList<>();
    private List<Vector3f> preferredSizes;
    private final Vector3f lastPreferredSize;

    public ClockLayout() {
        this(FillMode.None);
    }

    public ClockLayout(FillMode fill ) {
        this.fill = fill;

        if(fill != FillMode.Even && fill != FillMode.Proportional && fill != FillMode.None) {
            throw new IllegalArgumentException("Circle layout only supports even or proportional fill mode");
        }

        if(fill != FillMode.None) {
            preferredSizes = new ArrayList<>();
            lastPreferredSize = new Vector3f();
        } else {
            lastPreferredSize = null;
        }
    }

    @Override
    public ClockLayout clone() {
        ClockLayout result = (ClockLayout)super.clone();
        result.parent = null;
//        result.children = new ArrayList<Node>();
//        result.preferredSizes = new ArrayList<Vector3f>();
//        result.lastPreferredSize = null;
        return result;
    }

    @Override
    protected void invalidate() {
        if( parent != null ) {
            parent.invalidate();
        }
    }

    public void calculatePreferredSize( Vector3f size ) {
//        reshape(null, size);
        if(fill == FillMode.None) {
//            lastPreferredSize.set(size); // Is this needed?
            reshape(null, size);
            return;
        }


        // Calculate the size we'd like to be to let
        // all of the children have space
        Vector3f pref = lastPreferredSize.set(Vector3f.ZERO);//new Vector3f();
        preferredSizes.clear();
        for( Node n : children ) {
            Vector3f v = n.getControl(GuiControl.class).getPreferredSize();

            //TODO: avoid the clone() all the time. Use a pool or reuse the vectors somehow.
            preferredSizes.add(v.clone());

            v.addLocal(pref);
//            // We do a little trickery here by adding the
//            // axis direction to the returned preferred size.
//            // That way we can just "max" the whole thing.
////            switch( axis ) {
////                case X:
//                    v.x += pref.x;
////                    break;
////                case Y:
//                    v.y += pref.y;
////                    break;
////                case Z:
//                    v.z += pref.z;
////                    break;
////            }

            pref.maxLocal(v);
        }
//        lastPreferredSize.set(pref);

        // The preferred size is the size... because layouts will always
        // be the decider in a component stack.  They are always first
        // in the component chain for preferred size and last for reshaping.
        size.set(pref);
    }

    public void reshape(Vector3f pos, Vector3f size) {
//        if(pos != null) {
//            return;
//        }
//        calculatePreferredSize(new Vector3f());

        //TODO: avoid the rotation re-calculation when not comming from recalculate. Or better, separate the rotation (set it in calculate) from the sizing (set it in reshape)
        //TODO: remember to read fine: https://github.com/jMonkeyEngine-Contributions/lemur/wiki/GUI-Components#component-layout

        float radiusX = size.x / 2;
        float radiusY = size.y / 2;

        int samples = children.size();
        float rate =  FastMath.TWO_PI / samples;
        float angle = 0;

        int i = 0;
        for(Node n : children) {
//            n.setLocalRotation(n.getLocalRotation().fromAngleAxis(angle, Vector3f.UNIT_Z));
//            n.rotate(angle, 0, 0);

            GuiControl control = n.getControl(GuiControl.class);

//            System.out.println("SIZE BEFORE IS: " + control.getPreferredSize());

//            Vector3f pref = control.getSize();

            if(fill == FillMode.None) {
                control.setSize(control.getPreferredSize());
//                System.out.println("SIZE AFTER IS: " + control.getPreferredSize());

//                Vector3f pref = control.getSize().set(preferredSizes.get(i));
//                control.setSize(pref);

            } else {
                calculatePreferredSize(lastPreferredSize);

                Vector3f pref = control.getSize().set(preferredSizes.get(i));


                switch (fill) {
                    case Even:
                        if (radiusX / radiusY != pref.x / pref.y) {
                            float x = FastMath.cos(angle) * radiusX;
                            float y = FastMath.sin(angle) * radiusY;

                            pref.x = x;
                            pref.y = y;

                            control.setSize(pref);
                            break;
                        } // Else: proportional
                    case Proportional:
//                    pref.y = radiusX / pref.x * pref.y;
//                    pref.x = radiusX;
                        pref.x = radiusY / pref.y * pref.x;
                        pref.y = radiusY;

                        control.setSize(pref);
                        break;
                }
            }



//            Vector3f offset = new Vector3f(, 0, 0);
//            n.setLocalTranslation(radiusX - control.getSize().x, -radiusY, 1); // Why y = 0???

            Quaternion q = n.getLocalRotation().fromAngleAxis(angle, Vector3f.UNIT_Z);
            n.setLocalRotation(q);

            Vector3f translation = n.getLocalTranslation().set(- control.getSize().x * n.getLocalScale().x / 2, 0, 0);
            n.setLocalTranslation(q.multLocal(translation).addLocal(radiusX, -radiusY, 1));



            angle += rate;
            i++;
        }
    }

//    public Node getChild(Vector3f position) {
//        position.angleBetween()
//        float rate =  FastMath.TWO_PI / children.size();
//
//
//    }

    public int getSamples() {
        return children.size();
    }

    public Node getSample(int sample) {
        return children.size() > sample ? children.get(sample) : null;
    }

    public <T extends Node> T addChild( T n, Object... constraints ) {
        if( n.getControl( GuiControl.class ) == null )
            throw new IllegalArgumentException( "Child is not GUI element." );
        if( constraints != null && constraints.length > 0 )
            throw new IllegalArgumentException( "Clock layout does not take constraints." );

        children.add(n);

        if( parent != null ) {
            // We are attached
            parent.getNode().attachChild(n);
        }

        invalidate();
        return n;
    }

    public void removeChild( Node n ) {
        if( !children.remove(n) )
            return; // we didn't have it as a child anyway
        if( parent != null ) {
            parent.getNode().detachChild(n);
        }
        invalidate();
    }

    @Override
    public void attach( GuiControl parent ) {
        this.parent = parent;
        Node self = parent.getNode();
        for( Node n : children ) {
            self.attachChild(n);
        }
    }

    @Override
    public void detach( GuiControl parent ) {
        this.parent = null;
        // Have to make a copy to avoid concurrent mod exceptions
        // now that the containers are smart enough to call remove
        // when detachChild() is called.  A small side-effect.
        // Possibly a better way to do this?  Disable loop-back removal
        // somehow?
        Collection<Node> copy = new ArrayList<Node>(children);
        for( Node n : copy ) {
            n.removeFromParent();
        }
    }

    public Collection<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void clearChildren() {
        if( parent != null ) {
            // Have to make a copy to avoid concurrent mod exceptions
            // now that the containers are smart enough to call remove
            // when detachChild() is called.  A small side-effect.
            // Possibly a better way to do this?  Disable loop-back removal
            // somehow?
            Collection<Node> copy = new ArrayList<Node>(children);
            for( Node n : copy ) {
                parent.getNode().detachChild(n);
            }
        }
        children.clear();
        invalidate();
    }


}

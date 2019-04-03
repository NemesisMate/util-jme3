package com.nx.util.jme3.lemur.layout;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.util.TempVars;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.core.GuiControl;

/**
 * Created by inaki on 13/01/17.
 */
public class RotationLayout extends SingleChildLayout {

    //TODO: Is this really needed?
//    private final Vector3f lastPreferredSize = new Vector3f();

    float angle;
    boolean reset;

    float rotOriginX = 50;
    float rotOriginY = -50;

//    float offsetX = 0;
//    float offsetY = 0;

    public RotationLayout() {
        this(0, false);
    }

    public RotationLayout(float angle) {
        this(angle, false);
    }

    public RotationLayout(boolean reset) {
        this(0, reset);
    }

    public RotationLayout(float angle, boolean reset) {
        this.angle = angle;
        this.reset = reset;
    }

//    @Override
//    public RotationLayout clone() {
//        RotationLayout result = (RotationLayout)super.clone();
//
//        return result;
//    }
//
//    @Override
//    protected void invalidate() {
//        if( parent != null ) {
//            parent.invalidate();
//        }
//    }

    @Override
    public void calculatePreferredSize(Vector3f size, Node child) {

//        lastPreferredSize.set(size);

        //TODO:
        if(child instanceof Panel) {
            size.set(((Panel) child).getPreferredSize());
        }

//        if(!reset) {
//            child.setLocalRotation(child.getLocalRotation().fromAngleAxis(angle, Vector3f.UNIT_Z));
//        } else {
//            LoggerFactory.getLogger(this.getClass()).debug("OK, THE QUAT IS: {}, L: {}", child.getWorldRotation(), child.getLocalRotation());
//            LoggerFactory.getLogger(this.getClass()).debug("OK, THE Trans IS: {}, L: {}", child.getWorldTranslation(), child.getLocalTranslation());
//
//            child.setLocalRotation(Quaternion.IDENTITY);
//            Quaternion rot = child.getLocalRotation().set(child.getWorldRotation()).inverseLocal();
//
////            Quaternion q = new Quaternion();
////            q.set(child.getWorldRotation());
////            q.inverseLocal();
//
////            Quaternion rot = child.getWorldRotation().mult(q);
//
////            LoggerFactory.getLogger(this.getClass()).debug("OK, THE QUAT IS: {}, L: {}", child.getWorldRotation(), child.getLocalRotation());
////            LoggerFactory.getLogger(this.getClass()).debug("OK, THE Trans IS: {}, L: {}", child.getWorldTranslation(), child.getLocalTranslation());
//
////            Quaternion rot = child.getLocalRotation().multLocal(child.getWorldRotation().inverse());//.inverseLocal();
//
//            if(angle != 0) {
//                TempVars vars = TempVars.get();
//
//                vars.quat1.fromAngleAxis(angle, Vector3f.UNIT_Z);
//                rot.multLocal(vars.quat1);
//
//                vars.release();
//            }
//
//            child.setLocalRotation(rot);
////            child.setLocalRotation(new Quaternion().fromAngleAxis(90 * FastMath.DEG_TO_RAD, Vector3f.UNIT_Z));
//
////            Quaternion q = .fromAngleAxis(angle, Vector3f.UNIT_Z);
////            n.setLocalRotation(q);
//
////            Vector3f translation = n.getLocalTranslation().set(- control.getSize().x / 2, 0, 0);
////            n.setLocalTranslation(q.multLocal(translation).addLocal(radiusX, -radiusY, 1));
//        }
    }

    @Override
    public void reshape(Vector3f pos, Vector3f size, Node child) {
//        calculatePreferredSize(size, child);

        //TODO: REMOVE
//        angle = FastMath.HALF_PI;
        //////////////////////

        if(!reset) {
            child.setLocalRotation(child.getLocalRotation().fromAngleAxis(angle, Vector3f.UNIT_Z));
        } else {
//            LoggerFactory.getLogger(this.getClass()).debug("OK, THE QUAT IS: {}, L: {}", child.getWorldRotation(), child.getLocalRotation());
//            LoggerFactory.getLogger(this.getClass()).debug("OK, THE Trans IS: {}, L: {}", child.getWorldTranslation(), child.getLocalTranslation());

            child.setLocalRotation(Quaternion.IDENTITY);
            Quaternion rot = child.getLocalRotation().set(child.getWorldRotation()).inverseLocal();

//            Quaternion q = new Quaternion();
//            q.set(child.getWorldRotation());
//            q.inverseLocal();

//            Quaternion rot = child.getWorldRotation().mult(q);



//            Quaternion rot = child.getLocalRotation().multLocal(child.getWorldRotation().inverse());//.inverseLocal();

            if(angle != 0) {
                TempVars vars = TempVars.get();

                vars.quat1.fromAngleAxis(angle, Vector3f.UNIT_Z);
                rot.multLocal(vars.quat1);

                vars.release();
            }

            child.setLocalRotation(rot);
//            child.setLocalRotation(new Quaternion().fromAngleAxis(90 * FastMath.DEG_TO_RAD, Vector3f.UNIT_Z));

//            Quaternion q = .fromAngleAxis(angle, Vector3f.UNIT_Z);
//            n.setLocalRotation(q);

//            Vector3f translation = n.getLocalTranslation().set(- control.getSize().x / 2, 0, 0);
//            n.setLocalTranslation(q.multLocal(translation).addLocal(radiusX, -radiusY, 1));
        }

        if(rotOriginX != 0 || rotOriginY != 0) {
            float origX = -rotOriginX * size.x / 100;
            float origY = -rotOriginY * size.y / 100;

            Vector3f translation = child.getLocalTranslation().set(origX, origY, 0);
            child.getLocalRotation().multLocal(translation).subtractLocal(origX, origY, 0);//.addLocal(pos);

//            if(offsetX != 0 || offsetY != 0) {
//                float posX = offsetX * size.x / 100;
//                float posY = offsetY * size.y / 100;
//
//                translation.addLocal(posX, posY, 0);
//            }

            child.setLocalTranslation(translation);

        } else {
            child.setLocalTranslation(pos);
        }



        child.getControl(GuiControl.class).setSize(size);
    }
}

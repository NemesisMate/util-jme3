package com.nx.util.jme3.base;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import org.slf4j.LoggerFactory;

public abstract class ScenegraphListenerControl extends AbstractControl {

    protected ScenegraphAutoRemoverChecker checker;
    protected Spatial rootParent;

    public abstract void onAttached();
    public abstract void onDetached();

    public final void detach() {
        // Get sure that the checker is removed (eg: externally called)
        removeChecker();
        onDetached();

        checker = null;
    }

    public boolean isAttached() {
        return checker != null;
    }

    public Node getRootNode() {
        return checker != null ? (Node) checker.getSpatial() : null;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(checker != null) {
            checker.check();
            return;
        }


        rootParent = SpatialUtil.getRootFor(spatial);

        if (rootParent != null) {
//            ViewportPanel.this.rootNode = rootParent;
            checker = createChecker();
            rootParent.addControl(checker);
            onAttached();
        }
    }

    protected ScenegraphAutoRemoverChecker createChecker() {
        return new ScenegraphAutoRemoverChecker();
    }

    @Override
    public final void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        // If the control is removed because it is unwanted
        if(this.spatial == null && checker != null) {
            removeChecker();
        }
    }

    private void removeChecker() {
        // To avoid the warning
        checker.flagged = true;

        Spatial checkerSpatial = checker.getSpatial();
        if(checkerSpatial != null) {
            checkerSpatial.removeControl(checker);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public class ScenegraphAutoRemoverChecker extends AbstractControl {

        boolean flagged;

        public void check() {
            flagged = false;
        }

//            public abstract void onClose();

        @Override
        public void setSpatial(Spatial spatial) {
            if(spatial == null) {
                if(!flagged) {
                    LoggerFactory.getLogger(this.getClass()).warn("Shouldn't be removing this control manually!");
//                    this.spatial.addControl(this); // Re-add??, or just better let the developer see the problem.
                }
                // Not closing here because of this way the developer sees if there is something wrong.
//                    close();
            }

            super.setSpatial(spatial);
        }

        @Override
        protected void controlUpdate(float tpf) {
            if(flagged) {
                detach();
            } else {
                flagged = true;
            }
        }

        public final void detach() {
            ScenegraphListenerControl.this.detach();

            // Actually the SpatialAutoManager.detach() already does this removal
//            spatial.removeControl(this);
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) { }
    }
}
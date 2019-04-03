package com.nx.util.jme3.debug;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.*;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.TangentBinormalGenerator;
import com.nx.util.jme3.base.DebugUtil;
import com.nx.util.jme3.base.SpatialUtil;

/**
 * Created by NemesisMate on 1/12/16.
 */
public class DebugNormalsState extends AbstractThreadedDebugGraphStateModule {

    boolean useArrows;
    VertexBuffer.Type type;

    public DebugNormalsState(boolean useArrows, boolean tangents) {
        //FIXME: doesn't work because geometries inside hasn't any normals.
        super(false);
        this.useArrows = useArrows;

        type = tangents ? VertexBuffer.Type.Tangent : VertexBuffer.Type.Normal;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        getDebugsNode().setCullHint(Spatial.CullHint.Never);
    }

    @Override
    protected void threadCall() {

    }

    @Override
    protected boolean threadCall(Spatial spatial) {
        if(spatial instanceof Node) {
            return false;
        }

        final Geometry geometry = (Geometry) spatial;
        if(debugContains(geometry)) {
            return true;
        }

        if(geometry.getMesh().getBuffer(type) == null) {
            return true;
        }


        Mesh normalsMesh;

        if(useArrows) {
            normalsMesh = DebugUtil.createNormalArrows(geometry, null, .3f, useTangents());
        } else {
            if(useTangents()) {
                normalsMesh = TangentBinormalGenerator.genTbnLines(geometry.getMesh(), .3f);
            } else {
                normalsMesh = TangentBinormalGenerator.genNormalLines(geometry.getMesh(), .3f);
            }
        }

        Geometry debugArrowGeom = new Geometry("Normals for: " + geometry.getName(), normalsMesh);
        debugArrowGeom.addControl(new AbstractControl() {
            @Override
            protected void controlUpdate(float tpf) {
                if(geometry.getParent() == null) {
                    removeFromDebug(geometry);
                }

                this.spatial.setLocalTransform(geometry.getWorldTransform());
            }

            @Override
            protected void controlRender(RenderManager rm, ViewPort vp) {

            }
        });


        debugArrowGeom.setMaterial(SpatialUtil.createMaterial(getAssetManager(), null));
//        debugArrowGeom.setLocalTranslation(geometry.getWorldTranslation());



        addToDebug(geometry, debugArrowGeom);

        return true;
    }

    private boolean useTangents() {
        return type == VertexBuffer.Type.Tangent;
    }

}

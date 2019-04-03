package com.nx.util.jme3.debug;

import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.debug.SkeletonDebugger;
import com.nx.util.jme3.base.SpatialUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NemesisMate on 1/12/16.
 */
public class DebugSkeletonState extends AbstractThreadedDebugGraphStateModule {

    //TODO: Show different if the skeleton control is enabled or disabled.

    private final String SKELETON_NAME = "-SKLTNDBGGR-";

//    public void debugSkeletons() {
//        Collection<SkeletonControl> skeletonControlList = SpatialUtil.getControlsFor(selected, SkeletonControl.class);
//        for (SkeletonControl skeletonControl : skeletonControlList) {
//
//        }
//    }


//    @Override
//    protected Node createDebugsNode() {
//        return new BatchNode();
//    }

//    @Override
//    public void cleanup() {
//        super.cleanup();
//
//        selected.depthFirstTraversal(new SceneGraphVisitor() {
//            @Override
//            public void visit(Spatial task) {
//                if(SKELETON_NAME.equals(task.getName())) {
//                    task.removeFromParent();
//                }
//            }
//        });
//    }

//    Map<Spatial, Collection<SkeletonControl>> cachedMap;
    Set<SkeletonControl> cachedControls;

//    @Override
//    protected Node createDebugsNode() {
//    //FIXME: No normals info.
//        return new BatchNode();
//    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

//        cachedMap = new HashMap<>();
        cachedControls = Collections.newSetFromMap(new ConcurrentHashMap<SkeletonControl, Boolean>());
    }

    @Override
    protected void threadCall() {
        // Removing unused
//        for(Iterator<Map.Entry<Spatial, Collection<SkeletonControl>>> entryIter = cachedMap.entrySet().iterator(); entryIter.hasNext(); ) {
//            Map.Entry<Spatial, Collection<SkeletonControl>> entry = entryIter.next();
//
//            Spatial spat = entry.getKey();
//            Collection<SkeletonControl> skeletonControls = entry.getValue();
//
//
//            boolean parentNull = spat.getParent() == null;
//            for(Iterator<SkeletonControl> iter = skeletonControls.iterator(); iter.hasNext(); ) {
//                SkeletonControl skeletonControl = iter.next();
//
//                if(parentNull || !SpatialUtil.hasControl(spat, skeletonControl)) {
//                    removeFromDebug(spat);
//                    iter.remove();
//                }
//            }
//
//            if(skeletonControls.isEmpty()) {
//                entryIter.remove();
//
//            }
//        }
    }

    @Override
    protected boolean threadCall(Spatial spatial) {

        // Checking for new
        Collection<SkeletonControl> skeletonControls = SpatialUtil.getControlsNoRecursive(spatial, SkeletonControl.class);

        if(skeletonControls != null && !skeletonControls.isEmpty()) {

//            cachedMap.put(task, skeletonControls);

            for(final SkeletonControl skeletonControl : skeletonControls) {
                if(cachedControls.contains(skeletonControl)) {
                    continue;
                }

                Skeleton skeleton = skeletonControl.getSkeleton();



                final SkeletonDebugger skeletonDebugger = new SkeletonDebugger(SKELETON_NAME, skeleton);

                skeletonDebugger.depthFirstTraversal(new SceneGraphVisitor() {
                    @Override
                    public void visit(Spatial spatial) {
                        if (spatial instanceof Geometry) {
                            Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                            mat.setName("Skeleton-Debug_material");
                            mat.setColor("Color", ColorRGBA.randomColor());
                            mat.getAdditionalRenderState().setDepthTest(false);
                            mat.getAdditionalRenderState().setDepthWrite(false);

                            spatial.setMaterial(mat);
                        }
                    }
                });

                skeletonDebugger.setQueueBucket(RenderQueue.Bucket.Translucent);
                skeletonDebugger.setCullHint(Spatial.CullHint.Never);

                final Spatial originalSpatial = spatial;
                skeletonDebugger.addControl(new AbstractControl() {
                    @Override
                    protected void controlUpdate(float tpf) {
                        if(!SpatialUtil.hasControl(originalSpatial, skeletonControl)) {
                            cachedControls.remove(skeletonControl);
                            removeFromDebug(originalSpatial, skeletonDebugger);
                            return;
                        }
                        //                            LoggerFactory.getLogger(this.getClass()).debug("Updating this: {}, with: {}.", skeletonDebugger.hashCode(), originalSpatial);



                        // Very important when batching.
                        if(!skeletonDebugger.getLocalTransform().equals(originalSpatial.getWorldTransform())) {
                            skeletonDebugger.setLocalTransform(originalSpatial.getWorldTransform());
                        }
                    }

                    @Override
                    protected void controlRender(RenderManager rm, ViewPort vp) {

                    }
                });

                addToDebug(spatial, skeletonDebugger);
                cachedControls.add(skeletonControl);
            }

//            LoggerFactory.getLogger(this.getClass()).info(MarkerFactory.getMarker("CONSOLE"), "Found skeleton with {} bones for: {}.", skeletonControl.getSkeleton().getBoneCount(), task);
        }

        return false;
    }
}

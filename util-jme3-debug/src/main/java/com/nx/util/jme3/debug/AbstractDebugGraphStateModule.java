package com.nx.util.jme3.debug;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.nx.util.jme3.base.SpatialUtil;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by NemesisMate on 5/12/16.
 */
public abstract class AbstractDebugGraphStateModule extends AbstractDebugStateModule<Spatial> {

//    SceneGraphVisitor removeChildsFromDebug = new SceneGraphVisitor() {
//        @Override
//        public void visit(Spatial task) {
//            removeFromDebug(task);
//        }
//    };


    public AbstractDebugGraphStateModule() {
    }

    public AbstractDebugGraphStateModule(boolean defaultBatch) {
        super(defaultBatch);
    }

    private Future future;
    private Callable<Void> callable = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            for(final Spatial object : debugsCache.keySet()) {
                //TODO: make an AbstractDebugGraphStateModule. Currently this is incompatible with navmesh check (for that can add a boolean saying if check or not)
//                if(object instanceof Spatial) {
                    Node parent = object.getParent();
                    if(parent == null) {
                        continue;
                    }

                    while(parent.getParent() != null) {
                        parent = parent.getParent();
                    }

                    if(parent != getRootNode()) {
                        app.enqueue(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                removeFromDebug(object);
                                return null;
                            }
                        });
//                        removeFromDebug(object);
                    }
//                }
            }

            updateDebugsThreaded();

            return null;
        }
    };

    protected final void updateDebugs(float tpf) {

        if (future == null || future.isDone()) {
//            checkBatch();

            if(future != null) {
                try {
                    future.get();
                } catch (Exception e) {
                    // Shouldn't. The debugs should never crash the app.
                    //                throw new InternalError("An error has ocoured while processing thread for: " + this);
                    LoggerFactory.getLogger(this.getClass()).error("An error has ocoured while processing thread for: {}. Error: {}.", this, e.getCause());
                    e.printStackTrace();
                    e.getCause().printStackTrace();
                }
            }



            future = pool.submit(callable);
        }


        updateSceneGraph(tpf);
    }

    protected void updateSceneGraph(final float tpf) {
        final Node rootNode = getRootNode();

        SpatialUtil.visitNodeWith(debugSpatial, new SpatialUtil.Operation() {
            @Override
            public boolean operate(Spatial spatial) {
//                                LoggerFactory.getLogger(this.getClass()).debug("Operate future task: {}.", task);
                if(isDebugNode(spatial)) {
                    return true;
                }

                if(spatial == rootNode) {
                    return false;
                }
//                            if (task == debugsNode) {
//                                return true;
//                            }

//                            if (cached.contains(task)) {
//                                return false;
//                            }



                return update(spatial, tpf);
            }
        });
    }


    @Override
    protected void removeFromDebug(Spatial original) {
        if(original instanceof Node) {
            for(Spatial spatial : ((Node)original).getChildren()) {
                removeFromDebug(spatial);//task.depthFirstTraversal(removeChildsFromDebug);
            }
        }

        super.removeFromDebug(original);
    }

    protected abstract boolean update(Spatial spatial, float tpf);

    protected void updateDebugsThreaded() {

    }
}

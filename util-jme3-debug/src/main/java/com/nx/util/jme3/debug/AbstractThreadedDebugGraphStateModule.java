package com.nx.util.jme3.debug;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.nx.util.jme3.base.SpatialUtil;

import java.util.concurrent.Callable;

/**
 * Created by NemesisMate on 1/12/16.
 */
public abstract class AbstractThreadedDebugGraphStateModule extends AbstractDebugGraphStateModule {

//    private Future future;
//
//    private Callable<Void> callable = new Callable<Void>() {
//        @Override
//        public Void call() throws Exception {
//
//        }
//    };

    public AbstractThreadedDebugGraphStateModule() {
        this(false);
    }

    public AbstractThreadedDebugGraphStateModule(boolean defaultBatch) {
        super(defaultBatch);

//        disableBatch = true;
    }

    @Override
    protected void addToDebug(final Spatial original, final Spatial debug) {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AbstractThreadedDebugGraphStateModule.super.addToDebug(original, debug);
                return null;
            }
        });
    }

    @Override
    protected void removeFromDebug(final Spatial original) {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AbstractThreadedDebugGraphStateModule.super.removeFromDebug(original);
                return null;
            }
        });
    }

    @Override
    protected void addControl(final Spatial spatial, final Control control) {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AbstractThreadedDebugGraphStateModule.super.addControl(spatial, control);
                return null;
            }
        });
    }

//    @Override
//    public void updateDebugs(float tpf) {
////        super.updateDebugs(tpf);
//
//        if (future == null || future.isDone()) {
////            checkBatch();
//
//            if(future != null) {
//                try {
//                    future.get();
//                } catch (Exception e) {
//                    // Shouldn't. The debugs should never crash the app.
//                    //                throw new InternalError("An error has ocoured while processing thread for: " + this);
//                    LoggerFactory.getLogger(this.getClass()).error("An error has ocoured while processing thread for: {}. Error: {}.", this, e.getCause());
//                    e.printStackTrace();
//                    e.getCause().printStackTrace();
//                }
//            }
//
//
//
//            future = pool.submit(callable);
//        }
//    }

    @Override
    protected final boolean update(Spatial spatial, float tpf) {
        return false;
    }

    @Override
    protected void updateSceneGraph(float tpf) { }

    @Override
    protected final void updateDebugsThreaded() {
        SpatialUtil.visitNodeWith(debugSpatial, new SpatialUtil.Operation() {
            @Override
            public boolean operate(Spatial spatial) {
//                                LoggerFactory.getLogger(this.getClass()).debug("Operate future task: {}.", task);
                if(isDebugNode(spatial)) {
                    return true;
                }

                if(spatial == getRootNode()) {
                    return false;
                }

//                            if (task == debugsNode) {
//                                return true;
//                            }

//                            if (cached.contains(task)) {
//                                return false;
//                            }



                return threadCall(spatial);
            }
        });

        for(Spatial object : debugsCache.keySet()) {
            //TODO: make an AbstractDebugGraphStateModule. Currently this is incompatible with navmesh check (for that can add a boolean saying if check or not)
//            if(object instanceof Spatial) {
                Node parent = object.getParent();
                if(parent == null) {
                    continue;
                }

                while(parent.getParent() != null) {
                    parent = parent.getParent();
                }

                if(parent != getRootNode()) {
                    removeFromDebug(object);
                }
//            }
        }


        threadCall();
    }

    protected abstract void threadCall();

    /**
     *
     * @param spatial
     * @return true if it must stop deeping the scene graph by the given task.
     */
    protected abstract boolean threadCall(Spatial spatial);

}

package com.nx.util.jme3.debug;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.*;
import com.jme3.scene.control.Control;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by NemesisMate on 1/12/16.
 */
public abstract class AbstractDebugStateModule<T> extends AbstractAppState {

    public static final String DEBUG_NODES_NAME = "-_\\Debug||Node/_-";
    private int BATCH_INTERVAL = 1;

    protected Application app;
    protected ThreadPoolExecutor pool;

    private Node rootNode;
    protected Spatial debugSpatial;
    private AssetManager assetManager;

    private Node debugsNode;
    protected Map<T, List<Spatial>> debugsCache;
//    private Map<Spatial, Spatial> parentingRelation;

    @Deprecated
    protected Collection<Spatial> cached;

    boolean disableBatch;
    boolean forceBatch;
    int batchNeeded;

    boolean defaultBatch;


    public AbstractDebugStateModule() {
        this(false);
    }

    public AbstractDebugStateModule(boolean defaultBatch) {
        this.debugsNode = createDebugsNode();
        this.defaultBatch = defaultBatch;
    }


    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        DebugStateProperties debugState = stateManager.getState(DebugStateProperties.class);


        if(debugsNode == null) {
            if (debugState.isUseBatching() && defaultBatch) {
                this.debugsNode = new SimpleBatchNode();
            } else {
                this.debugsNode = new Node();
            }
        }

        this.app = app;
        this.pool = debugState.getPool();
        this.rootNode = debugState.getRootNode();
        this.assetManager = debugState.getAssetManager();
        this.debugSpatial = debugState.getDebugSpatial();

//        this.debugsNode = createDebugsNode();
        this.debugsNode.setName(DEBUG_NODES_NAME);

        this.debugsCache = new ConcurrentHashMap<>();

        this.cached = createCache();
        this.rootNode.attachChild(debugsNode);
    }

    @Override
    public void cleanup() {
        super.cleanup();

//        pool.shutdown();
        this.debugsNode.removeFromParent();
    }

    @Override
    public final void update(float tpf) {
        super.update(tpf);

        updateDebugs(tpf);

        checkBatch();
    }



    protected abstract void updateDebugs(float tpf);



    public boolean debugContains(T object) {
        return debugsCache.containsKey(object);
    }

    @Deprecated
    public boolean cacheContains(Spatial object) {
        return cached.contains(object);
    }

    @Deprecated
    protected boolean addToCache(Spatial object) {
        return cached.add(object);
    }

    @Deprecated
    protected void removeFromCache(Spatial object) {
        cached.remove(object);
    }

    @Deprecated
    protected Collection<Spatial> createCache() {
        return new HashSet<>();
    }

    protected Node createDebugsNode() {
        return null;
    }

//    protected void addToDebug(T object, final Spatial[] spatials) {
//        for(Spatial task : spatials) {
//            debugsCache.put(object, task);
//        }
//
//        app.enqueue(new Callable<Void>() {
//            @Override
//            public Void call() throws Exception {
//                for(Spatial task : spatials) {
//                    debugsNode.attachChild(task);
//                }
//
////                if(debugsNode instanceof BatchNode) {
////                    ((BatchNode) debugsNode).batch();
////                }
//                return null;
//            }
//        });
//
//    }

    protected void addToDebug(T original, final Spatial debug) {
        List<Spatial> list = debugsCache.get(original);
        if(list == null) {
            list = new ArrayList<>(1);

            debugsCache.put(original, list);
        }

        list.add(debug);

//        debugsCache.put(original, debug);

//        app.enqueue(new Callable<Void>() {
//            @Override
//            public Void call() throws Exception {
                debugsNode.attachChild(debug);


        LoggerFactory.getLogger(AbstractDebugStateModule.class).debug("Marked to batch needed: {} - {}", original, debug);
                batchNeeded++;
//                checkBatch();

//                return null;
//            }
//        });

    }

    private void checkBatch() {
        if(disableBatch) {
            return;
        }

        if (batchNeeded >= BATCH_INTERVAL) {
            applyBatch();
        }
    }

    private void applyBatch() {
        if(batchNeeded > 0) {
            if (debugsNode instanceof BatchNode) {
                ((BatchNode) debugsNode).batch();
                LoggerFactory.getLogger(AbstractDebugStateModule.class).debug("BATCHING: {}", batchNeeded);

                forceBatch = false;
                batchNeeded = 0;
            }
        }
    }



    protected void removeFromDebug(T original, Spatial debug) {
        final List<Spatial> list = debugsCache.get(original);
        if(list.remove(debug)) {
            removeDebug(debug);

            if(list.isEmpty()) {
                debugsCache.remove(original);
            }
        }

//        removeFromDebug();
//
//        if(toRemove != null) {
//
//
////            app.enqueue(new Callable<Void>() {
////                @Override
////                public Void call() throws Exception {
//
//
//            forceBatch = true;
////                    return null;
////                }
////            });
//        }
    }

    private void removeDebug(Spatial debug) {
        debugsNode.detachChild(debug);
        forceBatch = true;
    }

    protected void removeFromDebug(T original) {
        final List<Spatial> list = debugsCache.remove(original);

        if(list != null) {
            for (Spatial debug : list) {
                removeDebug(debug);
            }
        }
    }

    protected void addControl(Spatial spatial, Control control) {
        spatial.addControl(control);
    }

    protected Node getRootNode() {
        return rootNode;
    }

    protected boolean isDebugNode(Spatial node) {
        return node instanceof Node && node.getName() != null && node.getName().equals(DEBUG_NODES_NAME);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public Node getDebugsNode() {
        return debugsNode;
    }

    public void changeDebugMaterial(Geometry geometry, Material material) {
        if(geometry.getMaterial() != material) {
            if(debugsNode instanceof BatchNode) {
                Node parent = geometry.getParent();
                geometry.removeFromParent();

                geometry.setMaterial(material);

                parent.attachChild(geometry);
            } else {
                geometry.setMaterial(material);
            }
        }
    }
}

package com.nx.util.jme3.debug;

import com.jme3.app.Application;
import com.jme3.app.LegacyApplication;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by NemesisMate on 1/12/16.
 */
public class DebugStateProperties extends AbstractAppState {

    public LegacyApplication app;

    public AssetManager assetManager;
    private ThreadPoolExecutor pool;
    private Node rootNode;
    private Spatial debugSpatial;

    private boolean useBatching = false;

    public DebugStateProperties() {
    }

    public DebugStateProperties(Spatial debugSpatial) {
        this.debugSpatial = debugSpatial;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

//        LoggerFactory.getLogger(DebugUtil.class).info("Initting debugs for app: {}, with selected: {}.", app, selected);

        pool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() - 1);
        rootNode = ((SimpleApplication)app).getRootNode();
        assetManager = app.getAssetManager();
    }


    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public ThreadPoolExecutor getPool() {
        return pool;
    }

    @Override
    public void cleanup() {
        pool.shutdown();
    }

    public void setPool(ThreadPoolExecutor pool) {
        this.pool = pool;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    public Spatial getDebugSpatial() {
        return debugSpatial != null ? debugSpatial : rootNode;
    }

    public void setDebugSpatial(Spatial debugSpatial) {
        this.debugSpatial = debugSpatial;
    }

    public boolean isUseBatching() {
        return useBatching;
    }

    public void setUseBatching(boolean useBatching) {
        this.useBatching = useBatching;
    }


    public static void checkDebugState(LegacyApplication app, Spatial debugSpatial, boolean debug) {
        DebugStateProperties debugStateProperties = app.getStateManager().getState(DebugStateProperties.class);

        if(debug) {
            if (debugStateProperties == null) {
                app.getStateManager().attach(new DebugStateProperties(debugSpatial));
            } else {
                debugStateProperties.setDebugSpatial(debugSpatial);
            }
        } else {
            if (debugStateProperties != null) {
                if(app.getStateManager().getState(AbstractDebugStateModule.class) == null) {
                    app.getStateManager().detach(debugStateProperties);
                }
            }
        }
    }
}

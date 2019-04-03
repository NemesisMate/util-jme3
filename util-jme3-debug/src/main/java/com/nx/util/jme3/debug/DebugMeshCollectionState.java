package com.nx.util.jme3.debug;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by NemesisMate on 1/12/16.
 */
public class DebugMeshCollectionState extends AbstractThreadedDebugStateModule {
    private boolean colors;
    private boolean wire;
    private boolean normals;
    private boolean cullOff;
    private boolean neverCull;

//    Map<Object, Geometry> cachedGeometries = new HashMap<>();
    Collection collection;
    int size;

    Map<Object, Boolean> aux;

    public DebugMeshCollectionState(Collection meshesOrGeometries, boolean colors, boolean wire, boolean normals, boolean cullOff, boolean neverCull) {
        this.collection = meshesOrGeometries;

        this.colors = colors;
        this.wire = wire;
        this.normals = normals;
        this.cullOff = cullOff;
        this.neverCull = neverCull;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.aux = new HashMap<>();
//        this.size = this.collection.size();

        if(neverCull) {
            getDebugsNode().setCullHint(Spatial.CullHint.Never);
        }
    }

    private Future future;
    private Callable<Void> callable = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            //TODO: now adding when collection has new objects, but need to remove when it loose objects too.




            for(Map.Entry<Object, Boolean> entry : aux.entrySet()) {
                entry.setValue(Boolean.FALSE);
            }

            for(Object object : collection) {
                aux.put(object, Boolean.TRUE);

                if(debugContains(object)) {
                    continue;
                }

                Geometry geometry;
                if(object instanceof Geometry) {
                    // This confirmation should be done?, what about debugging things on another viewport?. Maybe is the desired behavior.
//                        if(((Geometry)object).getParent() != null) {
//                            new UnsupportedOperationException("Shouldn't be trying to debug a geometry in scene with this debugger.").printStackTrace();
//                            removeFromCache(object);
//                            continue;
//                        }

                    geometry = ((Geometry) object).clone();
                } else {
                    geometry = new Geometry("debug_geom", (Mesh) object);
                }


//                cachedGeometries.put(object, geometry);

                geometry.setMaterial(DebugMeshState.getDebugMaterial(geometry, colors, wire, normals, cullOff));

//                if(neverCull) {
//                    geometry.setCullHint(Spatial.CullHint.Never);
//                }

                addToDebug(object, geometry);
            }

            for(Iterator<Map.Entry<Object, Boolean>> entryIter =  aux.entrySet().iterator(); entryIter.hasNext(); ) {
                Map.Entry<Object, Boolean> entry = entryIter.next();
                // It implies that the mesh is removed from the collection
                if(!entry.getValue()) {
                    removeFromDebug(entry.getKey());
                    entryIter.remove();
                }
            }

            return null;
        }
    };

    @Override
    public void updateDebugs(float tpf) {
//        super.updateDebugs(tpf);

        if(size == collection.size()) {
            return;
        }

        size = collection.size();






        if (future == null || future.isDone()) {
            future = pool.submit(callable);
        }
    }
}

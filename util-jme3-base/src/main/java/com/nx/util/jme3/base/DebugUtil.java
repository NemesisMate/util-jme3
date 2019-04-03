package com.nx.util.jme3.base;

import com.jme3.animation.SkeletonControl;
import com.jme3.app.Application;
import com.jme3.app.LegacyApplication;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;
import jme3tools.optimize.GeometryBatchFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 * @author NemesisMate
 */
public class DebugUtil extends AbstractAppState {

    private static final Logger log = LoggerFactory.getLogger(DebugUtil.class);

    //TODO: make completely non-static and fetch it with stateManager.getState();

    public static LegacyApplication app;

    public static BatchNode batchNode;
    public static AssetManager assetManager;

    private static Map<LegacyApplication, DebugUtil> enabledDebugs = new HashMap<>();

//    private static ThreadPoolExecutor pool;

    private static Node rootNode;


    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        initDebugs((LegacyApplication) app, ((SimpleApplication)app).getRootNode(), app.getAssetManager());
    }

    public static void initDebugs(LegacyApplication app, Node rootNode, AssetManager assetManager) {
        log.info("Initting debugs for app: {}, with rootNode: {}.", app, rootNode);

        if(app != null) {
            DebugUtil.app = app;
        }

        if(rootNode != null) {
            setRootNode(rootNode);
        }

        if(assetManager != null) {
            DebugUtil.assetManager = assetManager;
        }

//        pool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() - 1);
        DebugUtil debugInstance = enabledDebugs.get(app);
        if(debugInstance == null) {
            debugInstance = new DebugUtil();

            enabledDebugs.put(app, debugInstance);
            app.getStateManager().attach(debugInstance);
//            rootNode.attachChild(debugInstance.debugsNode);
//            batchNode.attachChild(debugInstance.debugsNode);

            log.info("Debugs initted for app: {}, with rootNode: {}.", app, rootNode);
        } else {
            log.info("Debugs already initted for app: {}, with rootNode: {}.", app, rootNode);
        }



    }

    public static boolean isDebugging() {
        return assetManager != null;
    }

//    @Override
//    public void update(float tpf) {
//        super.update(tpf);
//
//        if(bounds) {
////            LoggerFactory.getLogger(this.getClass()).debug("Bounds enabled.");
//
//            Iterator<Map.Entry<Spatial, Geometry>> it = debuggedBounds.entrySet().iterator();
//            while(it.hasNext()) {
//
//
//                Map.Entry<Spatial, Geometry> entry = it.next();
//
//                Spatial key = entry.getKey();
//                Geometry value = entry.getValue();
//
////                LoggerFactory.getLogger(this.getClass()).debug("Debugging bound for node: {}.", key);
//
//                if(key.getParent() == null) {
//                    value.removeFromParent();
//                    it.remove();
//                } else {
//                    if(value.getParent() == null) {
//                        if(value.getMaterial() == null) {
//                            value.setMaterial(key instanceof Geometry ? debugsMaterial : nodesMaterial);
//                        }
//                        debugsNode.attachChild(value);
////                        LoggerFactory.getLogger(this.getClass()).debug("Debug attached: {}, to node: {}.", value, debugsNode);
//
//                    }
//
//
//                    BoundingVolume bv = key.getWorldBound();
//                    BoundingVolume bv2 = value.getWorldBound();
//
//                    Vector3f localScale = value.getLocalScale();
//                    if(bv != null) {
//                        Vector3f newLocation = bv.getCenter().subtract(key.getWorldTranslation(), aux).addLocal(key.getWorldTranslation());
//                        if(!value.getLocalTranslation().equals(newLocation)) {
//                            value.setLocalTranslation(newLocation);
//                        }
////                        if(!value.getLocalRotation().equals(key.getWorldRotation())) {
////                            value.setLocalRotation(key.getWorldRotation());
////                        }
//
//                        if(key instanceof Node) {
////                            if(((Node) key).getQuantity() == 0) {
////                                if(!value.getLocalRotation().equals(key.getWorldRotation())) {
////                                    value.setLocalRotation(key.getWorldRotation());
////                                }
////                            }
////                            else {
//                                if (!nodeBounds) {
//                                    value.removeFromParent();
//                                    it.remove();
//
//                                    return;
//                                }
////                            }
//                        }
//
//
//                        if (bv instanceof BoundingBox) {
//                            BoundingBox bb = (BoundingBox) bv;
//                            BoundingBox bb2 = (BoundingBox) bv2;
//
//                            if (bb.getXExtent() != bb2.getXExtent()) {
//                                value.setLocalScale(localScale.setX(localScale.getX() + (bb.getXExtent() - bb2.getXExtent())));
//                            }
//                            if (bb.getYExtent() != bb2.getYExtent()) {
//                                value.setLocalScale(localScale.setY(localScale.getY() + (bb.getYExtent() - bb2.getYExtent())));
//                            }
//                            if (bb.getZExtent() != bb2.getZExtent()) {
//                                value.setLocalScale(localScale.setZ(localScale.getZ() + (bb.getZExtent() - bb2.getZExtent())));
//                            }
//                        } else if (bv instanceof BoundingSphere) {
//                            BoundingSphere bs = (BoundingSphere) bv;
//                            BoundingBox bb2 = (BoundingBox) bv2;
//
//                            if (bs.getRadius() != bb2.getXExtent()) {
//                                float scaleValue = bs.getRadius() - bb2.getXExtent();
//                                value.setLocalScale(localScale.addLocal(scaleValue, scaleValue, scaleValue));
//                            }
//                        } else {
//                            LoggerFactory.getLogger(this.getClass()).warn("Bounding volume type not supported: {}.", bv.getClass());
//                        }
//                    } else {
//                        BoundingBox bb2 = (BoundingBox) bv2;
//                        if(bb2.getXExtent() != key.getWorldScale().length()) {
//                            float scaleValue = key.getWorldScale().getX() - bb2.getXExtent();
//                            value.setLocalScale(localScale.addLocal(scaleValue, scaleValue, scaleValue));
//                        }
//                    }
//
//                }
//            }
//
//            //TODO: add a future per each thread on the pool
//            if(boundsFuture == null || boundsFuture.isDone()) {
////                LoggerFactory.getLogger(this.getClass()).debug("Submitting future 0.");
//                boundsFuture = pool.submit(new Callable<Void>() {
//                    @Override
//                    public Void call() {
//
////                        LoggerFactory.getLogger(this.getClass()).debug("Submitting future 1.");
//                        SpatialUtil.visitNodeWith(rootNode, new SpatialUtil.Operation() {
//                            @Override
//                            public boolean operate(Spatial task) {
////                                LoggerFactory.getLogger(this.getClass()).debug("Operate future task: {}.", task);
//                                if(debuggedBounds.containsKey(task)) {
//                                    return false;
//                                }
//
//
//                                if(task instanceof Node) {
//                                    if(task == debugsNode) {
//                                        return true;
//                                    }
////                                    String spatialName = task.getName();
////                                    if (spatialName != null && spatialName.equals(DEBUG_BOUNDS_NAME)) {
////                                        return true;
////                                    }
//
//                                    if (((Node) task).getQuantity() == 0) {
//                                        Vector3f worldScale = null;
//                                        try {
//                                            worldScale = app.enqueue(new Callable<Vector3f>() {
//                                                @Override
//                                                public Vector3f call() throws Exception {
//                                                    return task.getWorldScale();
//                                                }
//                                            }).get();
//                                        } catch (InterruptedException | ExecutionException e) {
//                                            e.printStackTrace();
//                                        }
//
//
////                                        Mesh s = new Dome(null, 2, 10, 0.25f * worldScale.length(), false);
////                                        Mesh s = new Cylinder(10, 1, 0.25f * worldScale.length(), 0.25f * worldScale.length(), true);
//
//                                        Mesh finalS = new Sphere(7, 7, 0.25f * worldScale.length());
//
////                                        Mesh s = new Sphere(7, 7, 0.25f * worldScale.length());
////                                        Geometry geom = new Geometry("DU Created Sphere", s);
////
////                                        Mesh s2 = new Arrow(new Vector3f(0, 0.25f * worldScale.length(), 0));
////                                        Geometry geom2 = new Geometry("DU Created Arrow", s2);
////
////                                        List<Geometry> geoms = new ArrayList<Geometry>(2) {{
////                                            add(geom); add(geom2);
////                                        }};
////
////                                        Mesh finalS = new Mesh();
////                                        GeometryBatchFactory.mergeGeometries(geoms, finalS);
//
//                                        debuggedBounds.put(task, new Geometry("DU Created Compound", finalS));
//                                    }
//                                }
//
//                                if (task instanceof Geometry || nodeBounds) {
//                                    BoundingVolume bv = null;
//                                    try {
//                                        bv = app.enqueue(new Callable<BoundingVolume>() {
//                                            @Override
//                                            public BoundingVolume call() throws Exception {
//                                                return task.getWorldBound();
//                                            }
//                                        }).get();
//                                    } catch (InterruptedException | ExecutionException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    if (bv != null) {
//                                        if (bv instanceof BoundingBox) {
//                                            BoundingBox bb = (BoundingBox) bv;
//                                            debuggedBounds.put(task, SpatialUtil.createBox(bb.getXExtent(), bb.getYExtent(), bb.getZExtent()));
//                                        } else if (bv instanceof BoundingSphere) {
//                                            BoundingSphere bs = (BoundingSphere) bv;
//                                            debuggedBounds.put(task, SpatialUtil.createSphere(bs.getRadius()));
//                                        } else {
//                                            LoggerFactory.getLogger(this.getClass()).warn("Bounding volume type not supported: {}.", bv.getClass());
//                                        }
//                                    } else {
//                                        LoggerFactory.getLogger(this.getClass()).warn("Bounding volume null for: {}", task);
//                                    }
////                                    }
//                                }
////                                else {
////                                    if (((Node) task).getQuantity() == 0) {
////                                        Vector3f worldScale = null;
////                                        try {
////                                            worldScale = app.enqueue(new Callable<Vector3f>() {
////                                                @Override
////                                                public Vector3f call() throws Exception {
////                                                    return task.getWorldScale();
////                                                }
////                                            }).get();
////                                        } catch (InterruptedException | ExecutionException e) {
////                                            e.printStackTrace();
////                                        }
////
////
////                                        Sphere s = new Sphere(10, 10, 0.25f * worldScale.length());
////                                        Geometry geom = new Geometry("DU Created Sphere", s);
////
////
////                                        debuggedBounds.put(task, geom);
////                                    }
////                                }
//
//                                return false;
//                            }
//                        });
//
//                        return null;
//                    }
//                });
//            }
//        }
//
//        //TODO: MAKE A WHOLE GREAT SYSTEM INSTEAD OF THIS. With it debuggers per class (ie: MeshDebugger, BoundDebugger), and task add/removal.
//        //TODO: This would avoid lots of repeated iterations for every active debugger.
////        if(meshes) {
////            if(meshesFuture == null || meshesFuture.isDone()) {
////                meshesFuture = pool.submit(new Callable<Void>() {
////                    @Override
////                    public Void call() {
////
////                        while(meshes) {
////                            if(originalColors) {
////                                SpatialUtil.visitNodeWith(rootNode, new SpatialUtil.Operation() {
////                                    @Override
////                                    public boolean operate(Spatial task) {
////                                        if (task == debugsNode) {
////                                            return true;
////                                        }
////
////                                        if (task instanceof Geometry && !debuggedMeshes.containsKey(task)) {
////                                            Geometry geom = (Geometry) task;
////
////                                            debuggedMeshes.put(geom, geom.getMaterial());
////
////                                            RenderState renderState = geom.getMaterial().getAdditionalRenderState();
////                                            renderState.setWireframe(!renderState.isWireframe());
////
////                                            return true;
////                                        }
////
////                                        return false;
////                                    }
////                                });
////                            } else {
////                                SpatialUtil.visitNodeWith(rootNode, new SpatialUtil.Operation() {
////                                    @Override
////                                    public boolean operate(Spatial task) {
////                                        if (task == debugsNode) {
////                                            return true;
////                                        }
////
////                                        if (task instanceof Geometry && !debuggedMeshes.containsKey(task)) {
////                                            Geometry geom = (Geometry) task;
////                                            debuggedMeshes.put(geom, geom.getMaterial());
////
////                                            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
////
////                                            ColorRGBA color = ColorRGBA.randomColor();
////                                            if (geom.getQueueBucket() == RenderQueue.Bucket.Sky) {
////                                                color.set(color.r, color.g, color.b, 0.1f);
////                                                mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
////                                            }
////
////                                            mat.setColor("Color", color);
////                                            geom.setMaterial(mat);
////                                            mat.getAdditionalRenderState().setWireframe(true);
////
////                                            return true;
////                                        }
////
////                                        return false;
////                                    }
////
////
////                                });
////                            }
////                        }
////
////                        return null;
////                    }
////                });
////            }
////        } else {
////            if(meshesFuture != null) {
////                meshesFuture.cancel(true);
////                meshesFuture = null;
////
////            }
////        }
//    }
//
//    public static boolean isDebugBoundsEnabled() {
//        DebugUtil debugInstance = enabledDebugs.get(app);
//        return debugInstance.bounds;
//    }
//
//    public static void debugBounds(boolean enabled, boolean includeNodes) {
////        Node rootNode = ((SimpleApplication)app).getRootNode();
//
//
//
//        DebugUtil debugInstance = enabledDebugs.get(app);
//        if(enabled == debugInstance.bounds) {
//            return;
//        }
//
//
//        if(enabled) {
//            debugInstance.debuggedBounds = new ConcurrentHashMap<>();//SpatialUtil.getBounds(rootNode, new ConcurrentHashMap<>());
//            debugInstance.debugsMaterial = SpatialUtil.createMaterial(assetManager, ColorRGBA.Green);
//            debugInstance.nodesMaterial = SpatialUtil.createMaterial(assetManager, ColorRGBA.Yellow);
//            debugInstance.debugsMaterial.getAdditionalRenderState().setWireframe(true);
//            debugInstance.nodesMaterial.getAdditionalRenderState().setWireframe(true);
//
//
////            pool.execute(new Runnable() {
////                @Override
////                public void run() {
////                    while(true) {
////                        SpatialUtil.visitNodeWith(rootNode, new SpatialUtil.Operation() {
////                            @Override
////                            public boolean operate(Spatial task) {
////                                LoggerFactory.getLogger(this.getClass()).debug("Operating for: {}", task);
////
////                                if (task instanceof Geometry) {
////                                    Geometry bound = debugInstance.debuggedBounds.get(task);
////                                    if (bound == null) {
////                                        //                                    LoggerFactory.getLogger(this.getClass()).debug("Spatial bounding searching: {}", task);
////                                        BoundingVolume bv = task.getWorldBound();
////                                        if (bv != null) {
////                                            if (bv instanceof BoundingBox) {
////                                                BoundingBox bb = (BoundingBox) bv;
////                                                debugInstance.debuggedBounds.put(task, SpatialUtil.createBox(bb.getXExtent(), bb.getYExtent(), bb.getZExtent()));
////                                            } else if (bv instanceof BoundingSphere) {
////                                                BoundingSphere bs = (BoundingSphere) bv;
////                                                debugInstance.debuggedBounds.put(task, SpatialUtil.createSphere(bs.getRadius()));
////                                            } else {
////                                                LoggerFactory.getLogger(this.getClass()).warn("Bounding volume type not supported: {}.", bv.getClass());
////                                            }
////                                        }
////                                    }
////                                } else {
////                                    String spatialName = task.getName();
////                                    if (spatialName != null && spatialName.equals(DEBUG_BOUNDS_NAME)) {
////                                        return true;
////                                    }
////
////                                    if (((Node) task).getQuantity() == 0) {
////                                        Sphere b = new Sphere(10, 10, 0.25f * task.getWorldScale().length());
////                                        Geometry geom = new Geometry(null, b);
////
////                                        debugInstance.debuggedBounds.put(task, geom);
////                                    }
////                                }
////
////                                return false;
////                            }
////                        });
////                    }
////                }
////            });
//        } else {
//            for(Map.Entry<Spatial, Geometry> entry : debugInstance.debuggedBounds.entrySet()) {
//                Spatial key = entry.getKey();
//                debugInstance.debuggedBounds.remove(key);
//
//                entry.getValue().removeFromParent();
//            }
//        }
//
//        debugInstance.bounds = enabled;
//        debugInstance.nodeBounds = includeNodes;
//
////        Node rootNode = ((SimpleApplication)app).getRootNode();
////
////        Spatial bounds = null;
////        for(Spatial task : rootNode.getChildren()) {
////            if(task.getName().equals(DEBUG_BOUNDS_NAME)) {
////                bounds = task;
////                break;
////            }
////        }
////
////        if(bounds != null) {
////            bounds.removeFromParent();
////        }
////
////        bounds = SpatialUtil.getAllBounds(rootNode);
////        bounds.setName(DEBUG_BOUNDS_NAME);
////        bounds.setMaterial(SpatialUtil.createMaterial(assetManager, ColorRGBA.randomColor()));
////
////        rootNode.attachChild(rootNode);
//    }

    public static void debugScenegraph() {
        SpatialUtil.drawGraph(((SimpleApplication)app).getRootNode());
    }

    public static void debugLine(Node rootNode, AssetManager assetManager, ColorRGBA drawColor, Vector3f... drawPoints) {
        if(rootNode == null) {
            rootNode = DebugUtil.rootNode;
            if(rootNode == null) {
                log.warn("RootNode is null, not debugging.");
                return;
            }
        }

        if(assetManager == null) {
            assetManager = DebugUtil.assetManager;
            if(assetManager == null) {
                log.warn("AssetManager is null, not debugging.");
                return;
            }
        }

        if(drawColor == null) {
            drawColor = ColorRGBA.randomColor();
        }

        rootNode.attachChild(getLine(assetManager, null, drawColor, drawPoints));
    }

    public static void setRootNode(Node rootNode) {
//        DebugUtil.rootNode = rootNode;
        if(rootNode != null) {
            log.info("Debug nodes attached to root: {}.", rootNode);
            batchNode = new BatchNode("DebugBatchNode");
            rootNode.attachChild(batchNode);
        } else {
            if(batchNode != null) {
                log.info("Debug nodes detached from root: {}.", batchNode.getParent());
                batchNode.removeFromParent();
            }

            batchNode = null;
        }

        DebugUtil.rootNode = rootNode;
    }

    public static Geometry getLine(AssetManager assetManager, Geometry store, ColorRGBA drawColor, Vector3f... drawPoints) {
        int size = drawPoints.length;

        if(size < 2) {
            log.warn("Shouldn't be trying to debug a line with: {} points.", size);
            return null;
        }

        float[] points = new float[size * 3];
        short[] index = new short[(size - 1) * 2];

        ///////// Avoiding out of index (faster than if inside loop)
        int i = 0;
        Vector3f pos = drawPoints[i];

        int j = i*3;
        points[j++] = pos.x;
        points[j++] = pos.y;
        points[j] = pos.z;

//            int k = i*2;
//
//            index[k++] = (short)i;
//            index[k] = (short)(i + 1);
        ///////////////////////////////////


        for(i = 1; i < size; i++) {
            pos = drawPoints[i];
            j = i*3;
            points[j++] = pos.x;
            points[j++] = pos.y;
            points[j] = pos.z;

            int k = (i - 1)*2;

            index[k++] = (short)(i - 1);
            index[k] = (short)i;
        }
        //////////////////////////////////////////


        Mesh m;
        if(store == null) {
            store = new Geometry();
            m = new Mesh();
        } else {
            m = store.getMesh();
            
            if(m == null) m = new Mesh();
            
            m.clearBuffer(VertexBuffer.Type.Position);
            m.clearBuffer(VertexBuffer.Type.Index);
            
        }
        
        if(drawColor != null) {
            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setName("Line-Debug_material");
            material.setColor("Color", drawColor);
            material.getAdditionalRenderState().setWireframe(true);
            store.setMaterial(material);
        }

        m.setMode(Mesh.Mode.Lines);
        m.setBuffer(VertexBuffer.Type.Position, 3, points);
        m.setBuffer(VertexBuffer.Type.Index, 2, index);
        m.updateBound();
        m.updateCounts();

        store.setMesh(m);
        
        return store;
    }
    
    public static Geometry getRayLine(AssetManager assetManager, Geometry store, ColorRGBA drawColor, Ray ray) {
        return getLine(assetManager, store, drawColor, ray.getOrigin(), ray.getOrigin().add(ray.getDirection().mult(Integer.MAX_VALUE)));
    }


    public static void debugLocation(Node rootNode, AssetManager assetManager, ColorRGBA color, Vector3f location) {
        Geometry geom = new Geometry("Sphere", new Sphere(10,10,1));
        Material mat = new Material(assetManager,  "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setName("Location-Debug_material");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        
        geom.setLocalTranslation(location);
        add(geom);
//        rootNode.attachChild(geom);
    }

    public static Geometry debugMesh(Mesh mesh, ColorRGBA color) {
        Geometry geometry = new Geometry("Debug_Geom", mesh);

        debugGeometry(geometry, null);

        return geometry;
    }

    public static void debugSpatial(Spatial spatial, Vector3f position, ColorRGBA color) {
        spatial.setLocalTranslation(position);
        debugSpatial(spatial, color);
    }

    public static void debugSpatial(Spatial spatial, ColorRGBA color) {
        if(color == null) {
            color = ColorRGBA.randomColor();
        }

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setName("Spatial-Debug_material");
        material.setColor("Color", color);
        material.getAdditionalRenderState().setWireframe(true);
        spatial.setMaterial(material);

        add(spatial);
    }

    /**
     * @param spatial
     * @param color
     * @deprecated in favor of {@link #debugSpatial(Spatial, ColorRGBA)}
     */
    @Deprecated
    public static void debugGeometry(Spatial spatial, ColorRGBA color) {
        debugSpatial(spatial, color);
    }


    public static Mesh createNormalArrows(Geometry geometry, ColorRGBA color, float scale, boolean tangents) {
        VertexBuffer positionBuffer = geometry.getMesh().getBuffer(VertexBuffer.Type.Position);
        Vector3f[] positionVertexes = BufferUtils.getVector3Array((FloatBuffer) positionBuffer.getData());

        VertexBuffer normalBuffer;
        if(tangents) {
            normalBuffer = geometry.getMesh().getBuffer(VertexBuffer.Type.Tangent);
        } else {
            normalBuffer = geometry.getMesh().getBuffer(VertexBuffer.Type.Normal);
        }

        Vector3f[] normalsVectors = BufferUtils.getVector3Array((FloatBuffer) normalBuffer.getData());

        //FIXME: NPE when using tangent. See code in TangentBinormalGenerator.java to fix it.
        Geometry[] arrows = new Geometry[normalsVectors.length];
        for (int i = 0; i < normalsVectors.length; i++) {
            Geometry arrow = SpatialUtil.createArrow(normalsVectors[i].multLocal(scale), color);
            arrow.setLocalTranslation(positionVertexes[i]);
            arrows[i] = arrow;
        }
        Mesh normalsMesh = new Mesh();
        GeometryBatchFactory.mergeGeometries(Arrays.asList(arrows), normalsMesh);

        normalsMesh.setStatic();

        return normalsMesh;
    }


    public static Material createNormalMaterial(Geometry geom) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        material.setName("Normal-Debug_material");

        return material;
    }

    public static Material createDebugMaterial(Geometry geom, boolean colors) {
        Material mat;

        if(geom instanceof ParticleEmitter && ((ParticleEmitter) geom).getMeshType() == ParticleMesh.Type.Point) {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
            mat.setName("PointParticle-Debug_material");
//            if(colors) {
                mat.setColor("GlowColor", ColorRGBA.randomColor());
//            }
            return mat;
        }

        if(colors) {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setName("Color-Debug_material");
            ColorRGBA color = ColorRGBA.randomColor();
            if (geom.getQueueBucket() == RenderQueue.Bucket.Sky) {
                color.set(color.r, color.g, color.b, 0.1f);
                mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            }

            mat.setColor("Color", color);
        } else {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setName("VertexColor-Debug_material");
            mat.setBoolean("VertexColor", true);
        }

//        if(wire) {
//            mat.getAdditionalRenderState().setWireframe(true);
//        }
//        if(faceCullOff) {
//            mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
//        }

        return mat;
    }

    public static void debugMesh(Node rootNode, AssetManager assetManager, Mesh mesh, ColorRGBA color) {
        Geometry navGeom = new Geometry("NavMesh");
        
        navGeom.setMesh(mesh);
        Material green = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        green.setName("Mesh-Debug_material");
        green.setColor("Color", color);
        green.getAdditionalRenderState().setWireframe(true);
        navGeom.setMaterial(green);
        rootNode.attachChild(navGeom);
        
        Vector3f translation = navGeom.getLocalTranslation();
        navGeom.setLocalTranslation(translation.setY(translation.getY() + .1f));
    }

    public static Geometry getDebugBox(Vector3f extents, ColorRGBA color) {
        Geometry geom = SpatialUtil.createBox(extents);
        geom.setMaterial(SpatialUtil.createMaterial(assetManager, color));

        return geom;
    }

    public static Geometry getDebugBox(float halfX, float halfY, float halfZ, ColorRGBA color) {
        Geometry geom = SpatialUtil.createBox(halfX, halfY, halfZ);
        geom.setMaterial(SpatialUtil.createMaterial(assetManager, color));

        return geom;
    }

    public static Geometry getDebugBox(float size, ColorRGBA color) {
        Geometry geom = SpatialUtil.createBox(size);
        geom.setMaterial(SpatialUtil.createMaterial(assetManager, color));

        return geom;
    }

    public static Geometry getDebugArrow(Vector3f vector, ColorRGBA color) {
        Geometry geom = SpatialUtil.createArrow(vector);
        geom.setMaterial(SpatialUtil.createMaterial(assetManager, color));

        return geom;
    }

    public static void debugVector(Vector3f vector, Vector3f position, ColorRGBA color) {
        debugSpatial(getDebugArrow(vector, color), position);
    }

    public static void debugPoints(ColorRGBA color, Vector3f... points) {
//        Geometry geom = SpatialUtil.createBox(0.02f);
//        geom.setMaterial(SpatialUtil.createMaterial(assetManager, color));

        for(Vector3f point : points) {
            debugSpatial(getDebugBox(0.02f, color), point);
        }
    }

    public static void debugPoint(Vector3f point, ColorRGBA color) {
//        Geometry geom = SpatialUtil.createBox(0.02f);
//        geom.setMaterial(SpatialUtil.createMaterial(assetManager, color));
        debugSpatial(getDebugBox(0.02f, color), point);
    }


    private static void debugSpatial(final Spatial spatial, Vector3f position) {
        spatial.setLocalTranslation(position);
        debugSpatial(spatial);
    }

    private static void debugSpatial(final Spatial spatial) {
        app.enqueue(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                add(spatial);
                return null;
            }
        });
    }



    public static void debugRay(Vector3f from, ColorRGBA fromColor, Vector3f to, ColorRGBA toColor) {
        debugPoint(from, fromColor);
        debugPoint(to, toColor);
    }

    static int count;
    private static void add(Spatial spatial) {
        if(batchNode != null) {
            batchNode.attachChild(spatial);

            if(count++ > 50) {
                count = 0;
//                batchNode.batch();
            }
        } else {
            log.error("No debug rootNode set");
        }
    }

    private static void remove(Spatial spatial) {
        if(batchNode != null) {
            spatial.removeFromParent();

            batchNode.batch();
        } else {
            log.error("No debug rootNode set");
        }
    }


    private static class PrintTraceException extends RuntimeException {

    }
    public static void printStackTrace() {
        try {
            throw new PrintTraceException();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void validateSpatial(Spatial spatial) {
        spatial.depthFirstTraversal(s -> {
            SkeletonControl skeletonControl = s.getControl(SkeletonControl.class);
            if(skeletonControl == null) {
                return;
            }

            if(skeletonControl.getSkeleton().getBoneCount() == 0) {
                log.error("Removing Skeleton. Skeleton must have at least 1 bone. Spatial: {}, with parent: {}", s, spatial);
                s.removeControl(skeletonControl);
            } else if(skeletonControl.getSkeleton().getBoneCount() > 256) {
                log.error("Removing Skeleton. Skeleton must have less than 256 bones. Spatial: {}, with parent: {}", s, spatial);
                s.removeControl(skeletonControl);
            }
        });

        spatial.depthFirstTraversal(s -> {
            if(!(s instanceof Geometry)) {
                return;
            }

            ((Geometry) s).getMaterial().getParams().forEach( param -> {
                    if(param.getValue().getClass().isArray() && ((Object[])param.getValue()).length == 0) {
                        log.error("Replacing material for Geometry. Material array param can't be empty. Spatial: {}, with parent: {}", s, spatial);
                        s.setMaterial(createDebugMaterial((Geometry) s, false));
                    }
            });
        });
    }


    /**
     *
     * @author survivor
     */
    public static Vector3f[] getFrustumDebugPoints(Camera cam, Vector3f[] points) {
        if(points == null) {
            points = new Vector3f[8];
        }

        int w = cam.getWidth();
        int h = cam.getHeight();
        final float n = 0;
        final float f = 1f;

        points[0].set(cam.getWorldCoordinates(new Vector2f(0, 0), n));
        points[1].set(cam.getWorldCoordinates(new Vector2f(0, h), n));
        points[2].set(cam.getWorldCoordinates(new Vector2f(w, h), n));
        points[3].set(cam.getWorldCoordinates(new Vector2f(w, 0), n));

        points[4].set(cam.getWorldCoordinates(new Vector2f(0, 0), f));
        points[5].set(cam.getWorldCoordinates(new Vector2f(0, h), f));
        points[6].set(cam.getWorldCoordinates(new Vector2f(w, h), f));
        points[7].set(cam.getWorldCoordinates(new Vector2f(w, 0), f));

        return points;
    }

}

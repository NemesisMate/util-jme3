package com.nx.util.jme3.base;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.light.Light;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.GeometryGroupNode;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shader.VarType;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.util.SafeArrayList;
import jme3tools.optimize.GeometryBatchFactory;
import jme3tools.optimize.TextureAtlas;
import org.slf4j.LoggerFactory;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.nx.util.jme3.base.DebugUtil.assetManager;

/**
 * TODO: merge all duplicated methods (they can be handled in a single one without any performance impact. Doubling them is just useless... I think.)
 * @author NemesisMate
 */
public final class SpatialUtil {

    private SpatialUtil() {

    }

    public interface Operation {
        
        /**
         * Must stop that branch
         * @return 
         */
        boolean operate(Spatial spatial);
    }

    public interface GeometryPredicate extends Predicate<Spatial> {
        @Override
        default boolean test(Spatial spatial) {
            return spatial instanceof Geometry && testGeometry((Geometry) spatial);
        }

        boolean testGeometry(Geometry geometry);
    }

    public interface NodePredicate extends Predicate<Spatial> {
        @Override
        default boolean test(Spatial spatial) {
            return spatial instanceof Node && testNode((Node) spatial);
        }

        boolean testNode(Node node);
    }


    // TODO improve visit first to cut on the for if returnedObject on returnoperation == null.
    public static <T extends Operation> T visitNodeWith(Spatial spatial, T operation) {
        if(!operation.operate(spatial)) {
            if(spatial instanceof Node) {
                for(Spatial s : ((Node)spatial).getChildren()) {
                    visitNodeWith(s, operation);
                }
            }
        }

        return operation;
    }

//    public void generateTangents(Spatial spatial) {
//        if(spatial == null) {
//            return;
//        }
//
//        spatial.depthFirstTraversal(new SceneGraphVisitor() {
//            @Override
//            public void visit(Spatial spatial) {
//                if(spatial instanceof Geometry) {
//                    Mesh mesh = ((Geometry) spatial).getMesh();
//                    if(mesh.getBuffer(VertexBuffer.Type.Normal) != null && mesh.getBuffer(VertexBuffer.Type.Tangent) == null) {
//                        TangentBinormalGenerator.generate(mesh);
//                    }
//                }
//            }
//        });
//    }

    /**
     * Prints the scenegraph on the debug console.
     * 
     * @param spatial to print recursively.
     */
    public static void drawGraph(Spatial spatial) {
        System.out.println(spatial);
        
        if(!(spatial instanceof Node)) return;
        
        List<Spatial> spatials = new ArrayList<>();
        //Map<Spatial, Integer> repeated = new HashMap<>();
        List<String> lines = sceneGraphLoop(((Node)spatial).getChildren(), spatials, "", new ArrayList<String>());

        for(String line : lines) {
            System.out.println(line);
        }


        for(Spatial spat : spatials) {
            int count = 0;
            //System.out.println(spatial.getName());
            for(Spatial spat2 : spatials)
                if(spat == spat2) count++;
                
            if(count > 1){
                System.out.println(spat.getName() + "(REPEATED - IMPOSSIBLE): " + count);
                //repeated.put(spatial, count);
            }
        }
        System.out.println("Total: " + spatials.size() + "\n\n\n\n");
        
    }

    public static String getParentHierarchyString(Spatial spatial) {
        StringBuilder hierarchyString = new StringBuilder(spatial.getName());

        for(Spatial related : SpatialUtil.getParentHierarchy(spatial)) {
            hierarchyString.append(" <- ").append(related.getName());
        }

        return hierarchyString.toString();
    }

    public static List<Node> getParentHierarchy(Spatial spatial) {
        List<Node> hierarchy = new ArrayList<>();

        Node parent = spatial.getParent();
        while(parent != null) {
            hierarchy.add(parent);

            parent = parent.getParent();
        }

        return hierarchy;
    }

    //TODO
    public static List<String> getDrawGraph(final Spatial spatial) {
        return sceneGraphLoop(new ArrayList<Spatial>(1) {{ add(spatial); }}, new ArrayList<Spatial>(), "", new ArrayList<String>());
    }
    
    private static List<String> sceneGraphLoop(List<Spatial> spatials, List<Spatial> all, String dashes, List<String> lines) {
        dashes += "-";
        for(Spatial spatial : spatials) {
            all.add(spatial);
            
            StringBuilder data = new StringBuilder(dashes + spatial);
            if(spatial instanceof Geometry) {
                Material material = ((Geometry)spatial).getMaterial();

                data.append("(")
                        .append(material == null ?
                                "<N/M>" : ((Geometry) spatial).getMaterial().getName() + " <>" + ((Geometry) spatial).getMaterial().getKey())
                        .append(")");
            }

            int numControls = spatial.getNumControls();

            data.append(" - (")
                    .append(spatial.getTriangleCount()).append("t, ")
                    .append(spatial.getVertexCount()).append("v) ")
                    .append("LOC[L:").append(spatial.getLocalTranslation()).append(", W:").append(spatial.getWorldTranslation()).append("] ")
                    .append("SCALE[L:").append(spatial.getLocalScale()).append(", W:").append(spatial.getWorldScale()).append("] ")
                    .append("ROT[L:").append(spatial.getLocalRotation()).append(", W:").append(spatial.getWorldRotation()).append("]")
                    .append(" - Controls: ").append(numControls);

            if(numControls > 0) {
                data.append(" (");

                for(int i = 0; i < numControls; i++) {
                    Control control = spatial.getControl(i);
                    data.append(control.getClass().getSimpleName()).append(", ");
                }

                data = new StringBuilder(data.substring(0, data.length() - 2));
                data.append(")");
            }

            int numLights = spatial.getLocalLightList().size();
            data.append(", Lights: ").append(numLights);
            if(numLights > 0) {
                data.append(" (");

                for(Light light : spatial.getLocalLightList()) {
                    data.append(light.getClass().getSimpleName()).append(", ");
                }

                data = new StringBuilder(data.substring(0, data.length() - 2));
                data.append(")");
            }
            
//            System.out.println(data);
//            stringBuilder.append('\n' + data);
            lines.add(data.toString());
            
            if(spatial instanceof Node) {
                sceneGraphLoop(((Node)spatial).getChildren(), all, dashes, lines);
            }
            
        }

        return lines;
    }

    public static <T extends Spatial> T gatherFirstSpatialAncestor(Spatial spatial, Class<T> type) {
        if(type.isAssignableFrom(spatial.getClass())) {
            return (T) spatial;
        }

        Spatial parent = spatial.getParent();
        if(parent == null) {
            return null;
        }

        return gatherFirstSpatialAncestor(parent, type);
    }

    public static <T extends Spatial> T gatherFirstSpatial(Spatial spatial, Class<T> type) {
        if(type.isAssignableFrom(spatial.getClass())) {
            return (T) spatial;
        }

        if (spatial instanceof Node) {
            for (Spatial child : ((SafeArrayList<Spatial>)((Node)spatial).getChildren()).getArray()) {
                T s = gatherFirstSpatial(child, type);
                if(s != null) {
                    return s;
                }
            }
        }

        return null;
    }

    public static void gatherSpatials(Spatial spatial, Class<? extends Spatial> type, List store) {
        if(type.isAssignableFrom(spatial.getClass())) {
            store.add(spatial);
        }

        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
                gatherSpatials(child, type, store);
            }
        }
    }

    public static Geometry gatherFirstGeom(Spatial spatial) {
        if (spatial instanceof Node) {
            for (Spatial child : ((SafeArrayList<Spatial>)((Node)spatial).getChildren()).getArray()) {
                return gatherFirstGeom(child);
            }
        } else if (spatial instanceof Geometry) {
            return (Geometry) spatial;
        }

        return null;
    }

    public static List<Mesh> gatherMeshes(Spatial spatial, List<Mesh> meshStore) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
                gatherMeshes(child, meshStore);
            }
        } else if (spatial instanceof Geometry) {
            meshStore.add(((Geometry) spatial).getMesh());
        }

        return meshStore;
    }

    public static void gatherGeoms(Spatial spatial, Mesh.Mode mode, List<Geometry> geomStore) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
                gatherGeoms(child, mode, geomStore);
            }
        } else if (spatial instanceof Geometry && ((Geometry) spatial).getMesh().getMode() == mode) {
            geomStore.add((Geometry) spatial);
        }
    }

    public static Node getAllBounds(Spatial spatial) {
        final Node bounds = new Node(spatial.getName() + "_BOUNDS");

        spatial.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                if(spatial instanceof Geometry) {
                    BoundingBox bb = (BoundingBox) spatial.getWorldBound();
                    Box b = new Box(bb.getXExtent(), bb.getYExtent(), bb.getZExtent());
                    Geometry geom = new Geometry(null, b);

                    bounds.attachChild(geom);
                } else {
                    if(((Node)spatial).getQuantity() == 0) {
                        Sphere b = new Sphere(10, 10, 0.25f * spatial.getWorldScale().length());
                        Geometry geom = new Geometry(null, b);

                        bounds.attachChild(geom);
                    }
                }
            }
        });

        return bounds;
    }

    public static Map<Spatial, Geometry> getBounds(Spatial spatial, Map<Spatial, Geometry> bounds) {
        if(bounds == null) {
            bounds = new HashMap<>();
        }

        final Map<Spatial, Geometry> finalBounds = bounds;

        spatial.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                if(spatial instanceof Geometry) {
                    BoundingBox bb = (BoundingBox) spatial.getWorldBound();
                    Box b = new Box(bb.getXExtent(), bb.getYExtent(), bb.getZExtent());
                    Geometry geom = new Geometry(null, b);

                    finalBounds.put(spatial, geom);
                } else {
                    if(((Node)spatial).getQuantity() == 0) {
                        Sphere b = new Sphere(10, 10, 0.25f * spatial.getWorldScale().length());
                        Geometry geom = new Geometry(null, b);

                        finalBounds.put(spatial, geom);
                    }
                }
            }
        });

        return finalBounds;
    }

    public static boolean isChild(Spatial child, Spatial parent) {
        if(child == parent) {
            return true;
        }

        if(parent instanceof Node) {
            for (Spatial c : (((SafeArrayList<Spatial>)((Node)parent).getChildren()).getArray())) {
                if(isChild(child, c)) {
                    return true;
                }
            }
        }

        return false;
    };



    /**
     * Finds a spatial on the given spatial (usually, a node).
     * 
     * @param spatial
     * @param name
     * @return 
     */
    public static Spatial find(Spatial spatial, final String name) {
        if(spatial != null) {
            String spatName = spatial.getName();
            if(spatName != null && spatName.equals(name)) return spatial;
//            if(spatial instanceof Node) return nodeFind((Node)spatial, name);
            if(spatial instanceof Node) return ((Node)spatial).getChild(name);
        }
        
        return null;
    }

    public static Spatial find(Spatial spatial, Predicate<Spatial> predicate) {
        if(predicate.test(spatial)) {
            return spatial;
        }

        if(spatial instanceof Node) {
            for(Spatial child : ((SafeArrayList<Spatial>)((Node) spatial).getChildren()).getArray()) {
                Spatial found = find(child, predicate);
                if(found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    public static Geometry findGeometry(Spatial spatial, GeometryPredicate predicate) {
        return (Geometry) find(spatial, predicate);
    }

    public static Geometry findGeometryWithMatParam(Spatial spatial, String matParam) {
        return findGeometry(spatial, geometry -> hasMatParam(matParam, geometry));
    }

    public static Geometry findGeometryWithMatParamSet(Spatial spatial, String matParam) {
        return findGeometry(spatial, geometry -> hasMatParamSet(matParam, geometry));
    }


    public static Geometry findGeometry(Spatial spatial, final String name) {
        if(spatial != null) {
            String spatName = spatial.getName();

//            if(spatial instanceof Node) return nodeFind((Node)spatial, name);
            if(spatial instanceof Node) {
                return findGeometry((Node)spatial, name);
            } else {
                if(spatName != null && spatName.equals(name)) return (Geometry) spatial;
            }
        }

        return null;
    }

    public static Geometry findGeometry(Node node, String name) {
        for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
            String spatName = child.getName();
            if(child instanceof Node) {
                Geometry out = findGeometryStartsWith((Node)child, name);
                if(out != null) {
                    return out;
                }
            } else {
                if (spatName != null && spatName.equals(name)) {
                    return (Geometry) child;
                }
            }
        }

        return null;
    }

    public static List<Spatial> findAllStartsWith(Spatial spatial, final String name, List<Spatial> storeList, boolean nested) {
        if(spatial != null) {
            String spatName = spatial.getName();
            if(spatName != null && spatName.startsWith(name)) {
                storeList.add(spatial);

                if(nested) {
                    if(spatial instanceof Node) {
                        findAllStartsWith((Node)spatial, name, storeList, nested);
                    }
                }

            } else if(spatial instanceof Node) {
                return findAllStartsWith((Node)spatial, name, storeList, nested);
            }
        }

        return storeList;
    }

    public static List<Spatial> findAllStartsWith(Node node, final String name, List<Spatial> storeList, boolean nested) {
        for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
            String spatName = child.getName();
            if (spatName != null && spatName.startsWith(name)) {
                storeList.add(child);
                if(nested) {
                    if(child instanceof Node) {
                        findAllStartsWith((Node)child, name, storeList, nested);
                    }
                }
            } else if(child instanceof Node) {
                findAllStartsWith((Node)child, name, storeList, nested);
            }
        }

        return storeList;
    }

    public static Spatial findStartsWith(Spatial spatial, final String name) {
        if(spatial != null) {
            String spatName = spatial.getName();
            if(spatName != null && spatName.startsWith(name)) return spatial;
//            if(spatial instanceof Node) return nodeFind((Node)spatial, name);
            if(spatial instanceof Node) {
                return findStartsWith((Node)spatial, name);
            }
        }

        return null;
    }

    public static Spatial findStartsWith(Node node, String name) {
        for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
            String spatName = child.getName();
            if (spatName != null && spatName.startsWith(name)) {
                return child;
            } else if(child instanceof Node) {
                Spatial out = findStartsWith((Node)child, name);
                if(out != null) {
                    return out;
                }
            }
        }

        return null;
    }

    public static Spatial findEndsWith(Spatial spatial, final String name) {
        if(spatial != null) {
            String spatName = spatial.getName();
            if(spatName != null && spatName.endsWith(name)) return spatial;
//            if(spatial instanceof Node) return nodeFind((Node)spatial, name);
            if(spatial instanceof Node) {
                return findEndsWith((Node)spatial, name);
            }
        }

        return null;
    }

    public static Spatial findEndsWith(Node node, String name) {
        for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
            String spatName = child.getName();
            if (spatName != null && spatName.endsWith(name)) {
                return child;
            } else if(child instanceof Node) {
                Spatial out = findEndsWith((Node)child, name);
                if(out != null) {
                    return out;
                }
            }
        }

        return null;
    }


    public static String getUserDataKeyStartsWith(Spatial spatial, final String name) {
        Collection<String> keys = spatial.getUserDataKeys();

        if(keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                if(key.startsWith(name)) {
                    return key;
                }
            }
        }

        return null;
    }


    public static <T> Collection<T> getAllUserDataStartsWith(Spatial spatial, final String name) {
        Collection<String> keys = spatial.getUserDataKeys();

        if(keys != null && !keys.isEmpty()) {
            Collection<T> userDatas = new ArrayList<T>(keys.size());

            for (String key : keys) {
                if(key.startsWith(name)) {
                    userDatas.add((T) spatial.getUserData(key));
                }
            }

            return userDatas;
        }

        return null;
    }

    public static <T> T getUserDataStartsWith(Spatial spatial, final String name) {
        Collection<String> keys = spatial.getUserDataKeys();

        if(keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                if(key.startsWith(name)) {
                    return spatial.getUserData(key);
                }
            }
        }

        return null;
    }

    public static List<Spatial> findAllWithDataStartsWith(Spatial spatial, final String name, List<Spatial> storeList, boolean nested) {
        if(spatial != null) {
            Collection<String> keys = spatial.getUserDataKeys();
            if(keys != null && !keys.isEmpty()) {
                for(String key : keys) {
                    if(key.startsWith(name)) {
                        storeList.add(spatial);
                        if(!nested) {
                            return storeList;
                        } else {
                            break;
                        }
                    }
                }

            }

            if(spatial instanceof Node) {
                return findAllWithDataStartsWith((Node) spatial, name, storeList, nested);
            }
        }

        return storeList;
    }

    public static List<Spatial> findAllWithDataStartsWith(Node node, final String name, List<Spatial> storeList, boolean nested) {
        for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
            findAllWithDataStartsWith(child, name, storeList, nested);
        }

        return storeList;
    }




    public static Geometry findGeometryStartsWith(Spatial spatial, final String name) {
        if(spatial != null) {
            String spatName = spatial.getName();

//            if(spatial instanceof Node) return nodeFind((Node)spatial, name);
            if(spatial instanceof Node) {
                return findGeometryStartsWith((Node)spatial, name);
            } else {
                if(spatName != null && spatName.startsWith(name)) return (Geometry) spatial;
            }
        }

        return null;
    }

    public static Geometry findGeometryStartsWith(Node node, String name) {
        for (Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
            String spatName = child.getName();
            if(child instanceof Node) {
                Geometry out = findGeometryStartsWith((Node)child, name);
                if(out != null) {
                    return out;
                }
            } else {
                if (spatName != null && spatName.startsWith(name)) {
                    return (Geometry) child;
                }
            }
        }

        return null;
    }



    public static Spatial findHasUserData(Node node, final String key) {
        if(node != null) {
            for(Spatial spatial : node.getChildren()) {
                Spatial found = findHasUserData(spatial, key);
                if(found != null) {
                    return found;
                }
            }
        }

        return null;
    }


    public static Spatial findHasUserData(Spatial spatial, final String key) {
        if(spatial != null) {
            if(spatial.getUserData(key) != null) {
                return spatial;
            }
//            if(spatial instanceof Node) return nodeFind((Node)spatial, name);
            if(spatial instanceof Node) {
                return findHasUserData((Node)spatial, key);
            }
        }

        return null;
    }


    
    // NOT NEEDED, getChild(name) does the same thing
//    private static Spatial nodeFind(Node node, final String name) {
//        Spatial ret = null;
//        
//        for(Spatial spat : node.getChildren()) {
//            String spatName = spat.getName();
//            
//            if(spatName != null && spat.getName().equals(name)) return spat;
//            
//            if(spat instanceof Node) {
//                ret = nodeFind((Node)spat, name);
//                if(ret != null) return ret;
//            }
//            
//        }
//        
//        return ret;
//    }
    
    /**
     * Not safe (not enough tested).
     * 
     * Resizes a spatial without altering it scale.
     * TODO: make sure the animations still fine.
     * 
     * @param spatial
     * @param scale 
     */
    public static void resize(Spatial spatial, final float scale) {
        SceneGraphVisitor visitor = new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spat) {
                if(spat instanceof Geometry) {
                    VertexBuffer pb = ((Geometry)spat).getMesh().getBuffer(VertexBuffer.Type.Position);
                    FloatBuffer positions = (FloatBuffer) pb.getData();

                    for(int i = 0; i < positions.capacity(); i++) {
                        positions.put(i, positions.get(i) * scale);
                    }
                }
            }
        };
        
        spatial.depthFirstTraversal(visitor);
//        spatial.updateGeometricState();
//        spatial.updateModelBound();
        
        SkeletonControl control = spatial.getControl(SkeletonControl.class);
        if(control == null) return;
        
        for(Bone bone : control.getSkeleton().getRoots()) {
            // If don't want to change the whole model (only the instance on the spatial) uncomment
            // the following two lines (bone.setUser..., and comment bone.setBindTransforms(...)
            bone.setUserControl(true);
            bone.setUserTransforms(new Vector3f(Vector3f.ZERO), Quaternion.IDENTITY, new Vector3f(scale, scale, scale));
            //bone.setBindTransforms(Vector3f.ZERO, Quaternion.IDENTITY, new Vector3f(scale, scale, scale));
        }
        
//        AnimControl animControl = spatial.getControl(AnimControl.class);
//        if(animControl == null) return;
//        
//        
//        animControl.getAnim("").getTracks()[0]
    }
    
    /**
     * Translates the internal geometries of this spatial without altering the spatial local translation.
     * @param spatial
     * @param translation 
     */
    public static void translateGeom(final Spatial spatial, final Vector3f translation) {
        SceneGraphVisitor visitor = new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spat) {
                if(spat instanceof Geometry) {
//                    VertexBuffer pb = ((Geometry)spat).getMesh().getBuffer(VertexBuffer.Type.Position);
//                    FloatBuffer positions = (FloatBuffer) pb.getData();
//
//                    for(int i = 0; i < positions.capacity(); i++)
//                        positions.put(i, positions.get(i) + (1f/spatial.getLocalScale().y));
                    
                    //spat.setLocalTranslation(new Vector3f(0f, 1f, 0f).multLocal(Vector3f.UNIT_XYZ.divide(spatial.getLocalScale())));
                    spat.setLocalTranslation(translation.divide(spatial.getLocalScale()));
                }
            }
        };
        
        spatial.depthFirstTraversal(visitor);
    }
    
    /**
     * Rotates the internal geometries of this spatial without altering the spatial local Rotates.
     * 
     * @param spatial
     * @param angle
     * @param axis 
     */
    public static void rotateGeom(final Spatial spatial, final float angle, final Vector3f axis) {
        SceneGraphVisitor visitor = new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spat) {
                if(spat instanceof Geometry)
                    spat.getLocalRotation().fromAngleAxis(angle, axis);
                    //spat.setLocalRotation(rotation);
            }
        };
        
        spatial.depthFirstTraversal(visitor);
    }
    
    /**
     * Optimizes Geometries and Nodes for the given spatial.
     * Basically, performs a {@link #optimizeGeoms(Spatial)} followed by a {@link #optimizeNodes(Spatial)}
     * 
     * @param spatial to optimize.
     */
    public static void optimizeAll(Spatial spatial) {
        optimizeGeoms(spatial);
        optimizeNodes(spatial);
    }
    
    /**
     * Optimizes Geometries for the given spatial.
     * 
     * @param spatial to optimize.
     */
    public static void optimizeGeoms(Spatial spatial) {
        if(spatial instanceof Node) {
            GeometryBatchFactory.optimize((Node)spatial);
//            System.gc();
        }
    }
    
    /**
     * Optimizes Nodes for the given spatial.
     * 
     * @param spatial to optimize.
     */
    public static void optimizeNodes(Spatial spatial) {
        SceneGraphVisitor visitor = new SceneGraphVisitor() {
//            public int i = 0;
//            Node compareNode = new Node("node");
            @Override
            public void visit(Spatial spat) {
//                SceneGraphVisitor visitor = new SceneGraphVisitor() {
//                    @Override
//                    public void visit(Spatial spat) {
//                        
//                    }
//                
//                };
                if(spat instanceof Node) {
                    if(((Node)spat).getChildren().isEmpty()) spat.removeFromParent();
                    else for(Spatial spat2 : ((Node)spat).getChildren()) {
                        if(spat2 instanceof Node) {
                            if(spat2.getLocalScale().equals(Vector3f.UNIT_XYZ) &&
                               spat2.getLocalTranslation().equals(Vector3f.ZERO) &&
                               spat2.getLocalRotation().equals(Quaternion.IDENTITY) &&
                               spat2.getLocalLightList().size() == 0) {
//                            spat2.setName("node");
//                            if(spat2.equals(compareNode)) {
//                                System.out.println("Nodo sobrante");
//                                i++;
                                for(Spatial spat3 :((Node)spat2).getChildren())
                                    ((Node)spat).attachChild(spat3);
                                
                                spat.removeFromParent(); // igual a ((Node)spat).detachChild(spat2);
                            }
                        }
                    }
                    //spat.depthFirstTraversal(visitor);
                }
//                System.out.println("Total: " + i);
            }
        };
        
        spatial.depthFirstTraversal(visitor);
//        System.gc();
    }

    public static boolean hasControl(Spatial spat, Class<? extends Control> controlType) {
        return getFirstControlFor(spat, controlType) != null;
    }

    public static boolean hasControl(Spatial spat, Control control) {
        int numControl = spat.getNumControls();

        for(int i = 0; i < numControl; i++) {
            if(spat.getControl(i) == control) {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated use {@link #getControlsFor(Spatial, Class)} instead
     */
    @Deprecated
    public static Collection<AnimControl> getAnimControlsFor(Spatial spat) {
        return getControlsFor(spat, AnimControl.class);
    }

    /**
     * @deprecated use {@link #getFirstControlFor(Spatial, Class)} instead
     */
    @Deprecated
    public static AnimControl getFirstAnimControlFor(Spatial spat) {
        return getFirstControlFor(spat, AnimControl.class);
    }



    public static void removeControlsFor(Spatial spat, final Class<? extends Control> controlClass) {
//        List<T> controls = new ArrayList<>(3);

        spat.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                Control control = spatial.getControl(controlClass);
                if(control != null) {
                    spatial.removeControl(control);
//                    controls.add(control);
                }
            }
        });

//        return controls;
    }

    public static <T extends Control> Collection<T> getControlsNoRecursive(Spatial spat, Class<T> controlClass) {
        ArrayList<T> controls = new ArrayList<>(3);

        int numControls = spat.getNumControls();

        for(int i = 0; i < numControls; i++) {
            Control control = spat.getControl(i);
            if(controlClass.isAssignableFrom(control.getClass())) {
                controls.add((T) control);
            }
        }
        controls.trimToSize();

        return controls;
    }

    public static <T extends Control> Collection<T> getControlsFor(Spatial spat, final Class<T> controlClass) {
        final List<T> controls = new ArrayList<>(3);

        spat.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                T control = spatial.getControl(controlClass);
                if(control != null) {
                    controls.add(control);
                }
            }
        });

        return controls;
    }



    public static <T extends Control> T getFirstControlFor(Spatial spat, final Class<T> controlClass) {
        T control = spat.getControl(controlClass);
        if(control == null && (spat instanceof Node)) {
            for(Spatial child : ((SafeArrayList<Spatial>)((Node) spat).getChildren()).getArray()) {
                control = getFirstControlFor(child, controlClass);
                if(control != null) {
                    return control;
                }
            }
        }

        return control;

//        return visitNodeWith(spat, new Operation() {
//
//            T firstControl;
//
//            @Override
//            public boolean operate(Spatial spatial) {
//                if(firstControl != null) {
//                    return true;
//                }
//
//                T control = spatial.getControl(controlClass);
//                if(control != null) {
//                    this.firstControl = control;
//                    return true;
//                }
//
//                return false;
//            }
//        }).firstControl;
    }

    public static <T extends AbstractControl> T getFirstEnabledControlFor(Spatial spat, final Class<T> controlClass) {
        T control = spat.getControl(controlClass);
        if((control == null || !control.isEnabled()) && (spat instanceof Node)) {
            for(Spatial child : ((SafeArrayList<Spatial>)((Node) spat).getChildren()).getArray()) {
                control = getFirstEnabledControlFor(child, controlClass);
                if(control != null) {
                    return control;
                }
            }
        }

        return control;

//        //TODO: Change to a non-new-instantiation way
//        return visitNodeWith(spat, new Operation() {
//
//            T firstControl;
//
//            @Override
//            public boolean operate(Spatial spatial) {
//                if(firstControl != null) {
//                    return true;
//                }
//
//                T control = spatial.getControl(controlClass);
//                if(control != null && control.isEnabled()) {
//                    this.firstControl = control;
//                    return true;
//                }
//
//                return false;
//            }
//        }).firstControl;
    }

    public static Geometry createArrow(Vector3f direction) {
        Geometry geometry = new Geometry("SU Created Arrow", new Arrow(direction));
        if(geometry.getMesh().getBuffer(VertexBuffer.Type.Normal) == null) {
            int vCount = geometry.getMesh().getVertexCount();
            float[] buff = new float[vCount * 3];
            for(int i = 0; i < vCount; i++) {
                buff[i] = 0;
            }

            geometry.getMesh().setBuffer(VertexBuffer.Type.Normal, 3, buff);
        } else {
            LoggerFactory.getLogger(SpatialUtil.class).error("I know, this is not an error, but all this normal buffers stuff is not more needed.");
        }
        return geometry;
    }

    /**
     * Use the DebugUtil getDebugArrow instead.
     * @param direction
     * @param color
     * @return
     */
    @Deprecated
    public static Geometry createArrow(Vector3f direction, ColorRGBA color) {
        Arrow arrow = new Arrow(direction);
        Geometry geometry = new Geometry("SU Created Arrow", arrow);

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        if(color == null) {
            color = ColorRGBA.randomColor();
        }

        material.setColor("Color", color);

        geometry.setMaterial(material);

        return geometry;
    }


    public static Geometry createBox(float size) {
        return createBox(size, size, size);
    }

    public static Geometry createBox(Vector3f extents) {
        return createBox(extents.x, extents.y, extents.z);
    }

    public static Geometry createBox(float halfX, float halfY, float halfZ) {
        return new Geometry("SU Created Box", new Box(halfX, halfY, halfZ));  // create cube geometry from the shape
    }

    public static Geometry createSphere(float size) {
        return new Geometry("SU Created Sphere", new Sphere(10, 10, size));
    }

    public static Geometry createSphere(Vector3f extents) {
        return createSphere(extents.x, extents.y, extents.z);
    }

    public static Geometry createSphere(float halfX, float halfY, float halfZ) {
        if(halfY > halfX) {
            if(halfZ > halfY) {
                return createSphere(halfZ);
            }
            return createSphere(halfY);
        }

        return createSphere(halfX);
    }

    public static Material createMaterial(AssetManager assetManager, ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color != null ? color : ColorRGBA.randomColor());

        return mat;
    }

    //TODO: redo the full split method having in mind it optimization for usages with substring (avoiding it intensive current new allocation)
    public static List<String> splitTextIntoLines(BitmapFont font, float maxChatWidth, String text, List<String> finalMessage) {
        if(finalMessage == null) {
            finalMessage = new ArrayList<>();
        } else {
            if(!(finalMessage instanceof ArrayList)) {
//                LoggerFactory.getLogger(SpatialUtil.class).error("The current implementation only supports ArrayList");
                throw new UnsupportedOperationException("The current implementation only supports ArrayList");
            }
        }

        finalMessage = splitTextIntoLines(font, maxChatWidth, finalMessage, text.split(" "), 0);

        ((ArrayList)finalMessage).trimToSize();
        return finalMessage;
    }

    private static List<String> splitTextIntoLines(BitmapFont font, float maxChatWidth, List<String> finalMessage, String[] words, int index) {
        String current;
        String substring;
        float accumulated = 0;
        float whiteSpaceSize = font.getLineWidth(" ");
        float dashSpaceSize = font.getLineWidth("-");

        if(finalMessage.isEmpty()) finalMessage.add("");
        else accumulated = font.getLineWidth(finalMessage.get(finalMessage.size() - 1)) + whiteSpaceSize;

        for(;index < words.length; ++index) {
            accumulated += font.getLineWidth(words[index]) + whiteSpaceSize;
            if(accumulated > maxChatWidth) {
                // If the word itself is too big
                if(font.getLineWidth(words[index]) > maxChatWidth) {
                    if(words[index].length() <= 1) {
//                        LoggerFactory.getLogger(SpatialUtil.class).error("Given max width too small: {}", maxChatWidth);
                        throw new UnsupportedOperationException("Given max width too small: " + maxChatWidth);
                    }

                    int wordIndex = 0;
                    while(wordIndex < words[index].length()) {
                        substring = words[index].substring(wordIndex, words[index].length());
                        float size = font.getLineWidth(substring) + dashSpaceSize;
                        while(size > maxChatWidth) {
                            substring = substring.substring(0, substring.length()/2);
                            size = font.getLineWidth(substring) + dashSpaceSize;
                        }

                        // < because we have to add the "-" character at the end of that word
                        int wordEnd = wordIndex + substring.length();
                        while(size < maxChatWidth && wordEnd < words[index].length()) {
                            substring = words[index].substring(wordIndex, wordEnd++);
                            size = font.getLineWidth(substring) + dashSpaceSize;
                        }

                        wordIndex += substring.length();

                        int currentIndex = finalMessage.size() - 1;
                        current = finalMessage.get(currentIndex);
                        if(current.length() != 0) {
                            current = substring; //new StringBuilder(substring);
                            finalMessage.add(current);
                            currentIndex++;
                        } else current += substring;

                        if(wordIndex < words[index].length()) current += "-";
                        else current += " ";

                        finalMessage.set(currentIndex, current);
                    }

                    return splitTextIntoLines(font, maxChatWidth, finalMessage, words, index + 1);

                }
                else {
                    accumulated = font.getLineWidth(words[index]) + whiteSpaceSize;
                    finalMessage.add("");
                }
            }

            int currentIndex = finalMessage.size() - 1;
            current = finalMessage.get(currentIndex) + words[index];

            if(index < words.length - 1) current += " ";

            finalMessage.set(currentIndex, current);
        }

        return finalMessage;
    }

    public static List<StringBuilder> splitTextIntoLines2(BitmapFont font, float maxChatWidth, String text, List<StringBuilder> finalMessage) {
        if(finalMessage == null) {
            finalMessage = new ArrayList<>();
        } else {
            if(!(finalMessage instanceof ArrayList)) {
//                LoggerFactory.getLogger(SpatialUtil.class).error("The current implementation only supports ArrayList");
                throw new UnsupportedOperationException("The current implementation only supports ArrayList");
            }
        }

        finalMessage = splitTextIntoLines2(font, maxChatWidth, finalMessage, text.split(" "), 0);

        ((ArrayList)finalMessage).trimToSize();
        return finalMessage;
    }

    private static List<StringBuilder> splitTextIntoLines2(BitmapFont font, float maxChatWidth, List<StringBuilder> finalMessage, String[] words, int index) {
        StringBuilder current;
        String substring;
        float accumulated = 0;
        float whiteSpaceSize = font.getLineWidth(" ");
        float dashSpaceSize = font.getLineWidth("-");

        if(finalMessage.isEmpty()) finalMessage.add(new StringBuilder(""));
        else accumulated = font.getLineWidth(finalMessage.get(finalMessage.size() - 1).toString()) + whiteSpaceSize;

        for(;index < words.length; ++index) {
            accumulated += font.getLineWidth(words[index]) + whiteSpaceSize;
            if(accumulated > maxChatWidth) {
                // If the word itself is too big
                if(font.getLineWidth(words[index]) > maxChatWidth) {

                    int wordIndex = 0;
                    while(wordIndex < words[index].length()) {
                        substring = words[index].substring(wordIndex, words[index].length());
                        float size = font.getLineWidth(substring) + dashSpaceSize;
                        while(size > maxChatWidth) {
                            substring = substring.substring(0, substring.length()/2);
                            size = font.getLineWidth(substring) + dashSpaceSize;
                        }

                        // < because we have to add the "-" character at the end of that word
                        int wordEnd = wordIndex + substring.length();
                        while(size < maxChatWidth && wordEnd < words[index].length()) {
                            substring = words[index].substring(wordIndex, wordEnd++);
                            size = font.getLineWidth(substring) + dashSpaceSize;
                        }

                        wordIndex += substring.length();

                        current = finalMessage.get(finalMessage.size() - 1);
                        if(current.length() != 0) {
                            current = new StringBuilder(substring);
                            finalMessage.add(current);
                        } else current.append(substring);

                        if(wordIndex < words[index].length()) current.append("-");
                        else current.append(" ");
                    }

                    return splitTextIntoLines2(font, maxChatWidth, finalMessage, words, index+1);

                }
                else {
                    accumulated = font.getLineWidth(words[index]) + whiteSpaceSize;
                    finalMessage.add(new StringBuilder(""));
                }
            }

            current = finalMessage.get(finalMessage.size() - 1).append(words[index]);
            if(index < words.length - 1) current.append(" ");
        }
        return finalMessage;
    }


//    public static void enablePhysicsFor(Spatial spatial) {
//        spatial.depthFirstTraversal(new SceneGraphVisitor() {
//
//            @Override
//            public void visit(Spatial spatial) {
//                RigidBodyControl rigid = spatial.getControl(RigidBodyControl.class);
//                if(rigid != null) {
//                    //                    rigid.setPhysicsLocation(new Vector3f(0, 2, 0));
//                    rigid.setEnabled(true);
//                    rigid.activate();
//                }
//            }
//        });
//    }
//
//    public static void disablePhysicsFor(Spatial spatial) {
//        spatial.depthFirstTraversal(new SceneGraphVisitor() {
//
//            @Override
//            public void visit(Spatial spatial) {
//                RigidBodyControl rigid = spatial.getControl(RigidBodyControl.class);
//                if(rigid != null) {
////                    rigid.clearForces();
////                    rigid.setApplyPhysicsLocal(true);
//                    rigid.setEnabled(false);
//                }
//            }
//        });
//    }

    public static boolean hasGeometry(Spatial spatial) {
        if(spatial instanceof Node) {
            for(Spatial s : ((Node) spatial).getChildren()) {
                if(hasGeometry(s)) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    public static Spatial getDirectChild(Node node, String childName) {
        for(Spatial child : ((SafeArrayList<Spatial>)node.getChildren()).getArray()) {
            if(childName.equals(child.getName())) {
                return child;
            }
        }

        return null;
    }



    public static void offsetMesh(Mesh mesh, Vector3f offset) {
        FloatBuffer buffer = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        for(int i = 0; i < buffer.capacity(); i+=3) {
            buffer.put(i, buffer.get(i) + offset.x);
            buffer.put(i+1, buffer.get(i+1) + offset.y);
            buffer.put(i+2, buffer.get(i+2) + offset.z);
        }

        mesh.updateBound();
    }


    public static int collidesWithBounds(Collidable other, CollisionResults results, Spatial spatial) {
        BoundingVolume bv = spatial.getWorldBound();
        if(bv == null) {
            return 0;
        }

        int total = bv.collideWith(other, results);
        if(total == 0) {
            return total;
        }

        if(spatial instanceof Node) {
            for(Spatial child : ((SafeArrayList<Spatial>)((Node) spatial).getChildren()).getArray()) {
                total += collidesWithBounds(other, results, child);
            }
        } else {
            int size = results.size();
            for (int i = size - total; i < size; i++) {
                results.getCollisionDirect(i).setGeometry((Geometry) spatial);
            }
        }

        return total;
    }

    public static int collidesWithGeometryBounds(Collidable other, CollisionResults geometryResults, Spatial spatial) {
        CollisionResults allResults = new CollisionResults();
        if(collidesWithBounds(other, allResults, spatial) != 0) {
            toGeometryResults(allResults, geometryResults);
        }

        return geometryResults.size();
    }

    public static CollisionResults toGeometryResults(CollisionResults allResults, CollisionResults geometryResults) {
        if(geometryResults == null) {
            geometryResults = new CollisionResults();
        }

        for(CollisionResult result : allResults) {
            if(result.getGeometry() != null) {
                geometryResults.addCollision(result);
            }
        }

        return geometryResults;
    }


    public static boolean meshEquals(Mesh mesh1, Mesh mesh2) {
        if(mesh1 == mesh2) {
            return true;
        }

        if(mesh1 == null || mesh2 == null) {
            return false;
        }

        if(mesh1.getVertexCount() != mesh2.getVertexCount()) {
            return false;
        }

        if(mesh1.getTriangleCount() != mesh2.getTriangleCount()) {
            return false;
        }

        if(mesh1.getMode() != mesh2.getMode()) {
            return false;
        }

        Collection<VertexBuffer> buffers1 = mesh1.getBufferList();
        Collection<VertexBuffer> buffers2 = mesh2.getBufferList();

        if(buffers1.size() != buffers2.size()) {
            return false;
        }

//        outer:
        for(VertexBuffer vertexBuffer1 : buffers1) {
            VertexBuffer vertexBuffer2 = mesh2.getBuffer(vertexBuffer1.getBufferType());
            if(vertexBuffer2 == null) {
                return false;
            }
//            for(VertexBuffer vertexBuffer2 : buffers2) {
//                if(vertexBuffer1.getBufferType() == vertexBuffer2.getBufferType()) {
                    if(vertexBuffer1.getFormat() != vertexBuffer2.getFormat()) {
                        return false;
                    }

                    if(vertexBuffer1.getUsage() != vertexBuffer2.getUsage()) {
                        return false;
                    }

                    if(vertexBuffer1.getNumComponents() != vertexBuffer2.getNumComponents()) {
                        return false;
                    }

                    Buffer data1 = vertexBuffer1.getData();
                    Buffer data2 = vertexBuffer2.getData();

                    if(data1 == null || data2 == null)  {
                        if((data1 == null ? 0 : data1.capacity()) != (data2 == null ? 0 : data2.capacity())) {
                            return false;
                        }

                        continue;
                    }

//                    if(vertexBuffer1.getNumElements() != vertexBuffer2.getNumElements()) {
//                        return false;
//                    }
                    if(data1.capacity() != data2.capacity()) {
                        return false;
                    }



                    if (data1 instanceof FloatBuffer) {
                        FloatBuffer buf1 = (FloatBuffer) data1;
                        FloatBuffer buf2 = (FloatBuffer) data2;

                        int capacity = buf1.capacity();
                        for(int i = 0; i < capacity; i++) {
                            if(buf1.get(i) != buf2.get(i)) {
                                return false;
                            }
                        }
                    } else if (data1 instanceof ShortBuffer) {
                        ShortBuffer buf1 = (ShortBuffer) data1;
                        ShortBuffer buf2 = (ShortBuffer) data2;

                        int capacity = buf1.capacity();
                        for(int i = 0; i < capacity; i++) {
                            if(buf1.get(i) != buf2.get(i)) {
                                return false;
                            }
                        }
                    } else if (data1 instanceof ByteBuffer) {
                        ByteBuffer buf1 = (ByteBuffer) data1;
                        ByteBuffer buf2 = (ByteBuffer) data2;

                        int capacity = buf1.capacity();
                        for(int i = 0; i < capacity; i++) {
                            if(buf1.get(i) != buf2.get(i)) {
                                return false;
                            }
                        }
                    } else if (data1 instanceof IntBuffer) {
                        IntBuffer buf1 = (IntBuffer) data1;
                        IntBuffer buf2 = (IntBuffer) data2;

                        int capacity = buf1.capacity();
                        for(int i = 0; i < capacity; i++) {
                            if(buf1.get(i) != buf2.get(i)) {
                                return false;
                            }
                        }
                    } else if (data1 instanceof DoubleBuffer) {
                        DoubleBuffer buf1 = (DoubleBuffer) data1;
                        DoubleBuffer buf2 = (DoubleBuffer) data2;

                        int capacity = buf1.capacity();
                        for(int i = 0; i < capacity; i++) {
                            if(buf1.get(i) != buf2.get(i)) {
                                return false;
                            }
                        }
                    } else {
                        throw new UnsupportedOperationException();
                    }
//                    continue outer;
//                }
//            }

//            return false;
        }

        return true;
    }

    public static int meshBuffersHash(Mesh mesh) {
        int hash = 0;

        for (VertexBuffer vb : mesh.getBufferList().getArray()) {
            hash += vb.hashCode();
        }

        return hash;
    }

    public static boolean meshShareBuffers(Mesh mesh1, Mesh mesh2) {
        if(mesh1 == mesh2) {
            return true;
        }

        if(mesh1 == null || mesh2 == null) {
            return false;
        }

        for (VertexBuffer vb : mesh1.getBufferList().getArray()) {
            if(mesh2.getBuffer(vb.getBufferType()) != vb) {
                return false;
            }
        }

        return true;
    }

    public static boolean meshShareBuffers(Mesh mesh1, Mesh mesh2, Mesh... meshes) {
        if(!meshShareBuffers(mesh1, mesh2)) {
            return false;
        }

        VertexBuffer[] buffers = mesh1.getBufferList().getArray();
        for(Mesh m : meshes) {
            if(m == mesh1) {
                return true;
            }

            if(m == null) {
                return false;
            }

            for (VertexBuffer vb : buffers) {
                if(m.getBuffer(vb.getBufferType()) != vb) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Based on Ali_RS original code: {@link <a href="https://hub.jmonkeyengine.org/t/instancednode-noob-questions/34931/">}
     *
     * Currently this breaks the material somehow. So, maybe a new material should be created
     * @param spatial
     */
    public static void enableMaterialInstancing(Spatial spatial) {
        if(spatial instanceof Geometry) {
            enableMaterialInstancing(((Geometry) spatial).getMaterial());
            spatial.setBatchHint(Spatial.BatchHint.Inherit);
        } else if (spatial instanceof Node) {
            for (Spatial child : ((SafeArrayList<Spatial>)((Node) spatial).getChildren()).getArray()) {
                if (child instanceof GeometryGroupNode) {
                    continue;
                }

                enableMaterialInstancing(child);
            }
        }
    }

    public static void disableMaterialInstancing(Spatial spatial) {
        if(spatial instanceof Geometry) {
            disableMaterialInstancing(((Geometry) spatial).getMaterial());
            spatial.setBatchHint(Spatial.BatchHint.Inherit);
        } else if (spatial instanceof Node) {
            for (Spatial child : ((SafeArrayList<Spatial>)((Node) spatial).getChildren()).getArray()) {
                if (child instanceof GeometryGroupNode) {
                    continue;
                }

                disableMaterialInstancing(child);
            }
        }
    }

    public static boolean isMaterialInstancingEnabled(Material material) {
        MatParam matParam = material.getParam("UseInstancing");

        return matParam != null && (Boolean) matParam.getValue();
//        MatParam matParam;
//        return material.getMaterialDef().getMaterialParam("UseInstancing") != null
//                && (matParam = material.getParam("UseInstancing")) != null
//                && (Boolean) matParam.getValue();
    }

    public static void enableMaterialInstancing(Material material) {
        MaterialDef matDef = material.getMaterialDef();

        if(matDef.getMaterialParam("UseInstancing") == null) {
            matDef.addMaterialParam(VarType.Boolean, "UseInstancing", false);
        }

        material.setBoolean("UseInstancing", true);
    }

    public static void disableMaterialInstancing(Material material) {
        MaterialDef matDef = material.getMaterialDef();

        if(matDef.getMaterialParam("UseInstancing") != null) {
            material.setBoolean("UseInstancing", false);
        }
    }

    public static void setMaterialInstancing(Material material, boolean enabled) {
        if(enabled) {
            enableMaterialInstancing(material);
        } else {
            disableMaterialInstancing(material);
        }
    }

    public static void setMaterialInstancing(Spatial spatial, boolean enabled) {
        if(enabled) {
            enableMaterialInstancing(spatial);
        } else {

        }
    }

    public static void addPositionToVertexBuffer(Vector3f position, Mesh mesh, VertexBuffer.Type bufferType) {
        if(bufferType == null) {
            bufferType = VertexBuffer.Type.TexCoord8;
        }

        int elements = mesh.getVertexCount();
        int components = 3;

        float[] bufferData = new float[elements * components];

        for(int i = 0; i < elements; i+=3) {
            bufferData[i] = position.x;
            bufferData[i+1] = position.y;
            bufferData[i+2] = position.z;
        }

        mesh.setBuffer(bufferType, components, bufferData);
    }

    public static Boolean getBooleanFromData(Object userData, Boolean defaultValue) {
        if(userData == null) {
            return defaultValue;
        }

        if(userData instanceof Boolean) {
            return (Boolean) userData;
        } else {
            return getFloatFromData(userData, null) > 0;
        }
    }

    public static Float getFloatFromData(Object userData, Float defaultValue) {
        if(userData == null) {
            return defaultValue;
        }

        if(userData instanceof Float) {
            return (Float) userData;
        } else if(userData instanceof Integer) {
            return ((Integer) userData).floatValue();
        } else if(userData instanceof Short) {
            return ((Short) userData).floatValue();
        } else if(userData instanceof Byte) {
            return ((Byte) userData).floatValue();
        } else if(userData instanceof Double) {
            return ((Double) userData).floatValue();
        }

        throw new IllegalArgumentException();
    }

    public static Integer getIntegerFromData(Object userData, Integer defaultValue) {
        if(userData == null) {
            return defaultValue;
        }

        if(userData instanceof Integer) {
            return (Integer) userData;
        } else if(userData instanceof Float) {
            return ((Float) userData).intValue();
        } else if(userData instanceof Short) {
            return ((Short) userData).intValue();
        } else if(userData instanceof Byte) {
            return ((Byte) userData).intValue();
        } else if(userData instanceof Double) {
            return ((Double) userData).intValue();
        }

        throw new IllegalArgumentException();
    }

    public static void copyMaterialParams(Material fromMaterial, Material toMaterial) {
        for(MatParam fromParam: fromMaterial.getParams()) {
            MatParam toParam = toMaterial.getMaterialDef().getMaterialParam(fromParam.getName());

            if(toParam != null) {
                toMaterial.setParam(fromParam.getName(), fromParam.getVarType(), fromParam.getValue());
            }
        }
    }




    // --- START TEXCOORDS AREA ---- //



    public static float[] getTexCoordsForAtlas(Vector4f coords, Image image) {
        return getTexCoordsForAtlas(coords, image.getWidth(), image.getHeight());
    }

    public static float[] getTexCoordsForAtlas(Vector4f coords, float imageWidth, float imageHeight) {
        float x0 = coords.x / imageWidth;
        float y1 = (imageHeight - coords.y) / imageHeight;
        float x1 = coords.z / imageWidth;
        float y0 = (imageHeight - coords.w) / imageHeight;

        return new float[] {
                x0, y0,
                x1, y0,
                x1, y1,
                x0, y1
        };
    }

    public static void setTexCoordsForAtlas(Mesh mesh, Vector4f coords, Image image) {
        setTexCoordsForAtlas(mesh, null, coords, image.getWidth(), image.getHeight());
    }

    public static void setTexCoordsForAtlas(Mesh mesh, Vector4f coords, float imageWidth, float imageHeight) {
        setTexCoordsForAtlas(mesh, null, coords, imageWidth, imageHeight);
    }

    public static void setTexCoordsForAtlas(Mesh mesh, VertexBuffer.Type type, Vector4f coords, Image image) {
        setTexCoordsForAtlas(mesh, type, coords, image.getWidth(), image.getHeight());
    }

    public static void setTexCoordsForAtlas(Mesh mesh, VertexBuffer.Type type, Vector4f coords, float imageWidth, float imageHeight) {
        if(type == null) {
            type = VertexBuffer.Type.TexCoord;
        }

        mesh.setBuffer(type, 2, getTexCoordsForAtlas(coords, imageWidth, imageHeight));
    }

    public static void setTexCoordsForAtlas(Geometry geometry, Texture texture, Vector4f texCoords) {
        Image image = texture.getImage();
        setTexCoordsForAtlas(geometry.getMesh(), null, texCoords, image.getWidth(), image.getHeight());
    }


    // --- END TEXCOORDS AREA ---- //


//    /**
//     * TODO: Is this even right?, Why not use just the Spatial.worldToLocal?. Where is this being used?
//     *
//     * @deprecated Use Spatial.worldToLocal instead
//     */
//    @Deprecated
//    public static void setWorldTranslation(Spatial spatial, Vector3f translation) {
//        Vector3f worldTranslation = Vector3f.ZERO;
//        Vector3f worldScale = Vector3f.UNIT_XYZ;
//
//        Spatial parent = spatial.getParent();
//        if(parent != null) {
//            worldTranslation = parent.getWorldTranslation();
//            worldScale = parent.getWorldScale();
//        }
//
//        spatial.setLocalTranslation(
//                spatial.getLocalTranslation().set(
//                        // spatial.getWorldTranslation is being used as an aux.
//                        spatial.getWorldTranslation().set(Vector3f.UNIT_XYZ).divideLocal(worldScale).multLocal(translation).subtractLocal(worldTranslation)
//                )
//        );
//    }

    public static void setWorldTranslation(Spatial spatial, Vector3f translation) {
        Node parent = spatial.getParent();
        if(parent == null) {
            spatial.setLocalTranslation(translation);
        } else {
            spatial.setLocalTranslation(parent.worldToLocal(translation, spatial.getLocalTranslation()));
        }
    }

    public static Vector3f getBoneWorldTranslation(Spatial spatial, String boneName, Vector3f store) {
        return getBoneWorldTranslation(SpatialUtil.getFirstControlFor(spatial, SkeletonControl.class), boneName, store);
    }

    public static Vector3f getBoneWorldTranslation(SkeletonControl skeletonControl, String boneName, Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        } else {
            store.set(Vector3f.ZERO);
        }

        Skeleton skeleton = skeletonControl.getSkeleton();
        Bone bone = skeleton.getBone(boneName);

        Spatial spatial = skeletonControl.getSpatial();

        if(bone != null) {
            store.set(bone.getModelSpacePosition()).multLocal(spatial.getWorldScale());
            spatial.getWorldRotation().multLocal(store);
        }

        store.addLocal(spatial.getWorldTranslation());

        return store;
    }

    public static void setPositionFromBone(Spatial toSpatial, Spatial fromSpatial, String fromBone) {
        Vector3f position = toSpatial.getLocalTranslation();
        getBoneWorldTranslation(fromSpatial, fromBone, position);
        toSpatial.setLocalTranslation(position);
    }


    public static Control createControlFollow(Quaternion followRotation) {
        return new FollowRotationControl(followRotation);
    }

    public static Control createControlFollow(Vector3f followPosition, Quaternion followRotation) {
        return new FollowFullControl(followPosition, followRotation);
    }

    public static Control createControlFollow(Vector3f followPosition) {
        return new FollowControl(followPosition);
    }

    public static Control createControlFollowOffset(Vector3f followPosition, Vector3f offset) {
        return new FollowOffsetControl(followPosition, offset);
    }

    public static Control createControlTimer(float time) {
        return new TimerControl(time);
    }

    public static Control createControlTimerLeave(float time) {
        return new TimerLeaveControl(time);
    }

    public static class FollowRotationControl extends AbstractControl {

        protected Quaternion followRotation;

        public FollowRotationControl(Quaternion followRotation) {
            this.followRotation = followRotation;
        }

        @Override
        protected void controlUpdate(float tpf) {
            spatial.setLocalRotation(followRotation);
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) { }
    }

    public static class FollowFullControl extends AbstractControl {

        protected Vector3f followPosition;
        protected Quaternion followRotation;

        public FollowFullControl(Vector3f followPosition, Quaternion followRotation) {
            this.followPosition = followPosition;
            this.followRotation = followRotation;
        }

        @Override
        protected void controlUpdate(float tpf) {
            spatial.setLocalTranslation(followPosition);
            spatial.setLocalRotation(followRotation);
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) { }
    }

    public static class FollowControl extends AbstractControl {

        protected Vector3f followPosition;

        public FollowControl(Vector3f followPosition) {
            this.followPosition = followPosition;
        }

        @Override
        protected void controlUpdate(float tpf) {
            spatial.setLocalTranslation(followPosition);
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) { }
    }

    //TODO: Move to SpatialUtil
    public static class FollowOffsetControl extends FollowControl {

        protected Vector3f offset;

        public FollowOffsetControl(Vector3f followPosition, Vector3f offset) {
            super(followPosition);

            this.offset = offset;
        }

        @Override
        protected void controlUpdate(float tpf) {
            spatial.setLocalTranslation(followPosition.add(offset, spatial.getLocalTranslation()));
        }
    }

    public static class TimerControl extends AbstractControl {

        float time;
        float elapsed;

        public TimerControl() {
        }

        public TimerControl(float time) {
            this.time = time;
        }

        public void resetTime() {
            elapsed = 0;
        }

        public float getTime() {
            return time;
        }

        public void setTime(float time) {
            this.time = time;
        }

        @Override
        protected void controlUpdate(float tpf) {
            elapsed += tpf;
            if(elapsed >= time) {
                onTimeCompleted();
                resetTime();
            }
        }

        protected void onTimeCompleted() {

        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) { }
    }

    public static class TimerLeaveControl extends TimerControl {
        public TimerLeaveControl() {
            super();
        }

        public TimerLeaveControl(float time) {
            super(time);
        }

        @Override
        protected void onTimeCompleted() {
            this.spatial.removeFromParent();
        }
    }





    // Buffers
    public static void offsetBuffer(Geometry geometry, VertexBuffer.Type bufferType, float... values) {
        offsetBuffer(geometry.getMesh(), bufferType, values);
    }

    public static void offsetBuffer(Mesh mesh, VertexBuffer.Type bufferType, float... values) {
        Objects.requireNonNull(values);

        VertexBuffer vb = mesh.getBuffer(bufferType);
        FloatBuffer floatBuffer = (FloatBuffer) vb.getData();
        floatBuffer.rewind();

        vb.setUpdateNeeded();

        switch (bufferType) {
            case TexCoord:
            case TexCoord2:
            case TexCoord3:
            case TexCoord4:
            case TexCoord5:
            case TexCoord6:
            case TexCoord7:
            case TexCoord8:
                offsetTexcoordBuffer(mesh, bufferType, values);
                return;
        }

        int limit = floatBuffer.limit();
        int steps = vb.getNumComponents() - values.length;

        for (int i = 0; i < limit; i += steps) {
            for(float value : values) {
                floatBuffer.put(i, floatBuffer.get(i++) + value);
            }
        }
    }

    public static void offsetTexcoordBuffer(FloatBuffer buffer, float... values) {
        if(values.length == 1) {
            for (int i = 0; i < buffer.capacity(); i++) {
                buffer.put(i, buffer.get(i) + values[0]);
            }
        } else if(values.length == 2) {
            for (int i = 0; i < buffer.capacity(); i++) {
                buffer.put(i, buffer.get(i) + values[0]);
                buffer.put(i, buffer.get(i++) + values[1]);
            }
        } else {
            throw new IllegalArgumentException("Texcoord has 2 components. Give between 0 and 1 of them.");
        }
    }



    public static void offsetTexcoordBuffer(Mesh mesh, VertexBuffer.Type bufferType, float... values) {
        offsetTexcoordBuffer(mesh.getFloatBuffer(bufferType), values);
    }




    // Textures
    public static Vector4f getTexCoordsFromAtlasTile(TextureAtlas.TextureAtlasTile atlasTile, Vector4f store) {
        if(store == null) {
            store = new Vector4f();
        }

        float x = atlasTile.getX();
        float y = atlasTile.getY();

        return store.set(x, y, x + atlasTile.getWidth(), y + atlasTile.getHeight());
    }

    public static Spatial getRootFor(Spatial spatial) {
        Spatial rootParent = spatial;
        for(Node parent = spatial.getParent(); parent != null; parent = parent.getParent()) {
            rootParent = parent;
        }

        return rootParent;
    }


    public static <T extends Control> boolean moveFirstControl(Spatial from, Spatial to, Class<T> clazz) {
        Control control = getFirstControlFor(from, clazz);
        if(control == null) {
            return false;
        }

        throw new UnsupportedOperationException();
        //TODO
    }

    public static Vector3f getCenter(Spatial spatial, Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        }

        BoundingBox bb = (BoundingBox) spatial.getWorldBound();

        if(bb == null) {
            throw new IllegalStateException();
        }

        bb.getCenter(store);

        return store;
    }

    public static boolean hasMatParamSet(String param, Geometry geometry) {
        return hasMatParamSet(param, geometry.getMaterial());
    }

    public static boolean hasMatParam(String param, Geometry geometry) {
        return hasMatParam(param, geometry.getMaterial());
    }

    public static boolean hasMatParam(String param, Material material) {
        return material.getMaterialDef().getMaterialParam(param) != null;
    }

    public static boolean hasMatParamSet(String param, Material material) {
        return hasMatParam(param, material) && material.getParam(param) != null;
    }

    public static List<Animation> getAnimationsFrom(AnimControl animControl) {
        return animControl.getAnimationNames().stream().map(animControl::getAnim).collect(Collectors.toList());
    }

    public static AnimControl createAnimControlFrom(Skeleton skeleton, Collection<Animation> animations) {

        AnimControl animControl = new AnimControl(skeleton);
        animations.forEach(animControl::addAnim);

        return animControl;
    }
}

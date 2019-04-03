package com.nx.util.jme3.debug;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.*;
import com.jme3.scene.instancing.InstancedNode;
import com.jme3.util.TangentBinormalGenerator;
import com.nx.util.jme3.base.DebugUtil;
import com.nx.util.jme3.base.SpatialUtil;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * //TODO: Currently it can have strange effects in the scene if their geometries materials (or the materials renderstates) are changed externally once the debug is active.
 * //TODO: Solution to this: create a dupped scene and attach it to the debugNode (and set the cullhint for the uppernodes to always)
 * //TODO: Fix a bug with instanced not going back to it original material
 * Created by NemesisMate on 1/12/16.
 */
public class DebugMeshState extends AbstractThreadedDebugGraphStateModule {

    private boolean colors;
    private boolean wire;
    private boolean faceCullOff;
    private boolean normals;
//    private boolean neverCull;

    private boolean groupedGeometries;
    private boolean sharedMeshes;

//    Node selected;
//    AssetManager assetManager;


//    boolean usingOriginalMats;
    private Map<Geometry, Material> originalMats;
    private Map<Geometry, Material> clonedMats;
    private Map<Material, RenderState> originalRenderStates;

//    private Map<Integer, SharedMesh> meshesShared;
////    private Map<Mesh, Material> meshesMaterials;
//    private class SharedMesh {
//        List<Mesh> meshes = new ArrayList<>(1);
//        List<Material> materials = new ArrayList<>(1);
//
//        public SharedMesh(Mesh mesh, Material material) {
//            add(mesh, material);
//        }
//
//        public void add(Mesh mesh, Material material) {
//            meshes.add(mesh);
//            materials.add(material);
//        }
//
//        public void remove(Mesh mesh) {
//            int index = meshes.indexOf(mesh);
//
//            if(index >= 0) {
//                meshes.remove(index);
//                materials.remove(index);
//            }
//        }
//    }





    private Map<Mesh, Material> meshesShared;
    private Map<Spatial, Material> groupedMaterials;

    private Map<GeometryGroupNode, ColorRGBA> groupColors;


    public DebugMeshState(boolean colors, boolean wire, boolean normals, boolean faceCullOff) {
        this(colors, wire, normals, faceCullOff, false, false);
//        this.neverCull = neverCull;

        // This mode DOESN'T respect the original shaders and thus, the vertex movement done by these.
//        if(isUsingGeneratedMats()) {

//        } else {
//            originalRenderStates = new HashMap<>();
//        }
    }

    public DebugMeshState(boolean colors, boolean wire, boolean normals, boolean faceCullOff, boolean sharedMeshes, boolean groupedGeometries) {
        this.colors = colors;
        this.wire = wire;
        this.normals = normals;
        this.faceCullOff = faceCullOff;
        this.sharedMeshes = sharedMeshes;
        this.groupedGeometries = groupedGeometries;
//        this.neverCull = neverCull;
        // This mode DOESN'T respect the original shaders and thus, the vertex movement done by these.
//        if(isUsingGeneratedMats()) {

//        } else {
//            originalRenderStates = new HashMap<>();
//        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        originalMats = new HashMap<>();
        clonedMats = new HashMap<>();

        if(sharedMeshes) {
            meshesShared = new HashMap<>();
        }

        if(groupedGeometries) {
            groupedMaterials = new HashMap<>();

            groupColors = new HashMap<>();
        }
    }

    //    @Override
//    public void initialize(AppStateManager stateManager, Application app) {
//        super.initialize(stateManager, app);
//
////        DebugState debugState = stateManager.getState(DebugState.class);
//
////        selected = debugState.getRootNode();
////        assetManager = debugState.getAssetManager();
////        originalMats = new HashMap<>();
//    }

    @Override
    public void cleanup() {
        super.cleanup();

//        if(isUsingGeneratedMats()) {

            for(Map.Entry<Geometry, Material> entry : originalMats.entrySet()) {
                if(!entry.getKey().isGrouped()) {
                    entry.getKey().setMaterial(entry.getValue());
                }
            }
//
//        } else {
//            for(Map.Entry<Material, RenderState> entry : originalRenderStates.entrySet()) {
//                entry.getKey().getAdditionalRenderState().set(entry.getValue());
//            }
//
//        }

        originalMats = null;
        clonedMats = null;
        meshesShared = null;
        groupColors = null;
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

        Material material = geometry.getMaterial();

        Material clonedMat = clonedMats.get(geometry);
        if(clonedMat != null) {
            if(!clonedMat.contentEquals(material)) {
                //TODO: solve this case
                String errorMessage = "Something weird could be happened. DO NOT change any material while mesh debug is enabled.";
                LoggerFactory.getLogger(this.getClass()).error(errorMessage);
                throw new UnsupportedOperationException(errorMessage);
            }

            return true;
        }

        Material m = null;
        if(groupedGeometries) {
            m = groupedMaterials.get(geometry);
        }

        if(m == null && sharedMeshes) {
            m = getSharedMeshMaterial(geometry.getMesh()); //meshesInstances.get(SpatialUtil.meshBuffersHash(geometry.getMesh()));
        }

        if(m == null) {

            GeometryGroupNode groupNode = null;
            gr:
            if(groupedGeometries) {
                Node parent = geometry.getParent();
                while(parent != null) {
                    if(parent instanceof GeometryGroupNode) {
//                        debugedSpatial = parent;

                        groupNode = (GeometryGroupNode) parent;
                        break gr;
                    }
                    parent = parent.getParent();
                }

                return true;
            }

            m = getDebugMaterial(geometry, colors, wire, normals, faceCullOff);

            if(sharedMeshes) {
                putSharedMeshMaterial(geometry.getMesh(), m);
            }

            if(groupedGeometries) {

                String paramName = "Color";
                ColorRGBA color;
                if(m.getMaterialDef().getMaterialParam("Diffuse") != null) {
                    paramName = "Diffuse";
                }

                if(groupNode instanceof InstancedNode) {
                    SpatialUtil.enableMaterialInstancing(m);
                    color = ColorRGBA.Blue;
                } else {
                    color = ColorRGBA.Green;
                }

                ColorRGBA c = groupColors.get(groupNode);
                if(c == null) {
                    c = color.mult(FastMath.nextRandomFloat());
                }

                m.setColor(paramName, c);


                groupedMaterials.put(geometry, m);
            }



        }

        final Material newMaterial = m;


//        if(isUsingGeneratedMats()) {
////            if(originalMats.containsKey(geom)) {
////                return true;
////            }
//
////            originalMats.put(geom, geom.getMaterial());
//
//            newMaterial = DebugUtil.createDebugMaterial(geometry, colors);
//
////            app.enqueue(new Callable<Void>() {
////                @Override
////                public Void call() throws Exception {
////                    geom.setMaterial(newMaterial);
////
////                    return null;
////                }
////            });
//
//
//        } else {
////            Material material = geom.getMaterial();
////            if(originalRenderStates.containsKey(material)) {
////                return true;
////            }
//
//            newMaterial = material.clone();
//            RenderState renderState = newMaterial.getAdditionalRenderState();
//
//
//
//            renderState.setWireframe(!renderState.isWireframe());
//
//
////            RenderState renderState = material.getAdditionalRenderState();
//
////            originalRenderStates.put(material, renderState.clone());
//
////            app.enqueue(new Callable<Void>() {
////                @Override
////                public Void call() throws Exception {
////
////                    if(faceCullOff) {
////                        renderState.setFaceCullMode(RenderState.FaceCullMode.Off);
////                    }
////
////                    renderState.setWireframe(!renderState.isWireframe());
////
////                    return null;
////                }
////            });
//        }
//
//        RenderState renderState = newMaterial.getAdditionalRenderState();
//
//        if(faceCullOff) {
//            renderState.setFaceCullMode(RenderState.FaceCullMode.Off);
//        }
//        if(wire || !colors) {
//            renderState.setWireframe(!renderState.isWireframe());
//        }



        originalMats.put(geometry, material);
        clonedMats.put(geometry, newMaterial.clone());

        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if(geometry.getMesh().getBuffer(VertexBuffer.Type.Normal) != null && geometry.getMesh().getBuffer(VertexBuffer.Type.Tangent) == null) {
                    TangentBinormalGenerator.generate(geometry);
                }


                geometry.setMaterial(newMaterial);

                return null;
            }
        });


        return false;
    }

    private Material getSharedMeshMaterial(Mesh mesh) {
        return meshesShared.get(mesh);
//        SharedMesh sharedMesh = meshesShared.get(SpatialUtil.meshBuffersHash(mesh));
//
//        if(sharedMesh != null) {
//            int i = 0;
//            for (Mesh m : sharedMesh.meshes) {
//                if (SpatialUtil.meshShareBuffers(mesh, m)) {
//                    return sharedMesh.materials.get(i);
//                }
//            }
//
//            LoggerFactory.getLogger(this.getClass()).debug("OKOKOKOK: " + mesh);
//        }
//
//        return null;
    }

    private void putSharedMeshMaterial(Mesh mesh, Material material) {
        meshesShared.put(mesh, material);
//        int buffersHash = SpatialUtil.meshBuffersHash(mesh);
//
//        LoggerFactory.getLogger(this.getClass()).debug("NONONO: " + buffersHash);
//        SharedMesh sharedMesh = meshesShared.get(buffersHash);
//        if(sharedMesh == null) {
//            meshesShared.put(buffersHash, new SharedMesh(mesh, material));
//        } else {
//            for(Mesh m : sharedMesh.meshes) {
//                if(SpatialUtil.meshShareBuffers(mesh, m)) {
//                    return;
//                }
//            }
//
//            sharedMesh.add(mesh, material);
//        }
    }


    private void checkTexCoord() {
        //TODO: use a colorgrid texture (1024x1024?) or just color each pixel with it texCoord value as color in the shader.

    }

    protected static Material getDebugMaterial(Geometry geometry, boolean colors, boolean wire, boolean normals, boolean faceCullOff) {
//        RenderState renderState = geometry.getMaterial().getAdditionalRenderState();

        Material debugMaterial;
        Material originalMaterial = geometry.getMaterial();
        if(normals) {
            debugMaterial = DebugUtil.createNormalMaterial(geometry);
        }  else if(colors || !wire || originalMaterial == null) {
            debugMaterial = DebugUtil.createDebugMaterial(geometry, colors);
        } else {
            debugMaterial = geometry.getMaterial().clone();
            wire = true;
        }

        RenderState debugRenderState = debugMaterial.getAdditionalRenderState();

        if(wire) {
            debugRenderState.setWireframe(originalMaterial == null || !originalMaterial.getAdditionalRenderState().isWireframe());
        }

        if(faceCullOff) {
            debugRenderState.setFaceCullMode(RenderState.FaceCullMode.Off);
        }

        return debugMaterial;
    }

//    @Override
//    protected Collection createCache() {
////        return new HashSet<>();
//        return originalMats != null ? originalMats.keySet() : super.createCache();
//    }

//    private boolean isUsingGeneratedMats() {
//        return colors || !wire;
//    }
}

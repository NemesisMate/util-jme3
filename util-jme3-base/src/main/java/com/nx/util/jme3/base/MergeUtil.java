package com.nx.util.jme3.base;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.BoneTrack;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.animation.Track;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;
import com.jme3.util.TempVars;
import jme3tools.optimize.GeometryBatchFactory;
import jme3tools.optimize.TextureAtlas;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * //TODO: improvement: remove vertexes dups.
 * //TODO: Efficiency: change all put(index, value) by put(value). It's faster.
 * @author NemesisMate
 */
public final class MergeUtil {

    private static int mergedCount;

    private MergeUtil() {

    }

    public static boolean createSkeletonBuffers(Mesh mesh, byte boneIndex) {

        // If the skeleton buffer is null, we create a new one
        if(mesh.getBuffer(VertexBuffer.Type.BoneIndex) == null) {

            //TODO: Use the BufferUtils clone utilities instead of manually copying
            VertexBuffer indexBuffer = mesh.getBuffer(VertexBuffer.Type.Index);
            int bufferComponents = 4;
            int bufferSize = indexBuffer.getNumElements() * bufferComponents;
            byte[] zeroIndex = new byte[bufferSize];
            float[] zeroWeight = new float[indexBuffer.getNumElements() * bufferComponents];

            if(boneIndex != 0) {
                for(int i = 0; i < bufferSize; i += bufferComponents) {
                    zeroIndex[i] = boneIndex;
                    zeroWeight[i] = 1;
                }
            }

            mesh.setBuffer(VertexBuffer.Type.BoneIndex, bufferComponents, zeroIndex);
            mesh.setBuffer(VertexBuffer.Type.BoneWeight, bufferComponents, zeroWeight);

//            mesh.setBuffer(VertexBuffer.Type.HWBoneIndex, bufferComponents, zeroIndex);
//            mesh.setBuffer(VertexBuffer.Type.HWBoneWeight, bufferComponents, zeroWeight);


            //TODO: Use the BufferUtils clone utilities instead of manually copying.
            VertexBuffer posBuffer = mesh.getBuffer(VertexBuffer.Type.Position);

            bufferComponents = posBuffer.getNumComponents();
            float[] array = new float[indexBuffer.getNumElements() * bufferComponents];

            FloatBuffer floatBuffer = (FloatBuffer) posBuffer.getDataReadOnly();
            int buffSize = bufferComponents * posBuffer.getNumElements();

            for (int k = 0; k < buffSize; k++) {
                array[k] = floatBuffer.get(k);
            }
            // BindPosePosition, according to it docs, has to have the same format and components as the Position buffer.
            mesh.setBuffer(VertexBuffer.Type.BindPosePosition, bufferComponents, array);

            // Why this doesn't work?
//            mesh.setBuffer(VertexBuffer.Type.BindPosePosition, bufferComponents, BufferUtils.clone((FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getDataReadOnly()));


            posBuffer = mesh.getBuffer(VertexBuffer.Type.Normal);

            bufferComponents = posBuffer.getNumComponents();
            array = new float[indexBuffer.getNumElements() * bufferComponents];

            floatBuffer = (FloatBuffer) posBuffer.getDataReadOnly();
            buffSize = bufferComponents * posBuffer.getNumElements();

            for (int k = 0; k < buffSize; k++) {
                array[k] = floatBuffer.get(k);
            }
            // BindPoseNormal, according to it docs, has to have the same format and components as the Normal buffer.
            mesh.setBuffer(VertexBuffer.Type.BindPoseNormal, bufferComponents, array);

            // Why this doesn't work?
//            mesh.setBuffer(VertexBuffer.Type.BindPoseNormal, bufferComponents, BufferUtils.clone((FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Normal).getData()));


            mesh.setMaxNumWeights(4);

            return true;
        }

        return false;
    }

    public static List<Geometry> gatherGeometries(List<Geometry> store, Spatial... spatials) {
        if(store == null) {
            store = new ArrayList<>();
        }

        // Not added to a node to not dettach from their parents and, so, do not mutate the original things.
        for(Spatial spatial : spatials) {
            GeometryBatchFactory.gatherGeoms(spatial, store);
        }

        return store;
    }

    public static boolean checkAtlasNeed(List<Geometry> geometries) {
        Material material = geometries.get(0).getMaterial();
        for(Geometry geometry : geometries) {
            //TODO: If is needed real-time checking should change !equals for !=.
            //TODO: Change !equals by != once the material loading system ensures there is no dup-materials.
            if(!geometry.getMaterial().equals(material)) {
                return true;
            }
        }

        return false;
    }

    public static TextureAtlas createAtlasFor(List<Geometry> geometries, int atlasSize) {
        TextureAtlas atlas = new TextureAtlas(atlasSize, atlasSize);
        for (Geometry geometry : geometries) {
            for(MatParam param : geometry.getMaterial().getParams()) {
                if(param instanceof MatParamTexture) {
                    Texture texture = ((MatParamTexture)param).getTextureValue();
                    if(texture.getKey() == null) {
//                        Logger.getLogger(MergeUtil.class.getName()).log(Level.WARNING, "Texture ({0}) hasn't got an assetKey, creating a generic one (this may lead to unexpected behaviors).", param.getName());
                        LoggerFactory.getLogger(MergeUtil.class).warn("Texture ({}) hasn't got an assetKey, creating a generic one (this may lead to unexpected behaviors).", param.getName());
                        texture.setKey(new TextureKey(geometry.getName() + "_packed_" + param.getName()));
                    }
                }
            }

            if (!atlas.addGeometry(geometry)) {
//                Logger.getLogger(MergeUtil.class.getName()).log(Level.WARNING, "Texture atlas size too small, cannot add all textures.");
                LoggerFactory.getLogger(MergeUtil.class).warn("Texture atlas size too small, cannot add all textures.");
                return null;
            }
        }

        return atlas;
    }

    public static void applyAtlasTo(TextureAtlas atlas, List<Geometry> geometries) {
        for(Geometry geometry : geometries) {
            atlas.applyCoords(geometry);
        }
    }

    public static void applyAtlasTo(TextureAtlas atlas, List<Geometry> geometries, Mesh outMesh) {
        int currentIndex = 0;
        for(Geometry geometry : geometries) {
            atlas.applyCoords(geometry, currentIndex, outMesh);
            currentIndex += geometry.getMesh().getBuffer(VertexBuffer.Type.TexCoord).getNumElements();
        }
    }

    public static Material createAtlasMaterialFor(TextureAtlas atlas, AssetManager assetManager, String matDef) {
        return createAtlasMaterialFor(atlas, (MaterialDef) assetManager.loadAsset(new AssetKey(matDef)));
    }

    public static Material createAtlasMaterialFor(TextureAtlas atlas, AssetManager assetManager) {
        return createAtlasMaterialFor(atlas, assetManager, "Common/MatDefs/Light/Lighting.j3md");
    }

    public static Material createAtlasMaterialFor(TextureAtlas atlas, MaterialDef materialDef) {
        Material material = new Material(materialDef);

        for(MatParam param : materialDef.getMaterialParams()) {
            if(param instanceof MatParamTexture) {
                Texture texture = atlas.getAtlasTexture(param.getName());
                if(texture != null) {
                    material.setTexture(param.getName(), texture);
                }
            }
        }

        return material;
    }


    //If two animations have the same name, merge it tracks (if they aren't the same)
    //Remove repeated bones (with same name) optionally. In this case, if A has bone b1 and b2, and B has b1 and b3, the resultant skeleton should have b1, b2, and b3
    //TODO: optionally allow to attach a full skeleton to a desired bone.
    public static Spatial mergeSpatialsWithAnimations(AssetManager assetManager, boolean replaceBoneDups, Spatial spatial, Map<Geometry, String> boneLinks) {
        List<Geometry> geometries = new ArrayList<Geometry>();

        GeometryBatchFactory.gatherGeoms(spatial, geometries);

        return mergeSpatialsWithAnimations(assetManager, replaceBoneDups, geometries, boneLinks);
    }

    public static Spatial mergeSpatialsWithAnimations(AssetManager assetManager, boolean replaceBoneDups, Map<Geometry, String> boneLinks, Geometry... geometries) {
        List<Geometry> geoms = Arrays.asList(geometries);

//        // Not added to a node to not dettach from their parents and, so, do not mutate the original things.
//        for(Spatial spatial : spatials) {
//            GeometryBatchFactory.gatherGeoms(spatial, geometries);
//        }

        return mergeSpatialsWithAnimations(assetManager, replaceBoneDups, geoms, boneLinks);
    }

    public static Spatial mergeSpatialsWithAnimations(AssetManager assetManager, boolean replaceBoneDups, Map<Geometry, String> boneLinks, Spatial... spatials) {
        List<Geometry> geometries = new ArrayList<Geometry>();

        // Not added to a node to not dettach from their parents and, so, do not mutate the original things.
        for(Spatial spatial : spatials) {
            GeometryBatchFactory.gatherGeoms(spatial, geometries);
        }

        return mergeSpatialsWithAnimations(assetManager, replaceBoneDups, geometries, boneLinks);
    }

    public static void findSkAnimControlsFor(List<Geometry> geometries,Set<SkeletonControl> skeletonControls, Set<AnimControl> animControls) {
        for(Geometry geometry : geometries) {
            Spatial parent = geometry;
            while(parent != null) {
                SkeletonControl skeletonControl = parent.getControl(SkeletonControl.class);
                boolean well = true;

                if(skeletonControl != null) {
                    well = false;
                    skeletonControls.add(skeletonControl);

                    // Patch to not updated targets
                    if(skeletonControl.getTargets().length == 0) {
                        parent.removeControl(skeletonControl);
                        parent.addControl(skeletonControl);
                        if(skeletonControl.getTargets().length == 0) {
                            throw new UnknownError();
                        }
                    }
                }

                AnimControl animControl = parent.getControl(AnimControl.class);
                if(animControl != null) {
                    well = !well;
                    animControls.add(animControl);
                }

                if(!well) {
                    LoggerFactory.getLogger(MergeUtil.class).warn("Spatial: {}, has only a SekeletonControl or an AnimControl. This can lead to further problems.", parent);
                }

                parent = parent.getParent();
            }
        }
    }

    public static Spatial mergeSpatialsWithAnimations(AssetManager assetManager, boolean replaceBoneDups, Map<Geometry, String> boneLinks, List<Geometry> geometries) {
        return mergeSpatialsWithAnimations(assetManager, replaceBoneDups, geometries, boneLinks);
    }


    public static Spatial mergeSpatialsWithAnimations(AssetManager assetManager, boolean replaceBoneDups, List<Geometry> geometries, Map<Geometry, String> boneLinks) {
        Set<SkeletonControl> skeletonControls = new HashSet<SkeletonControl>(geometries.size());
        Set<AnimControl> animControls = new HashSet<AnimControl>(geometries.size());

        return mergeSpatialsWithAnimations(assetManager, replaceBoneDups, geometries, boneLinks, skeletonControls, animControls);
    }

    public static Spatial mergeSpatialsWithAnimations(AssetManager assetManager, boolean replaceBoneDups, List<Geometry> geometries, Map<Geometry, String> boneLinks, Set<SkeletonControl> skeletonControls, Set<AnimControl> animControls) {

        findSkAnimControlsFor(geometries, skeletonControls, animControls);

        Mesh newMesh = new Mesh();

        //TODO: Allow this decision to the tool user.
        if(!skeletonControls.isEmpty()) {
            for(Geometry geom : geometries) {
                createSkeletonBuffers(geom.getMesh(), (byte) 0);
            }
        }

        // If this throws a NPE can be because geometries is empty. If problems with buffers, check that the HWBoneWeight and HWBoneIndex have the same components that BoneWeight and BoneIndex respectively.
        GeometryBatchFactory.mergeGeometries(geometries, newMesh);


        int boneCount = 0;
        for(SkeletonControl skeletonControl : skeletonControls) {
            boneCount += skeletonControl.getSkeleton().getBoneCount();
        }

        Map<Bone, Bone> bonesAssocs = new HashMap<>((int) (boneCount / 0.75f) + 1, 0.75f);
        Map<Bone, Integer> newSkeletonBoneIndexes = new HashMap<>();

        //TODO: Can be done more efficiently with an int[] array containing the bone index redefinition based on the boneOffset when looping over them. The conterpart is it is less robust.


        SkeletonControl skeletonControl = copySkeletonControls(skeletonControls, geometries, newMesh, replaceBoneDups, bonesAssocs, newSkeletonBoneIndexes);
        //TODO: FIXME: support hardware skinning.
        skeletonControl.setHardwareSkinningPreferred(false);

        if(boneLinks != null && !boneLinks.isEmpty()) {
            Skeleton skeleton = skeletonControl.getSkeleton();
            int count = skeleton.getBoneCount();

            Map<String, Byte> boneIndexPerName = new HashMap<>((int) (count / 0.75f) + 1, 0.75f);
            for(int k = 0; k < count; k++) {
                Bone bone = skeleton.getBone(k);
                boneIndexPerName.put(bone.getName(), (byte)k);
            }



//            int i = 0;
            int offset = 0;
            for(Geometry geom : geometries) {
//            for (String boneLink : boneLinks) {
                Mesh mesh = geom.getMesh();
                int bufferSize = mesh.getTriangleCount() * 4;

//                String boneLink = boneLinks[i];
                String boneLink = boneLinks.get(geom);
                if (boneLink != null) {
                    byte boneIndex = boneIndexPerName.get(boneLink);



                    ByteBuffer boneIndexBuffer = (ByteBuffer) newMesh.getBuffer(VertexBuffer.Type.BoneIndex).getData();
                    FloatBuffer boneWeightBuffer = mesh.getFloatBuffer(VertexBuffer.Type.BoneWeight);
                    int endOffset = bufferSize + offset;
                    for (int k = offset; k < endOffset; k++) {
                        boneIndexBuffer.put(k, boneIndex);
                        boneWeightBuffer.put(k, 1);
                    }

                    // The bindPosePosition should already be set.

                }

                offset += bufferSize;
//                i++;
            }
        }


        String spatialName = "Merged" + mergedCount;
        Node node = new Node(spatialName + "_node");

        Geometry geometry = new Geometry(spatialName + "_geom", newMesh);
        node.addControl(skeletonControl);


        // Atlas
        if(checkAtlasNeed(geometries)) {
            TextureAtlas atlas = createAtlasFor(geometries, 4096);
            if (atlas == null) {
//                LoggerFactory.getLogger(MergeUtil.class).error("Atlas too small");
                throw new NullPointerException("Atlas too small");
            }
            //        applyAtlasTo(atlas, geometries);
            applyAtlasTo(atlas, geometries, newMesh);

            //TODO: when using hardware skinning the material uses bones parameters (so something must be done with these)
            geometry.setMaterial(createAtlasMaterialFor(atlas, assetManager));
        } else {
            geometry.setMaterial(geometries.get(0).getMaterial());
        }
        /////////////////////



        node.addControl(copyAnimControls(animControls, skeletonControl.getSkeleton(), bonesAssocs, newSkeletonBoneIndexes));

        node.attachChild(geometry);
        return node;
    }

//    public static AnimControl copyAnimControls(Collection<AnimControl> animControls, Skeleton toSkeleton) {
//        int currentBoneOffset = 0;
//
//        AnimControl newAnimControl = new AnimControl(toSkeleton);
//
//
//        for(AnimControl animControl : animControls) {
//            for(String animName : animControl.getAnimationNames()) {
//                Animation anim = animControl.getAnim(animName);
//
////                if(currentBoneOffset > 0) {
//                Animation newAnim = new Animation(anim.getName(), anim.getLength());
//
//                for(Track track : anim.getTracks()) {
//                    if(track instanceof BoneTrack) {
//                        //TODO: TOASK: Would be much easier if bonetrack could be cloned (it can) and the targetBoneIndex changed with a set (it CAN'T)
//                        // If so, it would be just as easy as clone the animation and then use the set of the desired boneTracks.
//                        int tablesLength = ((BoneTrack)track).getTimes().length;
//
//                        float[] times = ((BoneTrack)track).getTimes().clone();
//                        Vector3f[] sourceTranslations = ((BoneTrack)track).getTranslations();
//                        Quaternion[] sourceRotations = ((BoneTrack)track).getRotations();
//                        Vector3f[] sourceScales = ((BoneTrack)track).getScales();
//
//                        Vector3f[] translations = new Vector3f[tablesLength];
//                        Quaternion[] rotations = new Quaternion[tablesLength];
//                        Vector3f[] scales = new Vector3f[tablesLength];
//                        for (int i = 0; i < tablesLength; ++i) {
//                            translations[i] = sourceTranslations[i].clone();
//                            rotations[i] = sourceRotations[i].clone();
//                            scales[i] = sourceScales != null ? sourceScales[i].clone() : new Vector3f(1.0f, 1.0f, 1.0f);
//                        }
//
//                        // Need to use the constructor here because of the final fields used in this class
//                        newAnim.addTrack(new BoneTrack(((BoneTrack)track).getTargetBoneIndex() + currentBoneOffset, times, translations, rotations, scales));
//
//                    } else {
//                        newAnim.addTrack(track.clone());
//                    }
//                }
//
//                newAnimControl.addAnim(newAnim);
////                } else {
////                    newAnimControl.addAnim(anim.clone());
////                }
//
//            }
//            currentBoneOffset += animControl.getSkeleton().getBoneCount();
////            skeletonNumber++;
////            mergedNode.addControl(animControl);
//        }
//        return newAnimControl;
//    }
    public static AnimControl copyAnimControls(Collection<AnimControl> animControls, Skeleton toSkeleton, Map<Bone, Bone> bonesAssocs, Map<Bone, Integer> newSkeletonBoneIndexes) {
        int currentBoneOffset = 0;

        AnimControl newAnimControl = new AnimControl(toSkeleton);
        Map<String, Animation> nameAnimAssocs = new HashMap<String, Animation>();

        for(AnimControl animControl : animControls) {
            for(String animName : animControl.getAnimationNames()) {
                Animation alreadyAnim = nameAnimAssocs.get(animName);

                Animation anim = animControl.getAnim(animName);

    //                if(currentBoneOffset > 0) {
                if(alreadyAnim != null) {
                    for(Track track : anim.getTracks()) {
                        boolean duplicated = false;
                        int newBoneIndex = -1;
                        if(track instanceof BoneTrack) {
                            newBoneIndex = newSkeletonBoneIndexes.get(bonesAssocs.get(animControl.getSkeleton().getBone(((BoneTrack) track).getTargetBoneIndex())));
                            for(Track track2 : alreadyAnim.getTracks()) {
                                if(track2 instanceof BoneTrack) {
                                    if(newBoneIndex == ((BoneTrack) track2).getTargetBoneIndex()) {
                                        duplicated = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            for(Track track2 : alreadyAnim.getTracks()) {
                                if(track.equals(track2)) {
                                    duplicated = true;
                                    break;
                                }
                            }
                        }

                        if(!duplicated) {
                            alreadyAnim.addTrack(copyTrack(track, newBoneIndex));
//                            alreadyAnim.addTrack(copyTrack(track, currentBoneOffset));
                        }
                    }
                } else {
                    Animation newAnim = new Animation(anim.getName(), anim.getLength());

                    int newBoneIndex;
                    for(Track track : anim.getTracks()) {
                        newBoneIndex = newSkeletonBoneIndexes.get(bonesAssocs.get(animControl.getSkeleton().getBone(((BoneTrack) track).getTargetBoneIndex())));
                        newAnim.addTrack(copyTrack(track, newBoneIndex));
//                        newAnim.addTrack(copyTrack(track, currentBoneOffset));
    //                            if(track instanceof BoneTrack) {
    //                                //TODO: TOASK: Would be much easier if bonetrack could be cloned (it can) and the targetBoneIndex changed with a set (it CAN'T)
    //                                // If so, it would be just as easy as clone the animation and then use the set of the desired boneTracks.
    //                                int tablesLength = ((BoneTrack)track).getTimes().length;
    //
    //                                float[] times = ((BoneTrack)track).getTimes().clone();
    //                                Vector3f[] sourceTranslations = ((BoneTrack)track).getTranslations();
    //                                Quaternion[] sourceRotations = ((BoneTrack)track).getRotations();
    //                                Vector3f[] sourceScales = ((BoneTrack)track).getScales();
    //
    //                                Vector3f[] translations = new Vector3f[tablesLength];
    //                                Quaternion[] rotations = new Quaternion[tablesLength];
    //                                Vector3f[] scales = new Vector3f[tablesLength];
    //                                for (int i = 0; i < tablesLength; ++i) {
    //                                    translations[i] = sourceTranslations[i].clone();
    //                                    rotations[i] = sourceRotations[i].clone();
    //                                    scales[i] = sourceScales != null ? sourceScales[i].clone() : new Vector3f(1.0f, 1.0f, 1.0f);
    //                                }
    //
    //                                // Need to use the constructor here because of the final fields used in this class
    //                                newAnim.addTrack(new BoneTrack(((BoneTrack)track).getTargetBoneIndex() + currentBoneOffset, times, translations, rotations, scales));
    //
    //                            } else {
    //                                newAnim.addTrack(track.clone());
    //                            }
                    }

                    newAnimControl.addAnim(newAnim);
                    nameAnimAssocs.put(animName, newAnim);
                }
    //                } else {
    //                    newAnimControl.addAnim(anim.clone());
    //                }
            }
            currentBoneOffset += animControl.getSkeleton().getBoneCount();
    //            skeletonNumber++;
    //            mergedNode.addControl(animControl);
        }
        return newAnimControl;
    }


    private static Track copyTrack(Track track, int currentIndex) {//int currentBoneOffset) {
        if(track instanceof BoneTrack) {
            //TODO: TOASK: Would be much easier if bonetrack could be cloned (it can) and the targetBoneIndex changed with a set (it CAN'T)
            // If so, it would be just as easy as clone the animation and then use the set of the desired boneTracks.
            int tablesLength = ((BoneTrack)track).getTimes().length;

            float[] times = ((BoneTrack)track).getTimes().clone();
            Vector3f[] sourceTranslations = ((BoneTrack)track).getTranslations();
            Quaternion[] sourceRotations = ((BoneTrack)track).getRotations();
            Vector3f[] sourceScales = ((BoneTrack)track).getScales();

            Vector3f[] translations = new Vector3f[tablesLength];
            Quaternion[] rotations = new Quaternion[tablesLength];
            Vector3f[] scales = new Vector3f[tablesLength];
            for (int i = 0; i < tablesLength; ++i) {
                translations[i] = sourceTranslations[i].clone();
                rotations[i] = sourceRotations[i].clone();
                scales[i] = sourceScales != null ? sourceScales[i].clone() : new Vector3f(1.0f, 1.0f, 1.0f);
            }

            // Need to use the constructor here because of the final fields used in this class
//            return new BoneTrack(((BoneTrack)track).getTargetBoneIndex() + currentBoneOffset, times, translations, rotations, scales);
            return new BoneTrack(currentIndex, times, translations, rotations, scales);

        } else {
            return track.clone();
        }
    }



    public static SkeletonControl copySkeletonControls(Collection<SkeletonControl> skeletonControls, List<Geometry> geometries, Mesh toMesh, boolean replaceDups, Map<Bone, Bone> bonesAssocs, Map<Bone, Integer> newSkeletonBoneIndexes) {
        int size = skeletonControls.size();
        Skeleton[] skeletons = new Skeleton[size];
        Vector3f[] skeletonOffsets = new Vector3f[size];


        Map<Mesh, Geometry> geometryMeshAssoc = new HashMap<Mesh, Geometry>((int) (geometries.size() / 0.75f) + 1, 0.75f);
        for(Geometry geometry : geometries) {
            geometryMeshAssoc.put(geometry.getMesh(), geometry);
        }

        int i = 0;
        for(SkeletonControl skeletonControl : skeletonControls) {
            Skeleton skeleton = skeletonControl.getSkeleton();
            skeletons[i] = skeleton;

//            LoggerFactory.getLogger(MergeUtil.class).debug("Null??? is: {}, {}", skeletonControl);

            Spatial skeletonSpatial = skeletonControl.getSpatial();
            if(skeletonSpatial != null) {
                skeletonOffsets[i] = skeletonSpatial.getWorldTranslation();
            } else {
                LoggerFactory.getLogger(MergeUtil.class).trace("Merged skeleton control without spatial: {}", skeletonControl);
                skeletonOffsets[i] = Vector3f.ZERO;
            }

            i++;
        }


        SkeletonControl newSkeletonControl = new SkeletonControl(copySkeleton(skeletons, skeletonOffsets, replaceDups, bonesAssocs, newSkeletonBoneIndexes));







        int currentBoneIndex = 0;
        int currentBindPoseIndex = 0;
        int currentBoneOffset = 0;
        Vector3f currentBindPoseOffset = Vector3f.ZERO.clone();

        for(SkeletonControl skeletonControl : skeletonControls) {
//        for(int i = 0; i < size; i++) {
//            SkeletonControl skeletonControl = skeletonControls.get(i);
            Skeleton skeleton = skeletonControl.getSkeleton();
//            skeletons[i] = skeleton;
//            skeletonOffsets[i] = skeletonControl.getSpatial().getWorldTranslation();

            //TODO: create a set with meshes so they doesn't dup and avoid outofbounds with exceptions. maybe? - is it already with geometryMeshAssoc? xD

            //TODO: treat the first case apart.
            for(Mesh mesh : skeletonControl.getTargets()) {
//            for (Geometry geom : geometries) {
//                Mesh mesh = geom.getMesh();
                // Bone buffer
                VertexBuffer vertexBuffer = mesh.getBuffer(VertexBuffer.Type.BoneIndex);

//                int bufferSize = vertexBuffer.getData().capacity();
                int bufferSize;
//                if(vertexBuffer == null) {
//                    vertexBuffer = mesh.getBuffer(VertexBuffer.Type.Index);
//////                    bufferSize = vertexBuffer.getNumComponents() * vertexBuffer.getNumElements();
////                    float[] zeroBuffer = new float[vertexBuffer.getNumElements()];
////                    mesh.setBuffer(VertexBuffer.Type.BoneIndex, 1, zeroBuffer);
//////                    vertexBuffer = VertexBuffer.createBuffer(VertexBuffer.Format.Float, 1, vertexBuffer.getNumElements());
//
//                    bufferSize = vertexBuffer.getNumElements();
//                    vertexBuffer = toMesh.getBuffer(VertexBuffer.Type.BoneIndex);
//                    bufferSize *= vertexBuffer.getNumComponents(); // To be sure the numComponents is the right amount, we get it from the known existant buffer.
//
//
//                    ByteBuffer byteData = (ByteBuffer) vertexBuffer.getData();
//                    for (int k = currentBoneIndex; k < bufferSize + currentBoneIndex; k++) {
//                        byteData.put(k, (byte) (byteData.get(k) + currentBoneOffset));
//                    }
//
//
//                    //TODO: link to the bone associated to a node instead of the rootBone
//
//                    continue;
//                }
//



                int numComponents = vertexBuffer.getNumComponents();
                bufferSize = numComponents * vertexBuffer.getNumElements();

//                LoggerFactory.getLogger(MergeUtil.class).debug("BufferSize: {} ({}), CurrentBoneIndex: {}, total: {}.", bufferSize, mesh.hashCode(), currentBoneIndex, bufferSize + currentBoneIndex);
//                LoggerFactory.getLogger(MergeUtil.class).debug("toMesh buffer size: {}.", toMesh.getBuffer(VertexBuffer.Type.BoneIndex).getData().capacity());

                ByteBuffer byteData = (ByteBuffer) toMesh.getBuffer(VertexBuffer.Type.BoneIndex).getData();
                // OLD WAY --------------
//                for (int k = currentBoneIndex; k < bufferSize + currentBoneIndex; k++) {
//                    byteData.put(k, (byte) (byteData.get(k) + currentBoneOffset));
//                }
//
//                currentBoneIndex += bufferSize;

                /// NEW WAY

                int newBoneIndex;
                for (int k = currentBoneIndex; k < currentBoneIndex + bufferSize; k++) {
                    newBoneIndex = newSkeletonBoneIndexes.get(bonesAssocs.get(skeleton.getBone((byteData.get(k)))));
                    byteData.put(k, (byte) newBoneIndex);
                }
                // --------------------



                // BindPose buffer
                vertexBuffer = mesh.getBuffer(VertexBuffer.Type.BindPosePosition);

//                bufferSize = vertexBuffer.getData().capacity();
                numComponents = vertexBuffer.getNumComponents();
                bufferSize = numComponents * vertexBuffer.getNumElements();


                LoggerFactory.getLogger(MergeUtil.class).debug("Offset: {}, GeometryMeshAssoc: {}, mesh: {}, obtained: {}, SkeletonControl's Spatial: {}", currentBindPoseOffset, geometryMeshAssoc, mesh, geometryMeshAssoc.get(mesh), skeletonControl.getSpatial());
                // If this throws a NPE is more likely because of the skeleton is having a wrong mesh (a mesh that isn't in the spatial being merged)
                currentBindPoseOffset.set(geometryMeshAssoc.get(mesh).getWorldTranslation());
//                currentBindPoseOffset.set(geom.getWorldTranslation());
                FloatBuffer floatData = toMesh.getFloatBuffer(VertexBuffer.Type.BindPosePosition);
                for (int k = currentBindPoseIndex; k < bufferSize + currentBindPoseIndex; k += 3) {
                    floatData.put(k, floatData.get(k) + currentBindPoseOffset.x);
                }
                for (int k = currentBindPoseIndex + 1; k < bufferSize + currentBindPoseIndex - 1; k += 3) {
                    floatData.put(k, floatData.get(k) + currentBindPoseOffset.y);
                }
                for (int k = currentBindPoseIndex + 2; k < bufferSize + currentBindPoseIndex - 2; k += 3) {
                    floatData.put(k, floatData.get(k) + currentBindPoseOffset.z);
                }


                currentBindPoseIndex += bufferSize;

            }

            currentBoneOffset += skeleton.getBoneCount();
//        }
        }



        return newSkeletonControl;


//        offsetBoneBuffers(mergedNode, 240, (byte)mergedSkeleton.getBoneCount());
//        offsetBindPoseBuffers(mergedNode, 180, escoba.getWorldTranslation());
    }


    public static Skeleton copySkeleton(Skeleton skeleton, Vector3f offset) {
        int boneCount = skeleton.getBoneCount();

        Bone[] bones = new Bone[boneCount];

        Map<Bone, Bone> bonesAssocs = new HashMap<Bone, Bone>((int) (boneCount / 0.75f) + 1, 0.75f);

        for(Bone bone : skeleton.getRoots()) {
            copyBone(bone, bonesAssocs, offset, null);
        }

        int b = 0;
        for(int i = 0; i < boneCount; i++) {
            bones[b++] = bonesAssocs.get(skeleton.getBone(i));
        }

        return new Skeleton(bones);
    }

    public static Skeleton copySkeleton(Skeleton[] skeletons, Vector3f[] offsets, boolean replaceDups, Map<Bone, Bone> bonesAssocs, Map<Bone, Integer> newSkeletonBoneIndexes) {
        return copySkeleton(Arrays.asList(skeletons), Arrays.asList(offsets), replaceDups, bonesAssocs, newSkeletonBoneIndexes);
    }

    public static Skeleton copySkeleton(List<Skeleton> skeletons, List<Vector3f> offsets, boolean replaceDups, Map<Bone, Bone> bonesAssocs, Map<Bone, Integer> newSkeletonBoneIndexes) {
        int boneCount = 0;
        for(Skeleton skeleton : skeletons) {
            boneCount += skeleton.getBoneCount();
        }

        Bone[] bones = new Bone[boneCount];

//        bonesAssocs = new HashMap<Bone, Bone>((int) (boneCount / 0.75f) + 1, 0.75f);
        Map<String, Map<String, Bone>> rootNameAssocs = new HashMap<>();

//        List<Map<Bone, Integer>> skeletonBoneIndexes; // Index on the final skeleton for a bone in the original skeleton.
//        newSkeletonBoneIndexes = new HashMap<>(); // Not needed to make a list if using Bones as keys (as they are different for every skeleton, not as the bone names)


        int b = 0;
        int offsetIndex = 0;
        for(Skeleton skeleton : skeletons) {
            for(Bone bone : skeleton.getRoots()) {
                LoggerFactory.getLogger(MergeUtil.class).debug("Merging skeleton under root bone: {}.", bone.getName());

                Map<String, Bone> nameBoneAssocs = rootNameAssocs.get(bone.getName());
                if(nameBoneAssocs == null) {
                    nameBoneAssocs = new HashMap<>();
                    rootNameAssocs.put(bone.getName(), nameBoneAssocs);
                }

                // Hm... why is this returned bone ignored? - Once the answer is known, write it here as a comment xD.
                copyBone(bone, bonesAssocs, offsets.get(offsetIndex), replaceDups ? nameBoneAssocs: null);
            }




            int count = skeleton.getBoneCount();
            for(int i = 0; i < count; i++) {
                Bone newBone = bonesAssocs.get(skeleton.getBone(i));
                if(!newSkeletonBoneIndexes.containsKey(newBone)) {
                    if (newSkeletonBoneIndexes.put(newBone, b) == null) {
                        bones[b++] = newBone;
                    } else {
                        LoggerFactory.getLogger(MergeUtil.class).error("WTF!");
                    }
                }
//                bones[b++] = bonesAssocs.get(skeleton.getBone(i));
            }

            offsetIndex++;
        }


        return new Skeleton(Arrays.copyOf(bones, b));
    }

    private static Bone copyBone(Bone bone, Map<Bone, Bone> assoc, Vector3f offset, Map<String, Bone> nameBoneAssocs) {

        TempVars vars = TempVars.get();

        Bone newBone = null;
        boolean newBoneCreated = false;
        if(nameBoneAssocs != null) {
            newBone = nameBoneAssocs.get(bone.getName());
        }

        if(newBone == null) {
            newBone = new Bone(bone.getName());
            newBone.setUserControl(bone.hasUserControl());
            if(bone.hasUserControl()) {
                newBone.setUserTransforms(vars.vect1.set(bone.getLocalPosition()).addLocal(offset), bone.getLocalRotation(), bone.getLocalScale());
            }

            newBone.setBindTransforms(vars.vect1.set(bone.getBindPosition()).addLocal(offset), bone.getBindRotation(), bone.getBindScale());
            nameBoneAssocs.put(newBone.getName(), newBone);
            newBoneCreated = true;
        } else {
            LoggerFactory.getLogger(MergeUtil.class).debug("Duplicated bone: {}. Replaced.", bone.getName());
        }
        vars.release();

//        newBone.getModelBindInversePosition().set(bone.getModelBindInversePosition());
//        newBone.getModelBindInverseRotation().set(bone.getModelBindInverseRotation());
//        newBone.getModelBindInverseScale().set(bone.getModelBindInverseScale());

        for(Bone b : bone.getChildren()) {
            Bone childBone = copyBone(b, assoc, Vector3f.ZERO, nameBoneAssocs);
            if(childBone != null) {
                // This if check is needed because of when newBone was dup, the childBone can also be a dup. Not needed if in dup case null is returned?
//                if(!newBone.getChildren().contains(childBone)) {
                    newBone.addChild(childBone);
//                }
            }
        }

        assoc.put(bone, newBone);

        return newBoneCreated ? newBone : null;
    }




//    public static Skeleton copySkeleton(Skeleton skeleton, Vector3f offset) {
//        int boneCount = skeleton.getBoneCount();
//
//        Bone[] bones = new Bone[boneCount];
//
//        Map<Bone, Bone> bonesAssocs = new HashMap<Bone, Bone>((int) (boneCount / 0.75f) + 1, 0.75f);
//
//        for(Bone bone : skeleton.getRoots()) {
//            copyBone(bone, bonesAssocs, offset);
//        }
//
//        int b = 0;
//        for(int i = 0; i < boneCount; i++) {
//            bones[b++] = bonesAssocs.get(skeleton.getBone(i));
//        }
//
//        return new Skeleton(bones);
//    }
//
//    public static Skeleton copySkeleton(Skeleton[] skeletons, Vector3f[] offsets) {
//        return copySkeleton(Arrays.asList(skeletons), Arrays.asList(offsets));
//    }
//
//    public static Skeleton copySkeleton(List<Skeleton> skeletons, List<Vector3f> offsets) {
//        int boneCount = 0;
//        for(Skeleton skeleton : skeletons) {
//            boneCount += skeleton.getBoneCount();
//        }
//
//        Bone[] bones = new Bone[boneCount];
//
//        Map<Bone, Bone> bonesAssocs = new HashMap<Bone, Bone>((int) (boneCount / 0.75f) + 1, 0.75f);
//
//        int b = 0;
//        int offsetIndex = 0;
//        for(Skeleton skeleton : skeletons) {
//            for(Bone bone : skeleton.getRoots()) {
//                copyBone(bone, bonesAssocs, offsets.get(offsetIndex));
//            }
//
//            int count = skeleton.getBoneCount();
//            for(int i = 0; i < count; i++) {
//                bones[b++] = bonesAssocs.get(skeleton.getBone(i));
//            }
//
//            offsetIndex++;
//        }
//
//
//        return new Skeleton(bones);
//    }
//
//    private static Bone copyBone(Bone bone, Map<Bone, Bone> assoc, Vector3f offset) {
//
//        TempVars vars = TempVars.get();
//
//        Bone newBone = new Bone(bone.getName());
//        newBone.setUserControl(bone.hasUserControl());
//        if(bone.hasUserControl()) {
//            newBone.setUserTransforms(vars.vect1.set(bone.getLocalPosition()).addLocal(offset), bone.getLocalRotation(), bone.getLocalScale());
//        }
//
//
//
//        newBone.setBindTransforms(vars.vect1.set(bone.getBindPosition()).addLocal(offset), bone.getBindRotation(), bone.getBindScale());
//
//        vars.release();
//
////        newBone.getModelBindInversePosition().set(bone.getModelBindInversePosition());
////        newBone.getModelBindInverseRotation().set(bone.getModelBindInverseRotation());
////        newBone.getModelBindInverseScale().set(bone.getModelBindInverseScale());
//
//        for(Bone b : bone.getChildren()) {
//            newBone.addChild(copyBone(b, assoc, Vector3f.ZERO));
//        }
//
//        assoc.put(bone, newBone);
//
//        return newBone;
//    }

}


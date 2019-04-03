package com.nx.util.jme3.base;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.SafeArrayList;
import com.jme3.util.clone.Cloner;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by NemesisMate on 27/04/17.
 *
 * Based on: {@link com.jme3.scene.AssetLinkNode}
 */
public class SingleLinkNode extends Node {


//    protected ArrayList<ModelKey> assetLoaderKeys = new ArrayList<ModelKey>();
//    protected Map<ModelKey, Spatial> assetChildren = new HashMap<ModelKey, Spatial>();

    Spatial assetChild;
    ModelKey assetLoaderKey;

    public SingleLinkNode() {
    }

    public SingleLinkNode(ModelKey modelKey) {
        this(modelKey.getName(), modelKey);
    }

    public SingleLinkNode(String name, ModelKey modelKey) {
        super(name);
        assetLoaderKey = modelKey;
//        assetLoaderKeys.add(key);
    }

    public SingleLinkNode(String name, Spatial asset, ModelKey modelKey) {
        super(name);

        attachLinkedChild(asset, modelKey);
    }

    /**
     *  Called internally by com.jme3.util.clone.Cloner.  Do not call directly.
     */
    @Override
    public void cloneFields(Cloner cloner, Object original ) {
        super.cloneFields(cloner, original);

        // This is a change in behavior because the old version did not clone
        // this list... changes to one clone would be reflected in all.
        // I think that's probably undesirable. -pspeed
//        this.assetLoaderKeys = cloner.clone(assetLoaderKeys);
//        this.assetChildren = new HashMap<ModelKey, Spatial>();

        this.assetLoaderKey = cloner.clone(assetLoaderKey);
        this.assetChild = null;
    }

    /**
     * Add a "linked" child. These are loaded from the assetManager when the
     * AssetLinkNode is loaded from a binary file.
     * @param key
     */
    public void addLinkedChild(ModelKey key) {
        if(!key.equals(assetLoaderKey)) {
            if(assetLoaderKey != null) {
                throw new IllegalArgumentException("SingleLinkNode can only have one child. To add more, use AssetLinkNode");
            }

            this.assetLoaderKey = key;
        }
//        if (assetLoaderKeys.contains(key)) {
//            return;
//        }
//        assetLoaderKeys.add(key);
    }

    public void removeLinkedChild(ModelKey key) {
        if(key.equals(assetLoaderKey)) {
            assetLoaderKey = null;
        }
//        assetLoaderKeys.remove(key);
    }

    public ModelKey getAssetLoaderKey() {
        return assetLoaderKey;
    }

    public void attachLinkedChild(AssetManager manager, ModelKey key) {
        addLinkedChild(key);
        Spatial child = manager.loadAsset(key);
        assetChild = child;
//        assetChildren.put(key, child);
        attachChild(child);
    }

    public void attachLinkedChild(Spatial spat, ModelKey key) {
        addLinkedChild(key);
//        assetChildren.put(key, spat);
        assetChild = spat;
        attachChild(spat);
    }

    public void detachLinkedChild(ModelKey key) {
        if(key.equals(assetLoaderKey)) {
            detachChild(assetChild);

            //        removeLinkedChild(key);
            assetLoaderKey = null;
            assetChild = null;
        }




//        Spatial spatial = assetChildren.get(key);
//        if (spatial != null) {
//            detachChild(spatial);
//        }
//        removeLinkedChild(key);
//        assetChildren.remove(key);
    }

    public void detachLinkedChild(Spatial child, ModelKey key) {
//        removeLinkedChild(key);
        if(key.equals(assetLoaderKey)) {
            assetLoaderKey = null;
            assetChild = null;
//            assetChildren.remove(key);
        }
        detachChild(child);
    }

    /**
     * Loads the linked children AssetKeys from the AssetManager and attaches them to the Node<br>
     * If they are already attached, they will be reloaded.
     * @param manager
     */
    public void attachLinkedChildren(AssetManager manager) {
        detachLinkedChildren();

        if(assetLoaderKey != null) {
            if (assetChild != null) {
                assetChild.removeFromParent();
            }

            Spatial assetChild = manager.loadAsset(assetLoaderKey);
            attachChild(assetChild);
        }
    }

    public void detachLinkedChildren() {
        if(assetLoaderKey != null) {
            assetChild.removeFromParent();
            assetLoaderKey = null;
        }
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        BinaryImporter importer = BinaryImporter.getInstance();
        AssetManager loaderManager = im.getAssetManager();

        LoggerFactory.getLogger(this.getClass()).debug("SINGLELINKING.... 0: ");

        assetLoaderKey = (ModelKey) ic.readSavable("assetLoaderKey", null);


        LoggerFactory.getLogger(this.getClass()).debug("SINGLELINKING: " + assetLoaderKey);

        if(assetLoaderKey != null) {
//            AssetInfo info = loaderManager.locateAsset(assetLoaderKey);

//            Spatial child = null;
//            if (info != null) {
                assetChild = loaderManager.getFromCache(assetLoaderKey);
                if(assetChild == null) {
                    AssetInfo info = loaderManager.locateAsset(assetLoaderKey);
                    assetChild = (Spatial) importer.load(info);

                    loaderManager.addToCache(assetLoaderKey, assetChild);
                } else {
                    assetChild = assetChild.clone();

                    LoggerFactory.getLogger(this.getClass()).debug("YEI!, loaded from cache!!!");
                }

                if(assetChild.getKey() == null) {
                    assetChild.setKey(assetLoaderKey);
                }
//                assetChild = (Spatial) importer.load(info);

//            }
            if (assetChild != null) {
                LoggerFactory.getLogger(this.getClass()).debug("LOADED: " + assetLoaderKey + ". Mesh: " + SpatialUtil.meshBuffersHash(((Geometry)assetChild).getMesh()));

                //FIXME: :( Can't access parent from here!!!, so a much less efficient way is used: attachChild
//                child.parent = this;
//                children.add(assetChild);
                attachChild(assetChild);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot locate {0} for asset link node {1}",
                        new Object[]{ assetLoaderKey, key });
            }
        }
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        SafeArrayList<Spatial> childs = children;
        children = new SafeArrayList<Spatial>(Spatial.class);
        super.write(e);
        OutputCapsule capsule = e.getCapsule(this);
        LoggerFactory.getLogger(this.getClass()).debug("Writing assetLoaderKey: {}, for: {}", assetLoaderKey, assetChild);
        capsule.write(assetLoaderKey, "assetLoaderKey", null);
        children = childs;
    }

}

package com.nx.util.jme3.base;

import com.jme3.asset.AssetManager;
import com.jme3.asset.MaterialKey;
import com.jme3.asset.ModelKey;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.util.clone.Cloner;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by NemesisMate on 28/04/17.
 *
 * Based on: {@link com.jme3.scene.AssetLinkNode}
 */
public class SingleLinkMaterializedNode extends SingleLinkNode {

    MaterialKey materialKey;


    public SingleLinkMaterializedNode() {
    }

    public SingleLinkMaterializedNode(ModelKey modelKey, MaterialKey materialKey) {
        super(modelKey);

        this.materialKey = materialKey;
    }

    public SingleLinkMaterializedNode(String name, ModelKey modelKey, MaterialKey materialKey) {
        super(name, modelKey);

        this.materialKey = materialKey;
    }

    public SingleLinkMaterializedNode(String name, Spatial asset, ModelKey modelKey, MaterialKey materialKey) {
        super(name, asset, modelKey);

        this.materialKey = materialKey;
    }

    public MaterialKey getMaterialKey() {
        return materialKey;
    }

    public void setMaterialKey(MaterialKey materialKey) {
        this.materialKey = materialKey;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);

        this.materialKey = cloner.clone(materialKey);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);

        InputCapsule capsule = im.getCapsule(this);

        materialKey = (MaterialKey) capsule.readSavable("materialLoaderKey", null);

        if(assetChild != null) {
            LoggerFactory.getLogger(this.getClass()).debug("LINKLOADING MKEY: {}, for: {}", materialKey, assetChild);

            AssetManager assetManager = im.getAssetManager();
            Material material = assetManager.getFromCache(materialKey);
            if(material == null) {
//                AssetInfo info = assetManager.locateAsset(assetLoaderKey);
//                material = (Material) BinaryImporter.getInstance().load(info);
//
//                assetManager.addToCache(material);
                material = im.getAssetManager().loadAsset(materialKey);
            }

            if(material.getKey() == null) {
                material.setKey(materialKey);
            }

            assetChild.setMaterial(material);
        }

        LoggerFactory.getLogger(this.getClass()).debug("LINKLOADED MKEY: {}, for: {}", assetChild.getKey(), assetChild);
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        super.write(e);

        LoggerFactory.getLogger(this.getClass()).debug("Writing materialLoaderKey: {}, for: {}", materialKey, assetChild);

        OutputCapsule capsule = e.getCapsule(this);
        capsule.write(materialKey, "materialLoaderKey", null);
    }

}

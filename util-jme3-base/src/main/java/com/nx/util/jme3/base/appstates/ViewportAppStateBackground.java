package com.nx.util.jme3.base.appstates;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.ui.Picture;

public class ViewportAppStateBackground extends ViewportAppState {

    private Picture picture;
    protected String texturePath;


    public ViewportAppStateBackground(String texturePath) {
        this(texturePath, null);
    }

    public ViewportAppStateBackground(String texturePath, Camera camera) {
        super(Mode.BACKGROUND, null, camera);

        this.texturePath = texturePath;
    }

    @Override
    protected void initialize(Application app) {
        super.initialize(app);

        camera.setParallelProjection(true);

        picture = new Picture("background");

        picture.setMaterial(createMaterial(app.getAssetManager()));

        picture.setWidth(viewPort.getCamera().getWidth());
        picture.setHeight(viewPort.getCamera().getHeight());

        picture.setQueueBucket(RenderQueue.Bucket.Gui);

        rootNode.attachChild(picture);

        rootNode.updateGeometricState();
    }

    protected Material createMaterial(AssetManager assetManager) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture(texturePath));

        return material;
    }

    public Picture getPicture() {
        return picture;
    }

    @Override
    public void update(float tpf) { }

    @Override
    public void render(RenderManager rm) { }

    @Override
    protected void cleanup(Application app) {
        super.cleanup(app);

        picture = null;
    }
}

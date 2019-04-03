package com.nx.util.jme3.base.appstates;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

public class ViewportAppState extends BaseAppState {

    public enum Mode {
        BACKGROUND, MAIN, FOREGROUND
    }

    protected Mode mode;
    protected Node rootNode;
    protected ViewPort viewPort;
    protected Camera camera;

    public ViewportAppState() {
        this(Mode.MAIN, null, null);
    }

    public ViewportAppState(Mode mode, String name) {
        this(mode, name, null);
    }

    public ViewportAppState(Mode mode, String name, Camera camera) {
        this.mode = mode;
        this.rootNode = new Node(name != null ? name : "Viewport-" + mode + "-Root");
        this.camera = camera;
    }

    @Override
    protected void initialize(Application app) {
        if(camera == null) {
            camera = app.getCamera().clone();
        }

        switch (mode) {
            case BACKGROUND:
                viewPort = app.getRenderManager().createPreView(rootNode.getName(), camera);
                viewPort.setClearFlags(true, true, true);
                app.getViewPort().setClearColor(false);
                viewPort.setBackgroundColor(new ColorRGBA(.13f, .19f, .21f, 1f));
                break;
            case FOREGROUND:
                viewPort = app.getRenderManager().createPostView(rootNode.getName(), camera);
                viewPort.setClearFlags(false, false, false);
                break;
            default:
                viewPort = app.getRenderManager().createMainView(rootNode.getName(), camera);
                viewPort.setClearFlags(false, false, false);
                break;
        }

        viewPort.attachScene(rootNode);
    }

    public Node getRootNode() {
        return rootNode;
    }

    public ViewPort getViewPort() {
        return viewPort;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void update(float tpf) {
        rootNode.updateLogicalState(tpf);
    }

    @Override
    public void render(RenderManager rm) {
        rootNode.updateGeometricState();
    }

    @Override
    protected void cleanup(Application app) {
        switch (mode) {
            case MAIN:
                app.getRenderManager().removeMainView(viewPort);
                break;
            case BACKGROUND:
                app.getRenderManager().removePreView(viewPort);
                break;
            case FOREGROUND:
                app.getRenderManager().removePostView(viewPort);
                break;
        }
        viewPort = null;
        rootNode.detachAllChildren();
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}

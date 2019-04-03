/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nx.util.jme3.lemur.panel;


import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.simsilica.lemur.style.ElementId;

/**
 *
 * @author xelun
 */
public class ViewportPanel2D extends ViewportPanel {
    protected Node rootNode = new Node("Absolute Node");

    public ViewportPanel2D(AppStateManager stateManager, ElementId elementid, String style) {
        super(stateManager, elementid, style);
        viewPortNode.attachChild(rootNode);
        autoZoom = false;
    }

    public ViewportPanel2D(ElementId elementid, String style) {
        super(null, elementid, style);
    }

    @Override
    public void setCam(Camera cam) {
        super.setCam(cam);

        
        this.cam.setLocation(Vector3f.ZERO);
        this.cam.setParallelProjection(true);
    }


    @Override
    protected void updatePerspective(Vector3f size) { }

    @Override
    protected void setViewPortSize(Vector3f size) {
        if(viewport == null) {
            return;
        }

        super.setViewPortSize(size);

        //TODO: Wouldn't it work if instead of the rootNode, the viewportNode was used? (avoiding instanceof checking on recalculate real size on ViewportPanel)
        Camera camera = viewport.getCamera();
        rootNode.setLocalTranslation(camera.getFrustumLeft(), camera.getFrustumTop(), -10f);
        rootNode.setLocalScale((camera.getFrustumRight() * 2) / size.x, (camera.getFrustumTop() * 2) / size.y, 1);
    }

    @Override
    public Node getViewportNode() {
        return rootNode;
    }
}

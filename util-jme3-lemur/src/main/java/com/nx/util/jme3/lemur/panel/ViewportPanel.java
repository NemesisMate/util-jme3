/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nx.util.jme3.lemur.panel;


import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.Light;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.util.SafeArrayList;
import com.nx.util.jme3.base.ScenegraphListenerControl;
import com.nx.util.jme3.base.SpatialUtil;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.core.AbstractGuiControlListener;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.event.BasePickState;
import com.simsilica.lemur.event.PickState;
import com.simsilica.lemur.style.ElementId;
import org.lwjgl.opengl.Display;

/**
 *
 * @author xelun
 */
public class ViewportPanel extends Panel {

    public static final String ELEMENT_ID = "viewportPanel";
    public static final String NODE_DATA = "VPanel";

    public interface ViewportPanelListener {
        void onViewportCreate(ViewPort viewPort);
        void onViewportRemoval(ViewPort viewPort);
    }

    SafeArrayList<ViewportPanelListener> listeners;

    protected ViewPort viewport;
    protected Node viewPortNode;
//    protected Vector3f lastSize = new Vector3f();
    protected Camera cam;
    protected RenderManager renderManager;
    protected AppStateManager stateManager;

    private final Vector3f camOrigin = new Vector3f();
    //    Vector3f boundsExtents = new Vector3f();
    private final Vector3f camOffset = new Vector3f();

    protected final Transform realTransform = new Transform();

    protected boolean autoZoom = true;

    private final Vector3f lastPosition =new Vector3f ();
//    private Node rootNode;

    private final Control viewportNodeUpdater = new AbstractControl() {
        @Override
        protected void controlUpdate(float tpf) {

//                if(viewport == null) {
//                    open();
//                }

//                    Vector3f size = ViewportPanel.this.getSize();
////                    LoggerFactory.getLogger(this.getClass()).debug("Size: {}.", size);
//                    if(!size.equals(lastSize)) {
//                        lastSize.set(size);
//                        setViewPortSize(lastSize);
//                    }

            viewport.setEnabled(true);

            viewPortNode.updateLogicalState(tpf);

// recalculate the ViewPort size in case the Display changed its size
            if (Display.wasResized())  setViewPortSize(ViewportPanel.this.getSize());          
            
            if(autoZoom) {

                if(viewPortNode.getQuantity() > 0) {

                    Spatial child = viewPortNode.getChild(0);
                    viewPortNode.updateModelBound();

                    //FIXME: When rotating, the bounds dimension can change, making the y be bigger than the x or z and viceverse, showing an undesired zoom-in-out effect.
                    BoundingBox bb = (BoundingBox) child.getWorldBound();
                    if (bb != null) {
                        float x = bb.getXExtent();
                        float y = bb.getYExtent();
                        float z = bb.getZExtent();


                        float dimensions;

                        float bigger = x;

                        if(z > bigger) {
                            bigger = z;
                        }

                        if (y > bigger) {
                            bigger = y;
                            dimensions = cam.getFrustumTop() - cam.getFrustumBottom();
                        } else {
                            dimensions = cam.getFrustumRight() - cam.getFrustumLeft();
                        }



                        // Teoria de los triangulos semejantes
                        float distance = (bigger * cam.getFrustumNear()) / (dimensions / 2f);

//                                LoggerFactory.getLogger(this.getClass()).debug("BB center: {}, extents: {}, frustums: [b:{}, t:{}, r:{}, l:{}]distance: {}.",
//                                        bb.getCenter(),
//                                        bb.getExtent(new Vector3f()),
//                                        cam.getFrustumBottom(),
//                                        cam.getFrustumTop(),
//                                        cam.getFrustumRight(),
//                                        cam.getFrustumLeft(),
//                                        distance);



                        //TODO: Set the correct equation in relation with origin to know the perfect cam distance.
                        camOffset.set(bb.getCenter()).addLocal(camOrigin).addLocal(0, 0, distance + bigger);
                        if (!cam.getLocation().equals(camOffset)) {
//                                    LoggerFactory.getLogger(this.getClass()).debug("Setting cam location: {}.", camOffset);
                            //TODO: Smooth this movement.
                            cam.setLocation(camOffset);
                        }
                    }
                }
            }

            // Checks if the position of the ViewPanel changed, if so, the ViewPort will be set to the new location
            // otherwise our loaded models and scenes will stay fixed
            // To do so we use the method already in use for the control
            if (!(ViewportPanel.this.getWorldTranslation().equals(getlastposition()))){
                setViewPortSize(ViewportPanel.this.getSize());
                refreshLastPosition();
            }

            viewPortNode.updateGeometricState();
//                else {

//                    open();
//                }
//                LoggerFactory.getLogger(this.getClass()).debug("Geom location: {}, scale: {}.", geom.getWorldTranslation(), geom.getWorldScale());
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
//                viewPortNode.updateGeometricState();
        }
    };



    public ViewportPanel(AppStateManager stateManager) {
        this(stateManager, new ElementId(ELEMENT_ID), null);
    }

    public ViewportPanel(AppStateManager stateManager, String style) {
        this(stateManager, new ElementId(ELEMENT_ID), style);
    }

    public ViewportPanel(AppStateManager stateManager, ElementId elementid, String style) {
        this(elementid, style);

        setStateManager(stateManager);
    }

    public ViewportPanel(ElementId elementid, String style) {
        super(elementid, style);

        viewPortNode = new Node("Root Node ViewPort Panel");
        viewPortNode.setUserData(NODE_DATA, this);

//        setPreferredSize(new Vector3f(1, 1, 1)); // Patch to the first NaN size value. Try with setSize instead?

        getControl(GuiControl.class).addListener(new AbstractGuiControlListener() {
            @Override
            public void reshape(GuiControl source, Vector3f pos, Vector3f size) {
                open();
                setViewPortSize(size);
            }
        });

        addControl(new ScenegraphListenerControl() {

            @Override
            public void onAttached() {
                open();
            }

            @Override
            public void onDetached() {
                ScenegraphAutoRemoverChecker checker = viewPortNode.getControl(ScenegraphAutoRemoverChecker.class);
                while(checker != null) {
                    checker.detach();
                    checker = viewPortNode.getControl(ScenegraphAutoRemoverChecker.class);
                }

                close();
            }

            @Override
            protected ScenegraphAutoRemoverChecker createChecker() {
                return new ScenegraphAutoRemoverChecker() {
                    @Override
                    protected void controlUpdate(float tpf) {
                        super.controlUpdate(tpf);

                        if(viewport != null) {
                            viewport.setEnabled(false);
                        }
                    }
                };
            }
        });

        addControl(viewportNodeUpdater);
    }

    /**
     * If changed to public, other operations with the statemanager should be taken in account.
     * @param stateManager
     */
    private void setStateManager(AppStateManager stateManager) {
        Application app = stateManager.getApplication();

        setCam(app.getCamera().clone());
        setRenderManager(app.getRenderManager());

        this.stateManager = stateManager;
    }

    public void setCam(Camera cam) {
        this.cam = cam.clone();
        this.cam.setFrustumPerspective(40, 1, 0.05f, 500f);
//        this.cam.setLocation(Vector3f.ZERO);
        this.cam.setLocation(new Vector3f(0, 0, 10));
    }

    public void setCamPosition(Vector3f position) {
        //If autozoom, this location works as the camera origin
        if(!autoZoom) {
            this.cam.setLocation(position);
        }

        camOrigin.set(position);
    }

    public void setRenderManager(RenderManager renderManager) {
        this.renderManager = renderManager;
    }

    private void open() {
        if(viewport != null) {
            return;
        }
//        LoggerFactory.getLogger(this.getClass()).debug("Opening viewport panel");

        setViewPort(renderManager.createPostView("viewportPanel", cam));

        GuiGlobals.getInstance().setupGuiComparators(viewport);
        stateManager.getState(BasePickState.class).addCollisionRoot(viewPortNode, viewport, PickState.PICK_LAYER_GUI);

        // Ensure that it gets closed when it is detached from the scenegraph.
//        Node rootParent = null;
//        Node parent = getParent();
//        while(parent != null) {
//            rootParent = parent;
//            parent = parent.getParent();
//        }
//
//        if(rootParent != null) {
////            ViewportPanel.this.rootNode = rootParent;
//            rootParent.addControl(new AbstractControl() {
//                @Override
//                protected void controlUpdate(float tpf) {
//                    Node rootParent = null;
//                    Node parent = ViewportPanel.this.getParent();
//                    while(parent != null) {
//                        rootParent = parent;
//                        parent = parent.getParent();
//                    }
//
//                    if(rootParent != spatial) {
//                        close();
//                        spatial.removeControl(this);
//                    }
//                }
//
//                @Override
//                public void setSpatial(Spatial spatial) {
//
//
//                    if(spatial == null) {
//                        if(viewport != null) {
//                            LoggerFactory.getLogger(this.getClass()).warn("Shouldn't be removing this control manually!");
////                                        this.spatial.addControl(this); // Re-add??, or just better let the developer see the problem.
//                        }
////                                    close();
//                    }
//
//                    super.setSpatial(spatial);
//                }
//
//                @Override
//                protected void controlRender(RenderManager rm, ViewPort vp) {
//
//                }
//            });
//
//        }

        if(listeners != null) {
            for(ViewportPanelListener listener : listeners) {
                listener.onViewportCreate(viewport);
            }
        }

        refreshLastPosition();
    }





    private void close() {
//        LoggerFactory.getLogger(this.getClass()).debug("Closing viewport panel");

        clearViewport();
//        renderManager.removePostView(viewport);
//
//        stateManager.getState(BasePickState.class).removeCollisionRoot(viewport);
    }

    protected void clearViewport() {
        if(viewport == null) {
            return;
        }

        renderManager.removePostView(viewport);
        stateManager.getState(BasePickState.class).removeCollisionRoot(viewport);
//        lastSize.set(0, 0, 0);

        ViewPort removed = viewport;
        viewport = null;

        if(listeners != null) {
            for(ViewportPanelListener listener : listeners) {
                listener.onViewportRemoval(removed);
            }
        }
    }

    protected void setViewPort(ViewPort viewport) {
        clearViewport();

        this.viewport = viewport;
        if(viewport == null) {
            return;
        }

        this.viewport.setClearFlags(false, true, true);

        // This two lines shouldn't be needed... hm....
        viewPortNode.updateModelBound();
        viewPortNode.updateGeometricState();
        //////////////////////////

        this.viewport.attachScene(viewPortNode);
    }

    protected void recalculateRealTranslation() {
//        getRealWorldTranslation(this, realTransform.getTranslation());
        getRealWorldTransform(this, realTransform);
//        realTranslation.set(this.getWorldTranslation());
//
//        Spatial root = SpatialUtil.getRootFor(this);
//
//        ViewportPanel viewportPanel = root.getUserData(NODE_DATA);
//        if(viewportPanel != null) {
//            //TODO: Find a prettier way
//            if(viewportPanel instanceof ViewportPanel2D) {
//
//                Camera cam = ((ViewportPanel2D) viewportPanel).cam;
//
//                realTranslation.subtractLocal(cam.getFrustumLeft(), cam.getFrustumTop(), -10f);
//                realTranslation.divideLocal(((ViewportPanel2D) viewportPanel).rootNode.getLocalScale());
//                realTranslation.addLocal(cam.getFrustumLeft(), cam.getFrustumTop(), -10f);
//            }
//
//            realTranslation.addLocal(viewportPanel.realTranslation);
//        }
    }

    protected void setViewPortSize(Vector3f size) {
        if(viewport == null) {
            return;
        }

        Vector3f realTranslation = realTransform.getTranslation();

        // Using realTranslation as an aux to avoid a new instance creation.
        cam.lookAtDirection(realTranslation.set(Vector3f.UNIT_Z).negateLocal(), Vector3f.UNIT_Y);

//        Vector3f pos = this.getWorldTranslation();
        recalculateRealTranslation();


//        float h = Display.getHeight();
//        float w = Display.getWidth();
        float h = cam.getHeight();
        float w = cam.getWidth();

        float top    = (realTranslation.y - 10 ) / h;
        float bottom = (realTranslation.y - size.y + 10) / h;
        float left   = (realTranslation.x + 10) / w;
        float right  = (realTranslation.x + size.x - 10) / w;


//        float top    = (pos.y ) / h;
//        float bottom = (pos.y - size.y ) / h;
//        float left   = (pos.x ) / w;
//        float right  = (pos.x + size.x ) / w;

        cam.setViewPort(left, right, bottom, top);
//        cam.setFrustumPerspective(40, size.x/size.y, 0.05f, 500f);
//        viewport.getCamera().setParallelProjection(true);
        updatePerspective(size);
    }

    protected void updatePerspective(Vector3f size) {
        cam.setFrustumPerspective(40, size.x/size.y, 0.05f, 500f);
    }


    public boolean isAutoZoom() {
        return autoZoom;
    }

    public void setAutoZoom(boolean autoZoom) {
        this.autoZoom = autoZoom;
    }

    @Override
    public void addLight(Light light) {
        viewPortNode.addLight(light);
    }

    public Node getViewportNode() {
        return viewPortNode;
    }

    public void attachScene(Spatial spatial) {
        if (spatial == null) {
            throw new IllegalArgumentException( "Scene cannot be null." );
        }

//        viewPortNode.detachAllChildren();
        getViewportNode().attachChild(spatial);
//        LoggerFactory.getLogger(this.getClass()).debug("Attaching to scene");
    }

    public void detachScene(Spatial spatial) {
        if (spatial == null) {
            throw new IllegalArgumentException( "Scene cannot be null." );
        }

        getViewportNode().detachChild(spatial);
    }

    public void detachAllScenes() {
        getViewportNode().detachAllChildren();
    }


    public void addListener(ViewportPanelListener listener) {
        if(listeners == null) {
            listeners = new SafeArrayList<>(ViewportPanelListener.class);
        }

        listeners.add(listener);
    }

    public void removeListener(ViewportPanelListener listener) {
        if(listeners != null && listeners.remove(listener) && listeners.isEmpty()) {
            listeners = null;
        }
    }


    public static ViewportPanel getRootPanel(Spatial spatial) {
        return SpatialUtil.getRootFor(spatial).getUserData(NODE_DATA);
    }

    public static Transform getRealWorldTransform(Spatial spatial, Transform store) {
        if(store == null) {
            store = new Transform();
        }

        store.set(spatial.getWorldTransform());

        Spatial root = SpatialUtil.getRootFor(spatial);

        ViewportPanel viewportPanel = root.getUserData(NODE_DATA);
        if(viewportPanel != null) {
            Vector3f translation = store.getTranslation();
            Quaternion rotation = store.getRotation();
//            Vector3f scale = store.getScale();

            //TODO: Find a prettier way
            if(viewportPanel instanceof ViewportPanel2D) {



                Camera cam = ((ViewportPanel2D) viewportPanel).cam;
                Node node2d = ((ViewportPanel2D) viewportPanel).rootNode;

                translation.subtractLocal(cam.getFrustumLeft(), cam.getFrustumTop(), -10f);


                translation.divideLocal(node2d.getLocalScale());
//                rotation.multLocal(node2d.getLocalRotation());
//                scale.multLocal(node2d.getLocalScale());

                translation.addLocal(cam.getFrustumLeft(), cam.getFrustumTop(), -10f);
            }

            translation.addLocal(viewportPanel.realTransform.getTranslation());
            rotation.multLocal(viewportPanel.realTransform.getRotation());
//            scale.multLocal(viewportPanel.realTransform.getScale());


//            store.combineWithParent(viewportPanel.realTransform);
        }

        return store;
    }

    public static Vector3f getRealWorldScale(Spatial spatial, Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        }

        // Yes, it is the same
        store.set(spatial.getWorldScale());

        return store;
    }

    public static Vector3f getRealWorldTranslation(Spatial spatial, Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        }

        store.set(spatial.getWorldTranslation());

        Spatial root = SpatialUtil.getRootFor(spatial);

        ViewportPanel viewportPanel = root.getUserData(NODE_DATA);
        if(viewportPanel != null) {
            //TODO: Find a prettier way
            if(viewportPanel instanceof ViewportPanel2D) {

                Camera cam = ((ViewportPanel2D) viewportPanel).cam;

                // We first remove the frustum extensions that affect to the translation
                store.subtractLocal(cam.getFrustumLeft(), cam.getFrustumTop(), -10f);

                // We apply the inverse scale to the remaining translation
                store.divideLocal(((ViewportPanel2D) viewportPanel).rootNode.getLocalScale());

                // We read the removed extensions
                store.addLocal(cam.getFrustumLeft(), cam.getFrustumTop(), -10f);
            }

            store.addLocal(viewportPanel.realTransform.getTranslation());
        }

        return store;
    }

    public static void depthFirstTraversal(Spatial spatial, final SceneGraphVisitor visitor) {
        SceneGraphVisitor vpVisitor = new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                if(spatial instanceof ViewportPanel) {
                    ((ViewportPanel) spatial).getViewportNode().depthFirstTraversal(this);
                }

                visitor.visit(spatial);
            }
        };

        spatial.depthFirstTraversal(vpVisitor);
    }

  private void refreshLastPosition() {
        lastPosition.set(ViewportPanel.this.getWorldTranslation());
    }

    private Vector3f getlastposition(){
        return lastPosition;
    }


}

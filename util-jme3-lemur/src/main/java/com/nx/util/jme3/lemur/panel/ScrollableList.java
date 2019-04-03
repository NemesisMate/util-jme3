package com.nx.util.jme3.lemur.panel;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.nx.util.jme3.lemur.BasicMouseListener;
import com.nx.util.jme3.lemur.listener.ScrollMouseListener;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.AbstractGuiControlListener;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;
import com.simsilica.lemur.event.ConsumingMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.event.MouseListener;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Styles;

import java.util.Collection;

/**
 * Created by NemesisMate on 20/02/17.
 */
//TODO: Move to lemur util
public class ScrollableList extends Container {

    public static final String ELEMENT_ID = "scrollableList";
    public static final String LIST_ID = "list";
//    public static final String HEADER_ID = "header";
//    public static final String FOOTER_ID = "footer";

    Container list;
    boolean pressingList;
    //    boolean listAtOrigin;
//    float lastX = 0;
//    float lastY = 0;
//    float offseted = 0;

    //TODO: Change to use ScrollMouseListener
//    public enum ScrollMode {
//        X, Y, BOTH
//    }

//    ScrollMode scrollMode = ScrollMode.BOTH;
//    boolean switchXandY;
//    private InputState inputState;


    private ScrollMouseListener listMoveListener = new ScrollMouseListener() {
        @Override
        protected void onScroll(MouseMotionEvent event, int dx, int dy) {
            list.move(dx, dy, 0);
        }
    };

    protected MouseListener patchListener = new BasicMouseListener() {
        @Override
        public void mouseButtonEvent(MouseButtonEvent mbe, Spatial sptl, Spatial sptl1) {
            listMoveListener.mouseButtonEvent(mbe, sptl, sptl1);
        }
    };





//    protected MouseListener listMoveListener = new BasicMouseListener() {
//
//        float lastY = 0;
//        float lastX = 0;
//
//        @Override
//        public void mouseButtonEvent(MouseButtonEvent event, Spatial target, Spatial capture) {
//            super.mouseButtonEvent(event, target, capture);
//
////            float y = event.getY();
////            float dy = lastY - y;
//
////            float x = event.getX();
////            float dx = lastX - x;
//
//            lastX = event.getX();
//            lastY = event.getY();
//
//
////            System.out.println("Mouse pressed: " + pressingList + ", moved: " + dx + ", " + dy);
//
////            if(target != null && event.isReleased()) {
////                cancelBuild();
////            }
//        }
//
//        @Override
//        public void mouseMoved(MouseMotionEvent event, Spatial target, Spatial capture) {
////            System.out.println("Mouse moving");
//            if(!pressingList) {
//                return;
//            }
//
//            event.setConsumed();
//
//            float y = event.getY();
//            float dy = lastY - y;
//            lastY = y;
//
//            float x = event.getX();
//            float dx = lastX - x;
//            lastX = x;
//
//
//            if(switchXandY) {
//                float aux = dx;
//                dx = dy;
//                dy = aux;
//            }
//
//            switch (scrollMode) {
//                case X:
//                    dy = 0;
//                    break;
//                case Y:
//                    dx = 0;
//                    break;
//            }
//
//            list.move(-dx, -dy, 0);
//        }
//
//    };

    public ScrollableList() {
        this(new ElementId(ELEMENT_ID), null);
//        inputState = Main.app.getStateManager().getState(InputState.class);


//        Texture tex = UiParams.getAtlasFor(UiParams.AtlasType.UI);//GuiGlobals.getItem().loadTexture("Textures/Atlas.png", false, true);
//        Image img = tex.getImage();
//
//
////        listPlaceholder.setBackground(new TbtQuadBackgroundComponent(new TbtQuad(1024, 1024, 320, 320, 640, 640, img.getWidth(), img.getHeight(), .1f), tex));
////        listPlaceholder.setBackground(TbtQuadBackgroundComponent.create(tex, 1, 320, 320, 640, 640, 0, false));
//
////        AutoTbtQuadBackgroundComponent backgroundComponent = new AutoTbtQuadBackgroundComponent(new AutoTbtQuad(672, 863, 32, 32, 0, 0, 32, 20, img.getWidth(), img.getHeight(), UiUtils.dp(.7f)), tex);
//        AutoTbtQuadBackgroundComponent backgroundComponent = new AutoTbtQuadBackgroundComponent(new AutoTbtQuad(UiParams.scrollableListBackground, img.getWidth(), img.getHeight(), UiUtils.dp(.7f)), tex);
//        this.setBackground(backgroundComponent);

//        this.setLocalScale(1, 1, 2);
    }



    public ScrollableList(ElementId elementId) {
        this(elementId, null);
    }

    public ScrollableList(String style) {
        this(new ElementId(ELEMENT_ID), style);
    }

    public ScrollableList(ElementId elementId, String style) {
        super(new SpringGridLayout(), elementId, style);
//        super(elementId = UiHelper.typeId(elementId, ELEMENT_ID), style);

        setupList(elementId, null);

//        Styles styles = GuiGlobals.getInstance().getStyles();
//        styles.applyStyles(this, elementId.getId(), style);
    }


    /**
     * Use y to move x.
     * @param switchXandY
     */
    public void setSwitchXandY(boolean switchXandY) {
//        this.switchXandY = switchXandY;
        this.listMoveListener.setSwitchXandY(switchXandY);
    }

    public void setScrollMode(ScrollMouseListener.ScrollMode scrollMode) {
//        this.scrollMode = scrollMode;
        this.listMoveListener.setScrollMode(scrollMode);
    }

    public void setListLayout(GuiLayout layout) {
        for(Node child : list.getLayout().getChildren()) {
            layout.addChild(child);
        }

        list.setLayout(layout);
    }

    private void setupList(ElementId elementId, String style) {
        this.setCullHint(Spatial.CullHint.Dynamic);

        list = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.None, FillMode.Even), elementId.child(LIST_ID), style);
//        list.setBackground(new QuadBackgroundComponent(ColorRGBA.Yellow));

        Node node = new Node();
        this.attachChild(node); // Not added, attached. More freedom to go out the screen.
        final Container c = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Proportional, FillMode.Even), style);
        node.attachChild(c);
        c.setBackground(new QuadBackgroundComponent(ColorRGBA.BlackNoAlpha));

//        c.setBackground(null);
//        c.setBackground(new QuadBackgroundComponent(ColorRGBA.Blue));

//        System.out.println("ELEMENTID SCROLL: " + elementId);
//        System.out.println("ELEMENTID SCROLL HEADER: " + elementId.child(HEADER_ID));

//        c.addChild(new Panel(elementId.child(HEADER_ID), style));//.setPreferredSize(new Vector3f(100, 18, 1)); // top offset
//        c.addChild(list);//.setPreferredSize(new Vector3f(100, 72, 1));
        node.attachChild(list);//.setPreferredSize(new Vector3f(100, 72, 1));
//        c.addChild(new Panel(elementId.child(FOOTER_ID), style));//.setPreferredSize(new Vector3f(100, 10, 1)); // bottom offset


//        list.setInsets(new Insets3f(35, 0, 5, 0));


//        Main.app.getStateManager().getState(UIState.class).getHeight();

//        list.setPreferredSize(((Panel) listPlaceholder).getPreferredSize().clone());
//        this.getControl(GuiControl.class).addComponent(new AbstractGuiComponent() {
//            @Override
//            public void calculatePreferredSize(Vector3f size) {
//
//            }
//
//            @Override
//            public void reshape(Vector3f pos, Vector3f size) {
//                c.setPreferredSize(size);
//            }
//        });

        this.getControl(GuiControl.class).addListener(new AbstractGuiControlListener() {
            @Override
            public void reshape(GuiControl source, Vector3f pos, Vector3f size) {
                c.setPreferredSize(size);
                list.setPreferredSize(size);
            }
        });




//        CursorEventControl.addListenersToSpatial(listPlaceholder, new DefaultCursorListener() {
//
//            @Override
//            public void cursorButtonEvent(CursorButtonEvent event, Spatial target, Spatial capture) {
//                // Consumming event
//                super.cursorButtonEvent(event, target, capture);
//
//
//                pressingList = event.isPressed();
//
//            }
//
//            @Override
//            public void cursorMoved(CursorMotionEvent event, Spatial target, Spatial capture) {
//                System.out.println("EVENT" + event.hashCode());
//            }
//        });


        list.addControl(this.listMoveListener.createControl(list));

//        list.addControl(new AbstractControl() {
////            Planet lastPlanet;
////            int lastPlanetVersion;
//
//            @Override
//            protected void controlUpdate(float tpf) {
//                // List not at origin
////                System.out.println("ZINDEX1: " + listPlaceholder.getWorldTranslation() + ", BG: " + listPlaceholder.getBackground());
////                System.out.println("ZINDEX2: " + header.getWorldTranslation());
//
//                if(pressingList) {
//                    return;
//                }
////                    c.updateModelBound();
//                BoundingBox bb = (BoundingBox) list.getWorldBound();
//                if(bb == null) {
//                    return;
//                }
//
//                //TODO: With y-axis too
//
//                float x = list.getLocalTranslation().x;
//
//                float xSize = switchXandY ? bb.getYExtent() : bb.getXExtent() * 2;
//
//
//
//                float fixedSize = list.getSize().x;
////                    float fixedSize = ((BoundingBox)ScrollableList.this.getWorldBound()).getYExtent();
//
//                float extraSize = 0;
//
//                if(xSize > fixedSize) {
//                    extraSize = (xSize - fixedSize);
//                }
//
//                if(x > 0 || extraSize == 0 || x < -extraSize) {
////                    if(x != 0) {
////                        float offset = -lastY * tpf;
////                        lastY += offset;
//
//                    if (x < 0) {
//                        x += extraSize;
//                    }
////                        list.move((x >= extraSize && x <= extraSize + 1) || (x < extraSize && x >= extraSize -1) ? -x : -x * 10 * tpf, 0, 0);
//                    list.move((x >= 0 && x <= 1) || (x < 0 && x >= -1) ? -x : -x * 10 * tpf, 0, 0);
////                        list.move(-x * 10 * tpf, 0, 0);
//                }
//
//                onListUpdate();
//            }
//
//            @Override
//            protected void controlRender(RenderManager rm, ViewPort vp) {
//
//            }
//        });

        MouseEventControl.addListenersToSpatial(this, listMoveListener, ConsumingMouseListener.INSTANCE);
//        MouseEventControl.addListenersToSpatial(this, new DefaultMouseListener() {
//            @Override
//            public void mouseMoved(MouseMotionEvent event, Spatial target, Spatial capture) {
//                super.mouseMoved(event, target, capture);
//
//                System.out.println("ASDASD");
//            }
//        });
//        CursorEventControl.addListenersToSpatial(this, new DefaultCursorListener() {
//            @Override
//            public void cursorMoved(CursorMotionEvent event, Spatial target, Spatial capture) {
//                super.cursorMoved(event, target, capture);
//
//                event.setConsumed();
//            }
//        });
    }

    public void clearContents() {
        list.clearChildren();
    }

    //TODO: Why I'm not overriding the addChild directly?


    /**
     * Maybe you must override this method to add the patch:
     *
     *
     * Main.getInstance().enqueue(new Callable<Object>() {
     *     @Override public Object call() throws Exception {
     *         child.depthFirstTraversal(patchVisitor);
     *         return null;
     *     }
     * });
     *
     *
     * @param child
     * @param <T>
     * @return
     */
    public <T extends Node> T addContent(final T child) {
        list.addChild(child);

//        MouseEventControl.addListenersToSpatial(child, patchListener);
//        Main.getInstance().enqueue(new Callable<Object>() {
//            @Override
//            public Object call() throws Exception {
//                child.depthFirstTraversal(patchVisitor);
//                return null;
//            }
//        });


        return child;
    }

    public Collection<Node> getContents() {
        return list.getLayout().getChildren();
    }

    protected void onListUpdate() {

    }

    public Container getList() {
        return list;
    }

//    public Collection<Node> getLayoutChildren() {
//        return list.getLayout().getChildren();
//    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles(Styles styles, Attributes attrs ) {
//        attrs = styles.getSelector(ELEMENT_ID, HEADER_ID);
//        attrs.set("preferredSize", new Vector3f(100, 18, 1));

        attrs = styles.getSelector(ELEMENT_ID, LIST_ID);
        attrs.set("preferredSize", new Vector3f(100, 72, 1));

//        attrs = styles.getSelector(ELEMENT_ID, FOOTER_ID);
//        attrs.set("preferredSize", new Vector3f(100, 10, 1));
    }
}

package com.nx.util.jme3.lemur;

import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import com.nx.util.jme3.base.ScenegraphListenerControl;
import com.nx.util.jme3.lemur.layout.CenterAlignLayout;
import com.nx.util.jme3.lemur.layout.WrapperLayout;
import com.nx.util.jme3.lemur.panel.ViewportPanel;
import com.nx.util.jme3.lemur.panel.ViewportPanel2D;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.LayerComparator;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.anim.PanelTweens;
import com.simsilica.lemur.anim.TweenAnimation;
import com.simsilica.lemur.anim.Tweens;
import com.simsilica.lemur.component.InsetsComponent;
import com.simsilica.lemur.core.AbstractGuiControlListener;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;
import com.simsilica.lemur.effect.AbstractEffect;
import com.simsilica.lemur.effect.Effect;
import com.simsilica.lemur.effect.EffectInfo;
import com.simsilica.lemur.style.ElementId;

/**
 * Created by NemesisMate on 15/05/17.
 */
public final class LemurHelper {

    private LemurHelper() {

    }

    /**
     * Warning: normally this isn't the desired effect, but start to fade out from the current alpha value instead of 1.
     */
    public static Effect<Panel> fadeOut = new AbstractEffect<Panel>("fade-In/Out") {
        @Override
        public Animation create(Panel target, EffectInfo existing) {
            return new TweenAnimation(Tweens.smoothStep(PanelTweens.fade(target, 1f, 0f, .2)));
        }
    };

    /**
     * Warning: normally this isn't the desired effect, but start to fade in from the current alpha value instead of 0.
     */
    public static Effect<Panel> fadeIn = new AbstractEffect<Panel>("fade-In/Out") {
        @Override
        public Animation create(Panel target, EffectInfo existing) {
            return new TweenAnimation(Tweens.smoothStep(PanelTweens.fade(target, 0f, 1f, .2)));
        }
    };

    public static Container addAlignedContainer(Container parent) {
        return parent.addChild(new Container(new CenterAlignLayout()));
    }

    public static Container addWrapperContainer(Container parent) {
        return parent.addChild(new Container(new WrapperLayout()));
    }

    public static Container addContainer(Container parent, GuiLayout... layouts) {
        for(GuiLayout layout : layouts) {
            parent = parent.addChild(new Container(layout));
        }

        return parent;
    }

    public static ElementId childId(Container parent, String id) {
        return parent.getElementId().child(id);
    }

    public static <T extends Panel> T addChildIfNotParented(Container container, T child) {
        if(child.getParent() == null) { container.addChild(child); } return child;
    }

    public static <T extends Panel> T addChildIfDiffParent(Container container, T child) {
        if(child.getParent() != container) { container.addChild(child); } return child;
    }

    public static <T extends Panel> T addChild(Container container, T child) {
        container.addChild(child); return child;
    }

    public static <T extends Panel> T addChild(Container container, T child, float x, float y, float top, float left, float bottom, float right) {
        return addChild(container, child, new Vector3f(x, y, 1), new Insets3f(top, left, bottom, right));
    }

    public static <T extends Panel> T addChild(Container container, T child, float x, float y, float z, float top, float left, float bottom, float right) {
        return addChild(container, child, new Vector3f(x, y, z), new Insets3f(top, left, bottom, right));
    }

    public static <T extends Panel> T addChild(Container container, T child, float x, float y, Insets3f insets) {
        return addChild(container, child, new Vector3f(x, y, 1), insets);
    }

    public static <T extends Panel> T addChild(Container container, T child, float x, float y, float z, Insets3f insets) {
        return addChild(container, child, new Vector3f(x, y, z), insets);
    }

    public static <T extends Panel> T addChild(Container container, T child, float x, float y) {
        return addChild(container, child, new Vector3f(x, y, 1));
    }

    public static <T extends Panel> T addChild(Container container, T child, float x, float y, float z) {
        return addChild(container, child, new Vector3f(x, y, z));
    }

    public static <T extends Panel> T addChild(Container container, T child, float top, float left, float bottom, float right) {
        return addChild(container, child, new Insets3f(top, left, bottom, right));
    }

    public static <T extends Panel> T addChild(Container container, T child, Insets3f insets) {
        container.addChild(child);
        child.setInsets(insets);
        return child;
    }

    public static <T extends Panel> T addChild(Container container, T child, Vector3f prefSize) {
        container.addChild(child);
        child.setPreferredSize(prefSize);
        return child;
    }

    public static <T extends Panel> T addChild(Container container, T child, Vector3f prefSize, Insets3f insets) {
        addChild(container, child, prefSize).setInsets(insets);
        return child;
    }

    public static <T extends Panel> T setProps(T panel, float x, float y, float z, float top, float left, float bottom, float right) {
        return setProps(panel, new Vector3f(x, y, z), new Insets3f(top, left, bottom, right));
    }

    public static <T extends Panel> T setProps(T panel, float x, float y, float top, float left, float bottom, float right) {
        return setProps(panel, new Vector3f(x, y, 1), new Insets3f(top, left, bottom, right));
    }

    public static <T extends Panel> T setProps(T panel, Vector3f prefSize, Insets3f insets) {
        panel.setPreferredSize(prefSize);
        panel.setInsets(insets);
        return panel;
    }

    public static <T extends Panel> T setPrefSize(T panel, float xy) {
        return setPrefSize(panel, xy, xy, 1);
    }

    public static <T extends Panel> T setPrefSize(T panel, float x, float y) {
        return setPrefSize(panel, x, y, 1);
    }

    public static <T extends Panel> T setPrefSize(T panel, float x, float y, float z) {
        panel.setPreferredSize(new Vector3f(x, y, z));
        return panel;
    }

    public static <T extends Panel> T setInsets(T panel, float top, float left, float bottom, float right) {
        panel.setInsets(new Insets3f(top, left, bottom, right));
        return panel;
    }

    public static <T extends Panel> T setInsets(T panel, float topBottom, float leftRight) {
        return setInsets(panel, topBottom, leftRight, topBottom, leftRight);
    }

    public static <T extends Panel> T setInsets(T panel, float topBottomLeftRight) {
        return setInsets(panel, topBottomLeftRight, topBottomLeftRight, topBottomLeftRight, topBottomLeftRight);
    }

    public static <T extends Panel> T addToPanel(Panel parent, T child) {
        return addToPanel(parent, child, Vector3f.ZERO);
    }

    public static <T extends Panel> T addToPanel(Panel parent, T child, Vector3f offset) {
        final Node node = new Node();
        parent.attachChild(node);
        node.attachChild(child);

        child.addControl(new ScenegraphListenerControl() {
            @Override
            public void onAttached() { }

            @Override
            public void onDetached() {
                if(spatial.getParent() == null) {
                    node.removeFromParent();
                }
            }
        });

        node.setLocalTranslation(offset);

        child.move(0, 0, 1);

        return child;
    }

    /**
     * WARNING: Can't currently be undone.
     *
     * Attaches a panel to another one given their attachment points.
     * @param parent
     * @param child
     * @param offsetPercent x,y => point in the parent panel where to attach the child. z, w => point in the child panel to be attached from.
     * @param <T>
     * @return the given child.
     */
    public static <T extends Panel> T addToPanel(Panel parent, final T child, Vector4f offsetPercent) {
        addToPanel(parent, child);

        addPopupListeners(parent, child, offsetPercent);

        GuiControl parentControl = parent.getControl(GuiControl.class);
        parentControl.addListener(new AbstractGuiControlListener() {
            @Override
            public void reshape(GuiControl source, Vector3f pos, Vector3f size) {
                super.reshape(source, pos, size);

                child.getControl(GuiControl.class).invalidate();
            }
        });

        parentControl.invalidate();

        return child;
    }

    /**
     * WARNING: Can't currently be undone.
     *
     * @param parent
     * @param layer
     * @param <T>
     * @return
     */
    public static <T extends Panel> T addLayerToPanel(Panel parent, final T layer) {
        return addLayerToPanel(parent, layer, Vector3f.UNIT_XYZ);
    }

    /**
     * WARNING: Can't currently be undone.
     *
     * @param parent
     * @param layer
     * @param scale
     * @param <T>
     * @return
     */
    public static <T extends Panel> T addLayerToPanel(Panel parent, T layer, Vector3f scale) {
        addToPanel(parent, layer);

        addLayerListeners(parent, layer, scale);

        return layer;
    }

    /**
     * WARNING: Can't currently be undone.
     *
     * @param parent
     * @param layer
     * @param offsetPercent
     * @param <T>
     * @return
     */
    public static <T extends Panel> T addLayerToPanel(Panel parent, T layer, Vector4f offsetPercent) {
        return addLayerToPanel(parent, layer, Vector3f.UNIT_XYZ, offsetPercent);
    }

    /**
     * WARNING: Can't currently be undone.
     *
     * @param parent
     * @param layer
     * @param scale
     * @param offsetPercent
     * @param <T>
     * @return
     */
    public static <T extends Panel> T addLayerToPanel(Panel parent, T layer, Vector3f scale, Vector4f offsetPercent) {
        addToPanel(parent, layer);

        addLayerListeners(parent, layer, scale);
        addPopupListeners(parent, layer, offsetPercent);

        return layer;
    }

    public static <T extends Panel> T addLayerToPanelViewported(Panel parent, final T layer, AppStateManager stateManager) {
        return addLayerToPanelViewported(parent, layer, Vector3f.UNIT_XYZ, stateManager);
    }

    /**
     * WARNING: Can't currently be undone.
     *
     * @param parent
     * @param layer
     * @param stateManager
     * @param <T>
     * @return
     */
    public static <T extends Panel> T addLayerToPanelViewported(Panel parent, final T layer, Vector3f scale, AppStateManager stateManager) {
        final ViewportPanel viewportPanel = new ViewportPanel2D(stateManager, new ElementId("Layer"), null);
        viewportPanel.attachScene(layer);

        addToPanel(parent, viewportPanel);
        addLayerListeners(parent, viewportPanel, scale);
        addLayerListeners(viewportPanel, layer, Vector3f.UNIT_XYZ);

        return layer;
    }

    private static void addPopupListeners(final Panel parent, final Panel child, final Vector4f offsetPercent) {
        // Remember to add a layerListener to the parent (or equivalent) where colling this. The parent guiControl shall be invalidated too.

        child.getControl(GuiControl.class).addListener(new AbstractGuiControlListener() {
            @Override
            public void reshape(GuiControl source, Vector3f pos, Vector3f size) {
                super.reshape(source, pos, size);

                Spatial spatial = source.getSpatial();

                Vector3f aux = spatial.getLocalTranslation().set(parent.getSize());

                InsetsComponent insetsComponent = parent.getInsetsComponent();
                if(insetsComponent != null) {
                    TempVars vars = TempVars.get();
                    Vector3f position = vars.vect1.set(Vector3f.ZERO);

                    insetsComponent.reshape(position, aux);

                    aux.multLocal(offsetPercent.x, -offsetPercent.y, 1);
                    aux.addLocal(position.x, position.y, 0);

                    vars.release();
                } else {
                    aux.multLocal(offsetPercent.x, -offsetPercent.y, 1);
                }

                spatial.setLocalTranslation(aux.subtractLocal(size.x * offsetPercent.z, -size.y * offsetPercent.w, -pos.z));
            }
        });
    }

    private static void addLayerListeners(Panel parent, final Panel layer, final Vector3f scale) {
        GuiControl parentControl = parent.getControl(GuiControl.class);

        parentControl.addListener(new AbstractGuiControlListener() {
            @Override
            public void reshape(GuiControl source, Vector3f pos, Vector3f size) {
                Vector3f prefSize = layer.getPreferredSize().set(size);
                Vector3f position = layer.getLocalTranslation().set(0, 0, pos.z + 1);

                InsetsComponent insetsComponent = ((Panel)source.getSpatial()).getInsetsComponent();
                if(insetsComponent != null) {
                    insetsComponent.reshape(position, prefSize);
                }

                float x = prefSize.x;
                float y = prefSize.y;

                prefSize.multLocal(scale);

                layer.setPreferredSize(prefSize);
                layer.setLocalTranslation(position.addLocal(-(prefSize.x - x) / 2f, (prefSize.y - y) / 2f, 0));
            }
        });

        parentControl.invalidate();
    }

    public static void offsetLayer(Panel layer, final Vector3f offset) {
        layer.getControl(GuiControl.class).addListener(new AbstractGuiControlListener() {
            @Override
            public void reshape(GuiControl source, Vector3f pos, Vector3f size) {
                source.getSpatial().move(offset);
            }
        });
    }

    public static void layerAlign(Panel parent, Panel child, boolean front) {
        Integer layer = LayerComparator.getLayer(parent);

        int offset = front ? 1 : -1;
        if(layer != null) {
            LayerComparator.setLayer(child, layer + offset);
        }

        child.getLocalTranslation().setZ(parent.getLocalTranslation().getZ() + offset);
    }

}

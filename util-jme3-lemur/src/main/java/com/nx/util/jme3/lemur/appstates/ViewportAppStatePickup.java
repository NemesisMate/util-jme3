package com.nx.util.jme3.lemur.appstates;

import com.jme3.app.Application;
import com.jme3.scene.Node;
import com.nx.util.jme3.base.appstates.ViewportAppState;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.PickState;

import java.lang.reflect.Array;

public class ViewportAppStatePickup extends ViewportAppState {

    public static final String PICK_LAYER_SCENEGUIOVERLAY = "sceneGuiOverlay";

    private int layerIndex;
    private String layerName;

    public ViewportAppStatePickup() {
        this(Mode.MAIN, 1);
    }

    public ViewportAppStatePickup(Mode mode) {
        this(mode, 1);
    }

    public ViewportAppStatePickup(Mode mode, int layerIndex) {
        super(mode, PICK_LAYER_SCENEGUIOVERLAY + "_" + layerIndex);

        if(layerIndex < 0) {
            throw new IllegalArgumentException("Index must be a positive number");
        }

        this.rootNode = new Node(PICK_LAYER_SCENEGUIOVERLAY);

        this.layerName = PICK_LAYER_SCENEGUIOVERLAY + "_" + layerIndex;
        this.layerIndex = layerIndex;
    }



    @Override
    protected void initialize(Application app) {
//        camera = app.getCamera();//.clone();
        super.initialize(app);

        viewPort.setClearFlags(false, true, false);

        GuiGlobals guiGlobals = GuiGlobals.getInstance();
        if(guiGlobals != null) {
            guiGlobals.setupGuiComparators(viewPort);
        }
    }

    @Override
    protected void onEnable() {
        super.onEnable();

        if(GuiGlobals.getInstance() != null) {
            PickState pickState = getState(PickState.class);
            pickState.addCollisionRoot(viewPort, layerName);

            String[] newOrder = addToArray(pickState.getPickLayerOrder(), layerName, layerIndex);

            pickState.setPickLayerOrder(newOrder);
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if(GuiGlobals.getInstance() != null) {
            PickState pickState = getState(PickState.class);
            pickState.removeCollisionRoot(viewPort);

            String[] oldOrder = pickState.getPickLayerOrder();
            String[] newOrder = new String[oldOrder.length - 1];

            int i = 0;
            for(String name : oldOrder) {
                if(!name.equals(layerName)) {
                    newOrder[i++] = name;
                }
            }

            pickState.setPickLayerOrder(newOrder);
        }
    }


    private static <T> T[] addToArray(T[] array, T object, int index) {
        int oldLength = array.length;
        int newLength = oldLength + 1;

        if(index > oldLength) {
            index = oldLength;
        }

        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), newLength);

        if(index > 0) {
            System.arraycopy(array, 0, newArray, 0, index);
        }

        newArray[index] = object;

        if(index < oldLength) {
            System.arraycopy(array, index, newArray, index+1, oldLength - index);
        }

        return newArray;
    }
}

package com.nx.util.cross;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.TouchInput;
import com.jme3.input.controls.TouchListener;
import com.jme3.input.controls.TouchTrigger;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.Vector2f;
import com.jme3.util.SafeArrayList;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.Axis;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;

/**
 * Created by NemesisMate on 7/06/17.
 */
public class CrossInputAppState extends BaseAppState {

    public interface ZoomListener {
        void onZoom(float delta);
        void onMove(float dx, float dy);
    }

    private AnalogFunctionListener wheelListener;

//    Vector2f mousePosition = new Vector2f();
    Vector2f mousePosition;

    // This is here instead of in the CamLayerAppState because this must be registered before lemur's.
    private MouseMoveRawListener mouseMoveRawListener;

    private SafeArrayList<ZoomListener> listeners = new SafeArrayList<>(ZoomListener.class);

    private EventPatch eventPatch = EventPatch.DEFAULT;

    private boolean wheelEnabled;
    private boolean touchEnabled;

    private static final FunctionId f = new FunctionId("wheel");

    private TouchTrigger touchTrigger;
    private TouchListener touchListener;

    public interface EventPatch {
        EventPatch DEFAULT = new EventPatch() {
            @Override
            public boolean cancelEvent() {
                return false;
            }
        };

        boolean cancelEvent();
    }

    public CrossInputAppState(InputManager inputManager, boolean wheelEnabled, boolean touchEnabled) {
        mouseMoveRawListener = new MouseMoveRawListener () {

            @Override
            public void onMouseButtonEvent(MouseButtonEvent event) {
                if(eventPatch.cancelEvent()) {
                    return;
                }

                super.onMouseButtonEvent(event);
            }

            @Override
            protected void onMove(MouseMotionEvent event, int dx, int dy) {
                super.onMove(event, dx, dy);

                //TODO: Use a listener, like with zoomListener
                move(dx, dy);
            }
        };

        inputManager.addRawInputListener(mouseMoveRawListener);

        this.wheelEnabled = wheelEnabled;
        this.touchEnabled = touchEnabled;
    }

    @Override
    protected void initialize(Application app) {

        mouseMoveRawListener.setEnabled(false);

        setWheelEnabled(wheelEnabled);
        setTouchEnabled(touchEnabled);

        mousePosition = app.getInputManager().getCursorPosition();
    }

    public void setTouchEnabled(boolean enabled) {
        if(!isInitialized()) {
            return;
        }

        InputManager inputManager = getApplication().getInputManager();

        if(enabled) {
            if(touchTrigger != null) {
                return;
            }

            touchTrigger = new TouchTrigger(TouchInput.ALL);
            touchListener = new TouchListener() {
                @Override
                public void onTouch(String name, TouchEvent event, float tpf) {
                    if(eventPatch.cancelEvent()) {
                        return;
                    }


                    if (event.getType() == TouchEvent.Type.SCALE_MOVE) {

//
//
////                        System.out.println("ZOOMING: " + event.getDeltaScaleSpan() + ", FACTOR: " + event.getScaleFactor());
//                        CamLayerAppState camLayerAppState = getState(CamLayerAppState.class);
//                        // It could be that the state isn't initiated yet
//                        if(camLayerAppState != null) {
//                            camLayerAppState.cancelDragToMove();
////                            camLayerAppState.zoom(delta);
//                        }

                        cancelDragToMove();

                        zoom(event.getDeltaScaleSpan() * event.getScaleFactor() * .1f);
                    }
                }
            };

            inputManager.addMapping("zoom", touchTrigger);
            inputManager.addListener(touchListener, "zoom");
        } else {
            if(touchTrigger == null) {
                return;
            }

            inputManager.deleteTrigger("zoom", touchTrigger);
            inputManager.removeListener(touchListener);

            touchTrigger = null;
            touchListener = null;
        }
    }

    public void setWheelEnabled(boolean enabled) {
        this.wheelEnabled = enabled;

        if(!isInitialized()) {
            return;
        }

        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();

        if(enabled) {
            if(wheelListener == null) {
                wheelListener = new AnalogFunctionListener() {
                    @Override
                    public void valueActive(FunctionId func, double value, double tpf) {
                        if(eventPatch.cancelEvent()) {
                            return;
                        }

                        // We don't want to have the scroll depending on tpf
                        zoom((float) value);
                    }
                };

                inputMapper.map(f, Axis.MOUSE_WHEEL);
                inputMapper.addAnalogListener(wheelListener, f);
            }

        } else {
            if(wheelListener != null) {
                inputMapper.removeMapping(f, Axis.MOUSE_WHEEL);
                inputMapper.removeAnalogListener(wheelListener, f);

                wheelListener = null;
            }
        }


    }

    public void setInputOverride(EventPatch eventPatch) {
        this.eventPatch = eventPatch;
    }

    public final void cancelDragToMove() {
        mouseMoveRawListener.setPressed(false);
    }

    private void move(float dx, float dy) {
        for(ZoomListener zoomListener : listeners.getArray()) {
            zoomListener.onMove(dx, dy);
        }
    }

    private void zoom(float delta) {
        for(ZoomListener zoomListener : listeners.getArray()) {
            zoomListener.onZoom(delta);
        }
    }

    @Override
    protected void cleanup(Application app) {
        setWheelEnabled(false);

//        selected = null;
//        mouseMoveRawListener = null;
        mousePosition = null;
        wheelListener = null;
    }

    public Vector2f getMousePosition() {
        return mousePosition;
    }

    public void setMousePosition(Vector2f position) {
        setMousePosition(position.x, position.y);
    }

    public void setMousePosition(float x, float y) {
        this.mousePosition.set(x, y);
    }


    public void addZoomListener(ZoomListener zoomListener) {
        if(!listeners.contains(zoomListener)) {
            listeners.add(zoomListener);
        }

        mouseMoveRawListener.setEnabled(true);
    }

    public void removeZoomListener(ZoomListener zoomListener) {
        listeners.remove(zoomListener);
        if(listeners.size() == 0) {
            mouseMoveRawListener.setEnabled(false);
        }
    }


    @Override
    protected void onEnable() {
        mouseMoveRawListener.setEnabled(listeners.size() > 0);
    }

    @Override
    protected void onDisable() {
        mouseMoveRawListener.setEnabled(false);
    }
}

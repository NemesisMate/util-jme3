package com.nx.util.cross;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.simsilica.lemur.event.DefaultRawInputListener;

/**
 * Created by NemesisMate on 17/06/17.
 */
public class MouseMoveRawListener extends DefaultRawInputListener {

    private boolean enabled;

    private int xDown;
    private int yDown;
    private int xClickThreshold;
    private int yClickThreshold;

    private int xLast;
    private int yLast;

    private boolean moving;
    private boolean pressed;

    public MouseMoveRawListener() {
        this(10, 10);
    }

    public MouseMoveRawListener(int xClickThreshold, int yClickThreshold ) {
        this.xClickThreshold = xClickThreshold;
        this.yClickThreshold = yClickThreshold;
    }

    protected boolean isMoving( MouseMotionEvent event, int xDown, int yDown ) {
        int x = event.getX();
        int y = event.getY();

        return Math.abs(x-xDown) > xClickThreshold || Math.abs(y-yDown) > yClickThreshold;
    }


    @Override
    public void onMouseButtonEvent(MouseButtonEvent event) {
        if(!enabled) {
            return;
        }

        if( pressed = event.isPressed() ) {
            xLast = xDown = event.getX();
            yLast = yDown = event.getY();
        } else if( moving ) {
//            event.setConsumed();

            moving = false;
        }
    }

    @Override
    public void onMouseMotionEvent(MouseMotionEvent event) {
        if(!enabled || !pressed) {
            return;
        }

        if(!moving) {
            if(isMoving(event, xDown, yDown)) {
                moving = true;
            }
            return;
        }

        int x = event.getX();
        int y = event.getY();

        onMove(event, x - xLast, y - yLast);

        xLast = x;
        yLast = y;
    }

    protected void onMove(MouseMotionEvent event, int dx, int dy) {
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
}

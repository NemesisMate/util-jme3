package com.nx.util.jme3.lemur.listener;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.MouseListener;

/**
 * Created by NemesisMate on 17/06/17.
 */
public class MouseMoveListener implements MouseListener {

    private int xDown;
    private int yDown;
    private int xClickThreshold;
    private int yClickThreshold;

    private int xLast;
    private int yLast;

    protected boolean moving;
    protected boolean pressed;

    public MouseMoveListener() {
        this(10, 10);
    }

    public MouseMoveListener( int xClickThreshold, int yClickThreshold ) {
        this.xClickThreshold = xClickThreshold;
        this.yClickThreshold = yClickThreshold;
    }

    protected boolean isMoving(MouseMotionEvent event, int xDown, int yDown ) {
        int x = event.getX();
        int y = event.getY();

        return Math.abs(x-xDown) > xClickThreshold || Math.abs(y-yDown) > yClickThreshold;
    }

    @Override
    public void mouseButtonEvent(MouseButtonEvent event, Spatial target, Spatial capture) {

        if( pressed = event.isPressed() ) {
            xLast = xDown = event.getX();
            yLast = yDown = event.getY();
        } else if( moving ) {
            event.setConsumed();

            moving = false;
        }

//        System.out.println("CONSUMED: " + event.isConsumed());
    }

    @Override
    public void mouseMoved(MouseMotionEvent event, Spatial target, Spatial capture) {
        if(!pressed) {
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

    @Override
    public void mouseEntered(MouseMotionEvent event, Spatial target, Spatial capture) { }
    @Override
    public void mouseExited(MouseMotionEvent event, Spatial target, Spatial capture) { }

}

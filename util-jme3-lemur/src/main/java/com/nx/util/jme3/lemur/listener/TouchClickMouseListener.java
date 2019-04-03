package com.nx.util.jme3.lemur.listener;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.MouseListener;

public class TouchClickMouseListener implements MouseListener {

    public static int DEFAULT_XCLICK_THRESHOLD = 20;
    public static int DEFAULT_YCLICK_THRESHOLD = 20;

    private int xDown;
    private int yDown;
    private int xClickThreshold;
    private int yClickThreshold;

    public TouchClickMouseListener() {
        this(DEFAULT_XCLICK_THRESHOLD, DEFAULT_YCLICK_THRESHOLD);
    }

    public TouchClickMouseListener(int xClickThreshold, int yClickThreshold ) {
        this.xClickThreshold = xClickThreshold;
        this.yClickThreshold = yClickThreshold;  
    }

    protected void click(MouseButtonEvent event, Spatial target, Spatial capture ) {
    } 

    protected boolean isClick( MouseButtonEvent event, int xDown, int yDown ) {
        int x = event.getX();
        int y = event.getY();
        return Math.abs(x-xDown) < xClickThreshold && Math.abs(y-yDown) < yClickThreshold;
    }

    public void mouseButtonEvent( MouseButtonEvent event, Spatial target, Spatial capture ) {
//        event.setConsumed();

        if( event.isPressed() ) {
            xDown = event.getX();
            yDown = event.getY();
            // Is this target == capture needed?
        } else if( target == capture && isClick(event, xDown, yDown) ) {
            click(event, target, capture);
        }
    }

    public void mouseEntered(MouseMotionEvent event, Spatial target, Spatial capture ) {
    }

    public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture ) {
    }

    public void mouseMoved( MouseMotionEvent event, Spatial target, Spatial capture ) {
    }
}
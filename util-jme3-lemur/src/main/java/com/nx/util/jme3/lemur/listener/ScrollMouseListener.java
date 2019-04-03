package com.nx.util.jme3.lemur.listener;

import com.jme3.bounding.BoundingBox;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.simsilica.lemur.Panel;

public class ScrollMouseListener extends MouseMoveListener {

    public enum ScrollMode {
        X, Y, BOTH
    }

    ScrollMode scrollMode = ScrollMode.BOTH;
    boolean switchXandY;


    public ScrollMouseListener() {
    }

    public ScrollMouseListener(int xClickThreshold, int yClickThreshold) {
        this(xClickThreshold, yClickThreshold, ScrollMode.BOTH, false);
    }

    public ScrollMouseListener(int xClickThreshold, int yClickThreshold, ScrollMode scrollMode) {
        this(xClickThreshold, yClickThreshold, scrollMode, false);
    }

    public ScrollMouseListener(int xClickThreshold, int yClickThreshold, ScrollMode scrollMode, boolean switchXandY) {
        super(xClickThreshold, yClickThreshold);

        this.scrollMode = scrollMode;
        this.switchXandY = switchXandY;
    }

    public ScrollMode getScrollMode() {
        return scrollMode;
    }

    public void setScrollMode(ScrollMode scrollMode) {
        this.scrollMode = scrollMode;
    }

    public boolean isSwitchXandY() {
        return switchXandY;
    }

    public void setSwitchXandY(boolean switchXandY) {
        this.switchXandY = switchXandY;
    }

    @Override
    protected final void onMove(MouseMotionEvent event, int dx, int dy) {
        if(switchXandY) {
            int aux = dx;
            dx = dy;
            dy = aux;
        }

        switch (scrollMode) {
            case X:
                dy = 0;
                break;
            case Y:

                dx = 0;
                break;
        }

        onScroll(event, dx, dy);
    }

    protected void onScroll(MouseMotionEvent event, int dx, int dy) {

    }

    public Control createControl(Panel panel) {
        return new ScrollControl(panel);
    }

    public Control createControl(Panel panel, float fixedInterval) {
        return new ScrollControl(panel, false, fixedInterval);
    }

    public Control create3dControl(Panel panel) {
        return new ScrollControl(panel, true);
    }

    public Control create3dControl(Panel panel, float fixedInterval) {
        return new ScrollControl(panel, true, fixedInterval);
    }

    public Control createControl(Panel panel, float fixedInterval, int amount) {
        return new IntervalScrollControl(panel, fixedInterval, amount);
    }

    //TODO: Add a setSelected method
    public class ScrollControl extends AbstractControl {

        float fixedInterval;
        Panel panel;
        boolean spatialIs3d;
        final Vector3f aux = new Vector3f();

        public ScrollControl(Panel panel) {
            this(panel, false);
        }

        public ScrollControl(Panel panel, boolean spatialIs3d) {
            this(panel, spatialIs3d, 0);
        }

        public ScrollControl(Panel panel, boolean spatialIs3d, float fixedInterval) {
            this.panel = panel;
            this.spatialIs3d = spatialIs3d;
            this.fixedInterval = fixedInterval;
        }

        @Override
        protected void controlUpdate(float tpf) {
            if(ScrollMouseListener.this.pressed) {
                return;
            }
//                    c.updateModelBound();
            BoundingBox bb = (BoundingBox) spatial.getWorldBound();
            if(bb == null) {
                return;
            }

            switch (scrollMode) {
                case X:
                    move(0, bb, tpf);
                    break;
                case Y:
                    move(1, bb, tpf);
                    break;
                case BOTH:
                    move(0, bb, tpf);
                    move(1, bb, tpf);
                    break;
            }

            spatial.setLocalTranslation(spatial.getLocalTranslation());
        }

        private void move(int axisIndex, BoundingBox bb, float tpf) {
            float x = spatial.getLocalTranslation().get(axisIndex);
//
            float xSize = bb.getExtent(aux).divideLocal(spatial.getWorldScale()).get(ScrollMouseListener.this.switchXandY ? (axisIndex + 1) % 2 : axisIndex) * 2;

            //TODO: Add intervals, fixed points, when the drag is stopped it goes to the nearest fixed point

            float fixedSize = panel.getSize().get(axisIndex);

            //TODO: in this case I must have the camera position and the spatial translation in mind.
            if(spatialIs3d) {
                throw new UnsupportedOperationException("YET");
                // This patch is just that, a temporal patch
//                fixedSize /= Display.getWidth();
            }

//                    float fixedSize = ((BoundingBox)ScrollableList.this.getWorldBound()).getYExtent();

            float extraSize = 0;

            if(xSize > fixedSize) {
                extraSize = (xSize - fixedSize);
            }

            // Inverse the sign for y-axis
            x -= x * 2 * axisIndex;

            // Horizontal
            if(x > 0 || extraSize == 0 || x < -extraSize || (fixedInterval > 0 && x % fixedInterval != 0)) {
                if(fixedInterval > 0) {

                    x %= fixedInterval;

                    if(FastMath.abs(x) > fixedInterval / 2f) {
                        x = (x < 0 ? fixedInterval : -fixedInterval) + x;
                    }

                } else {
                    if (x < 0) {
                        x += extraSize;
                    }
                }
//                        list.move((x >= extraSize && x <= extraSize + 1) || (x < extraSize && x >= extraSize -1) ? -x : -x * 10 * tpf, 0, 0);


//                spatial.move((x >= 0 && x <= 1) || (x < 0 && x >= -1) ? -x : -x * 10 * tpf, 0, 0);

                Vector3f loc = spatial.getLocalTranslation();

                float increment = ((x >= 0 && x <= 1) || (x < 0 && x >= -1) ? -x : -x * 10 * tpf);

                // Reinverse the sign for y-axis
                increment -= increment * 2 * axisIndex;

                loc.set(axisIndex, loc.get(axisIndex) + increment);
//                        list.move(-x * 10 * tpf, 0, 0);
            }

        }


        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
        }
    }

    //TODO: Add a setSelected method
    public class IntervalScrollControl extends AbstractControl {

        float fixedInterval;
        Panel panel;
        float boundSize;

//        int max;

        int selectedIndex;

        public IntervalScrollControl(Panel panel, float fixedInterval, int amount) {
            this.panel = panel;
            this.fixedInterval = fixedInterval;
//            this.max = amount - 1;

            this.boundSize = (amount - 1) * fixedInterval;
        }

        @Override
        protected void controlUpdate(float tpf) {
            if(ScrollMouseListener.this.pressed) {
                return;
            }

            //TODO: With y-axis too

            float posX = spatial.getLocalTranslation().x;

            int index = 0;

            if(posX > 0 || posX < -boundSize || posX % fixedInterval != 0) {
                float x = posX;
                if(x < -boundSize) {
                    x += boundSize;
                    index = ((int) boundSize / (int) fixedInterval);
                } else {
                    if(posX < 0) {
                        x %= fixedInterval;

                        if (-x > fixedInterval / 2f) {
                            x += fixedInterval;

                            index = -(int) ((posX - x) / fixedInterval);
                        } else {
                            index = -(int) (posX / fixedInterval);
                        }
//                        if (FastMath.abs(x) > fixedInterval / 2f) {
//                            x += x < 0 ? fixedInterval : -fixedInterval;
//                        }
                    }
                }

                spatial.move((x >= 0 && x <= 1) || (x < 0 && x >= -1) ? -x : -x * 10 * tpf, 0, 0);
            } else {
                index = (int) -(posX / fixedInterval);
            }

//            int index = (int) FastMath.clamp(-(posX / fixedInterval), 0, max);

            if(index != selectedIndex) {
                selectedIndex = index;
                onSelect(index);
            }
        }

        protected void onSelect(int index) {

        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
        }
    }




}

package com.nx.util.cross;

import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.nx.util.jme3.lemur.panel.ScrollableList;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.style.ElementId;

import java.util.concurrent.Callable;

public class PatchedScrollableList extends ScrollableList {

    private final SceneGraphVisitor patchVisitor = new SceneGraphVisitor() {
        @Override
        public void visit(Spatial spatial) {
            MouseEventControl mouseEventControl = spatial.getControl(MouseEventControl.class);
            if(mouseEventControl != null) {
                mouseEventControl.addMouseListener(patchListener);
            }
        }
    };

    public PatchedScrollableList() {
    }

    public PatchedScrollableList(ElementId elementId) {
        super(elementId);
    }

    public PatchedScrollableList(String style) {
        super(style);
    }

    public PatchedScrollableList(ElementId elementId, String style) {
        super(elementId, style);
    }

    @Override
    public <T extends Node> T addContent(final T child) {
        super.addContent(child);

        CrossMain.getInstance().enqueue(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                child.depthFirstTraversal(patchVisitor);
                return null;
            }
        });

        return child;
    }
}

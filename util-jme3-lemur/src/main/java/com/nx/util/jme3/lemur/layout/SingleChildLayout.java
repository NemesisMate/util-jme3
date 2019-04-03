package com.nx.util.jme3.lemur.layout;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.component.AbstractGuiComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by NemesisMate on 27/01/17.
 */
public abstract class SingleChildLayout extends AbstractGuiComponent
        implements GuiLayout, Cloneable {

    protected GuiControl parent;

    private Node child;

    public SingleChildLayout() {
    }

    @Override
    protected void invalidate() {
        if( parent != null ) {
            parent.invalidate();
        }
    }

    @Override
    public SingleChildLayout clone() {
        return (SingleChildLayout) super.clone();
    }

    @Override
    public void calculatePreferredSize(Vector3f size) {
        if(child != null) {
            calculatePreferredSize(size, child);
        }
    }

    @Override
    public void reshape(Vector3f pos, Vector3f size) {
        if(child != null) {
            reshape(pos, size, child);
        }
    }


    protected abstract void calculatePreferredSize(Vector3f size, Node child);

    protected abstract void reshape(Vector3f pos, Vector3f size, Node child);

    @Override
    public <T extends Node> T addChild(T n, Object... constraints) {
        if( n.getControl( GuiControl.class ) == null )
            throw new IllegalArgumentException( "Child is not GUI element." );
        if( constraints != null && constraints.length > 0 )
            throw new IllegalArgumentException( "Single layout does not take constraints." );

        if(child != null) {
            throw new IllegalArgumentException( "Single layout does not take more than 1 child." );
        }

        child = n;

        if( parent != null ) {
            // We are attached
            parent.getNode().attachChild(n);
        }

        invalidate();
        return n;
    }

    public <T extends Node> T setChild(T n) {
        Node c = child;
        child = n;

        if(parent != null) {
            if(c != null) {
                parent.getNode().detachChild(c);
            }

            if(n != null) {
                parent.getNode().attachChild(n);
            }
        }

        invalidate();
        return n;
    }

    @Override
    public void removeChild(Node n) {
        if(n != child) {
            return;
        }

        child = null;

        if( parent != null ) {
            parent.getNode().detachChild(n);
        }

        invalidate();
    }

    @Override
    public void attach( GuiControl parent ) {
        this.parent = parent;
        Node self = parent.getNode();

        if(child != null) {
            self.attachChild(child);
        }
    }

    @Override
    public void detach( GuiControl parent ) {
        this.parent = null;

        if(child != null) {
            child.removeFromParent();
        }
    }

    public Collection<Node> getChildren() {
        return Collections.singletonList(child);
    }

    public Node getChild() {
        return child;
    }

    public void clearChildren() {
        if( parent != null ) {
            Node parentNode = parent.getNode();

            if(child != null) {
                parentNode.detachChild(child);
            }

        }
        child = null;

        invalidate();
    }
}
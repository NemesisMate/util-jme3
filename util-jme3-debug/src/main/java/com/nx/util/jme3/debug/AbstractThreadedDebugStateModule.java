package com.nx.util.jme3.debug;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

import java.util.concurrent.Callable;

/**
 * Created by NemesisMate on 10/02/17.
 */
public abstract class AbstractThreadedDebugStateModule<T> extends AbstractDebugStateModule<T> {


    @Override
    protected void addToDebug(final T original, final Spatial debug) {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AbstractThreadedDebugStateModule.super.addToDebug(original, debug);
                return null;
            }
        });
    }

    @Override
    protected void removeFromDebug(final T original) {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AbstractThreadedDebugStateModule.super.removeFromDebug(original);
                return null;
            }
        });
    }

    @Override
    protected void addControl(final Spatial spatial, final Control control) {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AbstractThreadedDebugStateModule.super.addControl(spatial, control);
                return null;
            }
        });
    }

//    @Override
//    protected void addToDebug(T original, Spatial debug) {
//        super.addToDebug(original, debug);
//    }
//
//    @Override
//    protected void addControl(Spatial spatial, Control control) {
//        super.addControl(spatial, control);
//    }

//    @Override
//    protected void removeFromDebug(T original) {
//        super.removeFromDebug(original);
//    }

    @Override
    protected void removeFromDebug(final T original, final Spatial debug) {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AbstractThreadedDebugStateModule.super.removeFromDebug(original, debug);
                return null;
            }
        });
    }


}

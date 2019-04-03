package com.nx.util.jme3.lemur.tween;

import com.simsilica.lemur.anim.AbstractTween;

import java.util.concurrent.Callable;

/**
 * Created by NemesisMate on 22/02/17.
 */
public class CallableTween extends AbstractTween {

    private Callable callback;


    public CallableTween(Callable callback) {
        super(0);

        this.callback = callback;
    }

    @Override
    protected void doInterpolate(double t) {
        try {
            callback.call();
        } catch (Exception e) {
            throw new RuntimeException("Error running callback:" + callback, e);
        }
    }
}

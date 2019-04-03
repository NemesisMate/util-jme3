package com.nx.util.jme3.lemur.tween;

import com.jme3.scene.Spatial;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.effect.EffectControl;

/**
 * Created by NemesisMate on 23/02/17.
 */
public class RunEffectTween implements Tween {

    final Spatial target;
    final String effectName;

    Animation animation;
    AnimationState animationState;


    public RunEffectTween(Spatial target, String effectName) {
        this.target = target;
        this.effectName = effectName;
    }

    @Override
    public double getLength() {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean interpolate(double t) {
        if(animation == null) {
            EffectControl effects = target.getControl(EffectControl.class);
            if( effects == null ) {
                return false;
            }

            animation = effects.runEffect(effectName).getAnimation();
            animationState = AnimationState.getDefaultInstance();
        }

        return animationState.isRunning(animation);
    }
}

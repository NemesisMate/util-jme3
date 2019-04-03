package com.nx.util.jme3.lemur.tween;

import com.jme3.audio.AudioNode;
import com.jme3.math.FastMath;
import com.simsilica.lemur.anim.AbstractTween;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.Tweens;

/**
 * Created by NemesisMate on 8/05/17.
 */
public final class AudioTweens {

    private AudioTweens() {

    }

    public static Tween play(AudioNode target) {
        return Tweens.callMethod(target, "play");
    }

    /**
     *
     * @deprecated because of it doesn't make much sense. The tweens are usually created each time the effect is called
     * so a new instance is already being created with the normal play action.
     */
    @Deprecated
    public static Tween playInstance(AudioNode target) {
        return Tweens.callMethod(target, "playInstance");
    }

    public static Tween stop(AudioNode target) {
        return Tweens.callMethod(target, "stop");
    }

    public static Tween fade(AudioNode target, Float fromAlpha, Float toAlpha, double length ) {
        if( fromAlpha == null ) {
            fromAlpha = target.getVolume();
        }
        if( toAlpha == null ) {
            toAlpha = target.getVolume();
        }
        return new Fade(target, fromAlpha, toAlpha, length);
    }

    private static class Fade extends AbstractTween {

        private final AudioNode target;
        private final float from;
        private final float to;

        public Fade( AudioNode target, float from, float to, double length ) {
            super(length);
            this.target = target;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void doInterpolate( double t ) {
            float value = FastMath.interpolateLinear((float)t, from, to);
            target.setVolume(value);
        }
    }
}

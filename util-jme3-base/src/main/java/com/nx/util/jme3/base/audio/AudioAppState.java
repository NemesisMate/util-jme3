package com.nx.util.jme3.base.audio;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.SafeArrayList;
import com.nx.util.jme3.base.ScenegraphListenerControl;

/**
 * Created by NemesisMate on 8/05/17.
 */
public class AudioAppState extends BaseAppState {

    private class AudioAutoManager extends ScenegraphListenerControl {
        boolean playing;

        @Override
        public void onAttached() {
            start();
        }

        @Override
        public void onDetached() {
            stop();
        }

        protected void start() {
            startAudio((AudioNode) spatial);

            playing = true;
        }

        protected void stop() {
            stopAudio((AudioNode) spatial);
            playing = false;
        }
    }

    private class PositionalAudioAutoManager extends AudioAutoManager {
        @Override
        public void onAttached() {
            // Do nothing. It is already being managed in the update.
        }

        @Override
        protected void controlUpdate(float tpf) {
            super.controlUpdate(tpf);

            float maxDistance = ((AudioNode)spatial).getMaxDistance();

            if(spatial.getWorldTranslation().distanceSquared(positionRef) > maxDistance * maxDistance) {
                if(playing) {
                    stop();
                }
            } else {
                if(!playing) {
                    start();
                }
            }
        }
    }

    private Vector3f positionRef = Vector3f.ZERO;

    public AudioAppState() { }

    public AudioAppState(Vector3f positionRef) {
        this.positionRef = positionRef;
    }

    @Override
    protected void initialize(Application app) {
//        audioList = new ArrayList<>();
    }

    public void addAllAudioNodeSmart(Spatial spatial) {
        if(spatial instanceof Node) {
            if(spatial instanceof AudioNode) {
                addAudioNodeSmart((AudioNode) spatial);
            }

            for(Spatial child : ((SafeArrayList<Spatial>) ((Node)spatial).getChildren()).getArray()) {
                addAllAudioNodeSmart(child);
            }
        }
    }

    public void addAudioNodeSmart(AudioNode audioNode) {
        addAudioNode(audioNode, audioNode.getMaxDistance() != 0, false);
    }

    public void addAudioNode(AudioNode audioNode, boolean managePosition, boolean instanced) {
//        if(managePosition) {
//            if(!audioNode.isPositional()) {
//                throw new IllegalArgumentException("Only can manage position for positional-looping sounds.");
//            }
//        }

        audioNode.addControl(managePosition ? new PositionalAudioAutoManager() : new AudioAutoManager());
    }

//    public void removeAudioNode(AudioNode audioNode) {
//        if(audioNode.isPositional() && audioNode.isLooping()) {
//            audioList.remove(audioNode);
//        }
//
//    }

    public void stopAudio(AudioNode audioNode) {
        audioNode.stop();
    }

    public void startAudio(AudioNode audioNode) {
        audioNode.play();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    public void setPositionRef(Vector3f positionRef) {
        this.positionRef = positionRef;
    }
}

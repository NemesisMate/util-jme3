package com.nx.util.jme3.debug;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;

/**
 * Created by NemesisMate on 1/12/16.
 */
public class DebugBulletState extends AbstractAppState {

    BulletAppState bulletState;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        bulletState = stateManager.getState(BulletAppState.class);
        bulletState.setDebugEnabled(true);
    }

    @Override
    public void cleanup() {
        super.cleanup();

        bulletState.setDebugEnabled(false);
    }
}

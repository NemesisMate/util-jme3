package com.nx.util.cross;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;

import java.awt.*;

public class CrossMain extends SimpleApplication {

    private static CrossMain instance;

    public CrossMain(AppState... initialStates) {
        super(initialStates);

        if(CrossMain.instance != null) {
            throw new IllegalStateException("This is a singleton class");
        }
        CrossMain.instance = this;
    }

    public static CrossMain getInstance() {
        return instance;
    }

    @Override
    public void simpleInitApp() {

        UiState uiState = getStateManager().getState(UiState.class);

        if(uiState != null && uiState.getGraphics() == null) {
            // Values from: https://github.com/libgdx/libgdx/blob/master/backends/gdx-backend-lwjgl/src/com/badlogic/gdx/backends/lwjgl/LwjglGraphics.java
            uiState.setGraphics(new Graphics() {
                @Override
                public float getPpiX() {
                    return Toolkit.getDefaultToolkit().getScreenResolution();
                }

                @Override
                public float getPpiY() {
                    return Toolkit.getDefaultToolkit().getScreenResolution();
                }

                @Override
                public float getDensity() {
//                    if (config.overrideDensity != -1) return config.overrideDensity / 160f;

                    //TODO: adjust this
                    return (Toolkit.getDefaultToolkit().getScreenResolution() / 80f);
//                    return (Toolkit.getDefaultToolkit().getScreenResolution() / 160f);
                }

                @Override
                public float getScaledDensity() {
                    return getDensity();
                }
            });
        }
    }



}

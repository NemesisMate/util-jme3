package com.nx.util.jme3.debug;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.style.StyleLoader;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by NemesisMate on 11/01/17.
 */
public class DebugLemurReloadState extends AbstractDebugGraphStateModule {

//    Reader stylesReader;

    private StyleLoader styleLoader;

    private final String stylesPath;
    private final File file;

    private float badTime;

    public DebugLemurReloadState(String stylesPath) {
        this.stylesPath = stylesPath;
        file = new File(stylesPath);
    }

//    public DebugLemurReloadState(Reader stylesReader) {
//        this.stylesReader = stylesReader;
//    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.styleLoader = new StyleLoader();
    }

    @Override
    protected void updateSceneGraph(float tpf) {
        try {
//            GuiGlobals.getInstance().getStyles().clearCache();
            styleLoader.loadStyle(stylesPath, new FileReader(file));
//            new FileReader(file);



            badTime = 0;



//        styleLoader.loadStyle(stylesReader.toString(), stylesReader);

            super.updateSceneGraph(tpf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            LoggerFactory.getLogger(this.getClass()).error("File not found: {}.", file);
            return;
        } catch (Exception e) {
            if(badTime > 5 || badTime == 0) {
                badTime = 0;
                LoggerFactory.getLogger(this.getClass()).warn("Bad style file: {}.", file);
            }

            badTime += tpf;

            return;
        }


    }

    @Override
    protected boolean update(Spatial spatial, float tpf) {
        if(spatial instanceof Panel) {

            GuiGlobals.getInstance().getStyles().applyStyles(spatial, ((Panel) spatial).getElementId(), ((Panel) spatial).getStyle());
        }

        return false;
    }
}

package com.nx.util.jme3.debug;

import com.jme3.app.LegacyApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.MaterialKey;
import com.jme3.asset.plugins.CustomMaterialDebugAppState;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.nx.util.jme3.lemur.ConsoleCommand;
import com.simsilica.lemur.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * Created by NemesisMate.
 */
public class AssetCommand implements Command<ConsoleCommand> {

    private static final Logger log = LoggerFactory.getLogger(AssetCommand.class);

    CustomMaterialDebugAppState materialDebugAppState;

    LegacyApplication app;
    AssetManager assetManager;
    Spatial selected;

    public AssetCommand(LegacyApplication app, Spatial selected, AssetManager assetManager) {
        this.app = app;
        this.assetManager = assetManager;
        this.selected = selected;
    }

    @Override
    public void execute(ConsoleCommand source) {
        String[] args = source.getArgs();

        log.debug("Executing assets command.");
        if(args != null && args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "r":
                case "reload":
                    ReloadType type = ReloadType.ALL;
                    boolean auto = false;

                    if(args.length > 1) {
                        for(String arg : args) {
                            switch (arg.toLowerCase()) {
                                case "s":
                                case "stencil":
                                case "stencils":
                                    type = ReloadType.STENCIL;
                                    break;
                                case "all":
                                    type = ReloadType.ALL;
                                    break;
                                case "a":
                                case "auto":
                                case "automatic":
                                    auto = true;
                                    break;
                            }
                        }
                    }

                    reload(type, auto);

                    break;
            }
        }

        return;
    }

    private void reload(ReloadType type, boolean auto) {
        if(materialDebugAppState != null) {
            app.getStateManager().detach(materialDebugAppState);
            materialDebugAppState = null;
            return;
        }

        materialDebugAppState = new CustomMaterialDebugAppState();

        ReloadCondition reloadCondition = null;


        switch (type) {
            case STENCIL:
                final MaterialDef matDef12 = (MaterialDef)assetManager.loadAsset(new AssetKey("Common/MatDefs/Terrain/TerrainLighting.j3md"));
                final MaterialDef matDef3 = (MaterialDef)assetManager.loadAsset(new AssetKey("Common/MatDefs/Terrain/Terrain.j3md"));

                reloadCondition = new ReloadCondition() {
                    @Override
                    public boolean meetCondition(Spatial spatial) {
                        if(spatial instanceof Geometry) {
                            Material mat = ((Geometry) spatial).getMaterial();
                            if(mat.getMaterialDef() == matDef3 || mat.getMaterialDef() == matDef12) {
                                return true;
                            }
                        }

                        return false;
                    }
                };
                break;
            case ALL:
                reloadCondition = null;
                break;

        }

        final ReloadCondition[] reloadConditions = reloadCondition != null ? new ReloadCondition[] { reloadCondition } : null;

        MDC.clear();

        selected.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(final Spatial spatial) {
                if(reloadConditions != null) {
                    for(ReloadCondition reloadCondition : reloadConditions) {
                        if(!reloadCondition.meetCondition(spatial)) {
                            return;
                        }
                    }
                }

                if(!(spatial instanceof Geometry)) {
                    return;
                }

                Material mat = ((Geometry) spatial).getMaterial();

                MaterialKey matKey = (MaterialKey) mat.getKey();

                if(matKey == null) {
                    log.warn("Couldn't reload material because of it key is null.");
                    return;
                }

                // Bind material
                materialDebugAppState.registerBinding(matKey.getName(), spatial);

                // Bind shaders (frag/vert)
                mat.getMaterialDef().getTechniqueDefsNames().forEach(name ->
                        mat.getMaterialDef().getTechniqueDefs(name).forEach( technique -> {
                            materialDebugAppState.registerBinding(technique.getFragmentShaderName(), spatial);
                            materialDebugAppState.registerBinding(technique.getVertexShaderName(), spatial);
                        })
                );

                // Bind images
                for(MatParam matParam : mat.getParams()) {
                    if(matParam instanceof MatParamTexture) {
                        AssetKey texKey = ((MatParamTexture) matParam).getTextureValue().getKey();
                        materialDebugAppState.registerBinding(texKey.getName(), spatial);
                    }
                }
            }
        });



        if(auto) {
            app.getStateManager().attach(materialDebugAppState);
        } else {
            materialDebugAppState.initialize(app.getStateManager(), app);
            materialDebugAppState.triggerReloadingOfAllBindings();
            materialDebugAppState = null;
        }
    }


    private enum ReloadType {
        STENCIL, ALL
    }

    private interface ReloadCondition {
        boolean meetCondition(Spatial spatial);
    }

}

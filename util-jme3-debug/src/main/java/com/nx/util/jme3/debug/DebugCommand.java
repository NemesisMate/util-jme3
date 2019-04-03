package com.nx.util.jme3.debug;

import com.jme3.app.LegacyApplication;
import com.jme3.scene.Spatial;
import com.nx.util.jme3.base.DebugUtil;
import com.nx.util.jme3.lemur.ConsoleCommand;
import com.simsilica.lemur.Command;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

/**
 * Created by inaki on 9/12/16.
 */
public class DebugCommand implements Command<ConsoleCommand> {

    LegacyApplication app;
    Spatial selected;

    public DebugCommand(LegacyApplication app, Spatial selected) {
        this.app = app;
        this.selected = selected;
    }

    @Override
    public void execute(ConsoleCommand source) {


        String[] args = source.getArgs();
        boolean result = false;
        String type = "";

        DebugStateProperties.checkDebugState(app, selected, true);

        if(args != null && args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "graph":
                    result = showGraph();
                    type = "graph";
                    break;
                case "p":
                case "physic":
                case "physics":
                    type = "physics";
                    result = debugPhysics();
                    break;
                case "b":
                case "bound":
                case "bounds":
                    boolean nodeBounds = false;
                    boolean alwaysMarkNodePositions = false;

                    if(args.length > 1) {
                        for (int i = 0; i < args.length; i++) {
                            switch (args[i].toLowerCase()) {
                                case "n":
                                case "node":
                                case "nodes":
                                    nodeBounds = true;
                                    break;
                                case "p":
                                case "positions":
                                case "a":
                                case "always":
                                    alwaysMarkNodePositions = true;
                                    break;
                            }
                        }
                    }

                    type = "bounds";
                    result = debugBounds(nodeBounds, alwaysMarkNodePositions);
                    break;
                case "m":
                case "mesh":
                case "meshes":
                    boolean colors = false;
                    boolean wire = false;
                    boolean cullOff = false;
                    boolean normals = false;
//                    boolean meshInstances = false;
                    boolean sharedMeshes = false;
                    boolean grouped = false;


                    if(args.length > 1) {
                        for(int i = 0; i < args.length; i++) {
                            switch (args[i].toLowerCase()) {
                                case "colors":
                                case "c":
                                    colors = true;
                                    break;
                                case "wire":
                                case "w":
                                    wire = true;
                                    break;
                                case "culloff":
                                case "faceculloff":
                                case "off":
                                case "o":
                                    cullOff = true;
                                    break;
                                case "n":
                                case "normals":
                                    normals = true;
                                    break;
                                case "sm":
                                case "sharedMeshes":
                                    sharedMeshes = true;
                                    break;
                                case "gr":
                                case "grouped":
                                case "instanced":
                                case "batched":
                                    grouped = true;
                                    break;
                            }
                        }
                    }

                    type = "mesh";
                    result = debugMesh(colors, wire, normals, cullOff, sharedMeshes, grouped);
                    break;
                case "s":
                case "skeleton":
                case "skeletons":
                    result = debugSkeleton();
                    type = "skeleton";
                    break;
                case "n":
                case "normal":
                case "normals":
                    boolean arrows = false;
                    boolean tangents = false;


                    if(args.length > 1) {
                        for(int i = 0; i < args.length; i++) {
                            switch (args[i].toLowerCase()) {
                                case "a":
                                case "arrows":
                                    arrows = true;
                                    break;
                                case "t":
                                case "tangents":
                                    tangents = true;
                            }
                        }
                    }



                    result = debugNormals(arrows, tangents);
                    type = "normals";
                    break;
                case "lemur":
                case "panel":
                case "panels":
                    result = debugLemur();
                    type = "Lemur Panels";
                    break;

            }
        }

        DebugStateProperties.checkDebugState(app, selected, false);

        LoggerFactory.getLogger(this.getClass()).info(MarkerFactory.getMarker("CONSOLE"), "Debug {} enabled: {}", type, (result ? "true" : "false"));
    }

    private boolean debugLemur() {
        return false;
    }

    private boolean debugNormals(boolean arrows, boolean tangents) {

//        checkDebugState();
        DebugNormalsState debugs = app.getStateManager().getState(DebugNormalsState.class);

        if(debugs == null) {
            app.getStateManager().attach(new DebugNormalsState(arrows, tangents));
            return true;
        } else {
            app.getStateManager().detach(debugs);
            return false;
        }
    }


    private boolean debugMesh(boolean colors, boolean wire, boolean normals, boolean cullOff, boolean sharedMeshes, boolean grouped) {
//        checkDebugState();
        DebugMeshState debugs = app.getStateManager().getState(DebugMeshState.class);

        if(debugs == null) {
            app.getStateManager().attach(new DebugMeshState(colors, wire, normals, cullOff, sharedMeshes, grouped));
            return true;
        } else {
            app.getStateManager().detach(debugs);
            return false;
        }
    }


    private boolean debugSkeleton() {

//        checkDebugState();
        DebugSkeletonState debugs = app.getStateManager().getState(DebugSkeletonState.class);

        if(debugs == null) {
            app.getStateManager().attach(new DebugSkeletonState());
            return true;
        } else {
            app.getStateManager().detach(debugs);
            return false;
        }
    }

    private boolean debugBounds(boolean nodeBounds, boolean alwaysMarkNodePositions) {
//        checkDebugState();
        DebugBoundsState debugs = app.getStateManager().getState(DebugBoundsState.class);

        if(debugs == null) {
            app.getStateManager().attach(new DebugBoundsState(nodeBounds, alwaysMarkNodePositions));
            return true;
        } else {
            app.getStateManager().detach(debugs);
            return false;
        }
    }

    private boolean debugPhysics() {
//        checkDebugState();
        DebugBulletState debugs = app.getStateManager().getState(DebugBulletState.class);

        if(debugs == null) {
            app.getStateManager().attach(new DebugBulletState());
            return true;
        } else {
            app.getStateManager().detach(debugs);
            return false;
        }
    }




    private boolean showGraph() {
        DebugUtil.debugScenegraph();
        return true;
    }
}

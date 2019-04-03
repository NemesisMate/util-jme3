package com.nx.util.jme3.lemur;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.Styles;

//TODO: Use from UtilJME3-Lemur
public class LemurStyleBasic {

    public static void InitLemurStyle(AssetManager assetManager) {

        Styles styles = GuiGlobals.getInstance().getStyles();

        Attributes attrs;

        attrs = styles.getSelector("glass");
        attrs.set("fontSize", 20);

        QuadBackgroundComponent bg = new QuadBackgroundComponent(new ColorRGBA(0.207f, 0.518f, 0.89f, 0.5f));

        attrs = styles.getSelector("button", "glass");
        attrs.set("color", new ColorRGBA(0.5f, 0.75f, 0.75f, 0.85f));
        attrs.set("highlightColor", new ColorRGBA(1, 1, 1, 1));
        attrs.set("background", bg);

        attrs = styles.getSelector("container", "glass");
        attrs.set("color", new ColorRGBA(0.4f, 0.75f, 0.75f, 0.85f));
        attrs.set("background", bg);

        attrs = styles.getSelector("slider", "button", "glass");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.Blue));

        attrs = styles.getSelector("slider.thumb.button", "glass");
        attrs.set("text", "[]");
        attrs.set("color", new ColorRGBA(0.4f, 0.75f, 1f, 0.85f));
        attrs.set("preferredSize", new Vector3f(50, 50, 1));

        /*BitmapFont font = assetManager.loadFont("myFont.fnt");
        attrs = styles.getSelector("slider", "button", "glass");
        attrs.set("fontSize", 10);
        attrs.set("font", font);*/

        attrs = styles.getSelector("transparent");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.BlackNoAlpha));

        attrs = styles.getSelector("red");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.Red));

        attrs = styles.getSelector("blue");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.Blue));

        attrs = styles.getSelector("green");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.Green));

        attrs = styles.getSelector("orange");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.Orange));

        attrs = styles.getSelector("gray");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.Gray));

        attrs = styles.getSelector("white");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.White));

        attrs = styles.getSelector("black");
        attrs.set("background", new QuadBackgroundComponent(ColorRGBA.Black));
    }
}
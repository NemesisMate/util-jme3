package com.nx.util.jme3.base;

import com.jme3.app.LegacyApplication;
import com.jme3.renderer.Caps;
import com.jme3.system.JmeContext;
import org.slf4j.LoggerFactory;

/**
 * Created by NemesisMate on 18/06/17.
 */
public class PlatformsUtil {

    public static LegacyApplication application;
    public static JmeContext context;

    public static boolean supports(Caps cap) {
        if(context == null) {
            LoggerFactory.getLogger(PlatformsUtil.class).warn("Can't tell if the system supports caps or not as no context was given.");
            throw new NullPointerException("Can't tell if the system supports caps or not as no context was given.");
        }
//        return false;
        return context.getRenderer().getCaps().contains(cap);
    }

    public static boolean isAndroid() {
        return System.getProperty("java.vendor.url").contains("android");
    }
}

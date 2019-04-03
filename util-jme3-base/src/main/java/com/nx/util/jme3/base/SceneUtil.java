package com.nx.util.jme3.base;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;

import java.nio.ByteBuffer;

/**
 *
 * @author NemesisMate
 */
public final class SceneUtil {

    private SceneUtil() {

    }

    /**
     *
     * @param inputManager
     * @param cam
     * @param coordsStore
     * @param dirStore
     * @return dirStore
     */
    @Deprecated
    public static Vector3f camCoordsToWorld(InputManager inputManager, Camera cam, Vector3f coordsStore, Vector3f dirStore) {
        return camCoordsToWorld(inputManager.getCursorPosition(), cam, coordsStore, dirStore);
    }


    public static Vector3f camCoordsToWorld(Vector2f cursor2d, Camera cam, Vector3f coordsStore, Vector3f dirStore) {
        TempVars vars = TempVars.get();

//        Vector2f cursor2d = cursorPos;
        cam.getWorldCoordinates(vars.vect2d.set(cursor2d.x, cursor2d.y), 0f, coordsStore);
        cam.getWorldCoordinates(vars.vect2d, 1f, dirStore).subtractLocal(coordsStore).normalizeLocal();

        vars.release();

        return dirStore;
    }

    @Deprecated
    public static Vector3f getMousePoint(InputManager inputManager, Camera cam,  Spatial spat) {
        return getMousePoint(inputManager.getCursorPosition(), cam, spat);
    }

    public static Vector3f getMousePoint(Vector2f cursor2d, Camera cam,  Spatial spat) {
        SceneVars vars2 = SceneVars.get();

        CollisionResults localResults = getClickResults(cursor2d, cam, spat, vars2.collisionResults, vars2.ray);

        Vector3f contactPoint = null;
        if(localResults.size() > 0) {
            contactPoint = localResults.getClosestCollision().getContactPoint();
        }

        vars2.release();

        return contactPoint;
    }

    @Deprecated
    public static CollisionResult getMouseResult(InputManager inputManager, Camera cam, Spatial spat) {
        return getMouseResult(inputManager.getCursorPosition(), cam, spat);
    }

    public static CollisionResult getMouseResult(Vector2f cursor2d, Camera cam, Spatial spat) {
        SceneVars vars2 = SceneVars.get();

        CollisionResults localResults = getClickResults(cursor2d, cam, spat, vars2.collisionResults, vars2.ray);

        CollisionResult result = null;
        if(localResults.size() > 0) {
            result = localResults.getClosestCollision();
        }

        vars2.release();

        return result;
    }

    public static Vector3f getMouseDirection(InputManager inputManager, Camera cam, Vector3f dirStore) {
        return getMouseDirection(inputManager.getCursorPosition(), cam, dirStore);
    }

    public static Vector3f getMouseDirection(Vector2f pos2d, Camera cam, Vector3f dirStore) {
        if(dirStore == null) {
            dirStore = new Vector3f();
        }

        TempVars vars = TempVars.get();

        Vector3f click3d = cam.getWorldCoordinates(vars.vect2d.set(pos2d.x, pos2d.y), 0f, vars.vect1);
        // Aim the ray from the clicked spot forwards.
        cam.getWorldCoordinates(vars.vect2d, 1f, dirStore).subtractLocal(click3d).normalizeLocal();

        vars.release();

        return dirStore;
    }
//    private static CollisionResults getClickResults(InputManager inputManager, Camera cam, Spatial spat, CollisionResults localResults, Ray localRay) {
//        return getClickResults(inputManager.getCursorPosition(), cam, spat, localResults, localRay);
//    }

    private static CollisionResults getClickResults(Vector2f click2d, Camera cam, Spatial spat, CollisionResults localResults, Ray localRay) {
        TempVars vars = TempVars.get();
//        SceneVars vars2 = SceneVars.get();

//        CollisionResults localResults = new CollisionResults();

        // Convert screen click to 3d position
//        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(vars.vect2d.set(click2d.x, click2d.y), 0f, vars.vect1);
        // Aim the ray from the clicked spot forwards.
        Vector3f dir = cam.getWorldCoordinates(vars.vect2d, 1f, vars.vect2).subtractLocal(click3d).normalizeLocal();

//        Ray localRay = vars2.ray;
        localRay.setOrigin(click3d);
        localRay.setDirection(dir);
        localRay.setLimit(Float.POSITIVE_INFINITY);
        // Collect intersections between ray and all nodes in results list.
        spat.collideWith(localRay, localResults);

        vars.release();

        return localResults;
    }



    public static CollisionResults getClickResults(Vector2f cursor2d, Camera cam, Spatial spat) {
        SceneVars vars2 = SceneVars.get();

        CollisionResults localResults = getClickResults(cursor2d, cam, spat, new CollisionResults(), vars2.ray);

        vars2.release();

        return localResults;
    }

    @Deprecated
    public static CollisionResults getClickResults(InputManager inputManager, Camera cam, Spatial spat) {
        return getClickResults(inputManager.getCursorPosition(), cam, spat);
    }



    /**
     * TODO: add support for:
     *      * screenShot direction
     *      * distance from scene
     *      * non-parallel projection
     *
     * @param renderManager
     * @param offScene
     * @return
     */
    public static Texture2D takeScreenShot(final RenderManager renderManager, Spatial offScene) {
//        offScene = offScene.clone();

        boolean parallel = true;

        Camera offCamera = new Camera(512, 512);

        final ViewPort offView = renderManager.createPreView("Offscreen View", offCamera);
//        offView.setBackgroundColor(new ColorRGBA(.2f, .3f, .1f, .6f));
        offView.setBackgroundColor(ColorRGBA.BlackNoAlpha);
        offView.setClearFlags(true, true, true);

        // create offscreen framebuffer
        final FrameBuffer offBuffer = new FrameBuffer(512, 512, 1);

        //setup framebuffer's cam
        offCamera.setFrustumPerspective(45f, 1f, 1f, 1000f);

        //setup framebuffer's texture
        final Texture2D offTex = new Texture2D(512, 512, Image.Format.RGBA8);
//        offTex.setKey(new TextureKey("", true));
//        offTex.setMinFilter(Texture.MinFilter.Trilinear);
//        offTex.setMagFilter(Texture.MagFilter.Bilinear);

        //setup framebuffer to use texture
        offBuffer.setDepthBuffer(Image.Format.Depth);
//        offBuffer.setColorTexture(offTex);
        offBuffer.setColorBuffer(Image.Format.RGBA8);

        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);

        // setup framebuffer's scene
//        Box boxMesh = new Box(1, 1, 1);
//        Material material = assetManager.loadMaterial("Interface/Logo/Logo.j3m");
//        offBox = new Geometry("box", boxMesh);
//        offBox.setMaterial(material);

        // attach the scene to the viewport to be rendered
        offView.attachScene(offScene);


        offScene.updateLogicalState(0f);

        offScene.updateGeometricState();
        offScene.updateModelBound();

//        BoundingBox bb = (BoundingBox) offScene.getWorldBound();
        BoundingBox bb = (BoundingBox) offScene.getWorldBound();
        float xExtent = bb.getXExtent();
        float zExtent = bb.getZExtent();


        float yOffset = 100;

        if(parallel) {
            offCamera.setFrustumPerspective(90, 1f, 1f, 1000f);
            offCamera.setFrustum(1, 1000f, -xExtent, xExtent, zExtent, -zExtent);
            offCamera.setParallelProjection(true);
        } else {
            float wide = xExtent;
            if(zExtent > wide) {
                wide = zExtent;
            }

            // Don't need to do wide/2, xtents are already halfs.
            float distance = wide / FastMath.tan(FastMath.DEG_TO_RAD * 45f);
            yOffset = distance;
//            LoggerFactory.getLogger(SpatialUtil.class).debug("Setting off-camera distance: " + distance + ", for a wide: " + wide + ", Extents: (" + bb.getXExtent() + ", " + bb.getYExtent() + ", " + bb.getZExtent() + ")");
        }

        offCamera.setLocation(bb.getCenter().add(0, yOffset, 0));
        offCamera.lookAt(bb.getCenter(), offCamera.getUp());


        renderManager.renderViewPort(offView, 0);

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(offBuffer.getWidth() * offBuffer.getHeight() * 4);
        renderManager.getRenderer().readFrameBuffer(offBuffer, byteBuffer);

        final Image image = offTex.getImage();
        image.setData(0, byteBuffer);

        renderManager.removePreView(offView);


        ImageUtil.verticalFlip(offTex.getImage());

        return offTex;

    }


    public static float distanceSquared2D(Vector3f position1, Vector3f position2) {
        float dx = position1.x - position2.x;
        float dz = position1.z - position2.z;

        return dx * dx + dz * dz;
    }

    public static float distance2D(Vector3f position1, Vector3f position2) {
        float dx = position1.x - position2.x;
        float dz = position1.z - position2.z;

        return FastMath.sqrt(dx * dx + dz * dz);
    }
    
}

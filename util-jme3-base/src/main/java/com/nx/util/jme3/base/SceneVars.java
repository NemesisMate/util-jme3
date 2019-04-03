package com.nx.util.jme3.base;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;

/**
 *
 * @author Nemesismate - based on com.jme3.util.TempVars
 * @see com.jme3.util.TempVars
 */
public class SceneVars {
    
    /**
     * Allow X instances of TempVars in a single thread.
     */
    private static final int STACK_SIZE = 1;

    /**
     * <code>TempVarsStack</code> contains a stack of TempVars.
     * Every time TempVars.get() is called, a new entry is added to the stack,
     * and the index incremented.
     * When TempVars.release() is called, the entry is checked against
     * the current instance and  then the index is decremented.
     */
    private static class SceneVarsStack {

        int index = 0;
        SceneVars[] tempVars = new SceneVars[STACK_SIZE];
    }
    /**
     * ThreadLocal to store a TempVarsStack for each thread.
     * This ensures each thread has a single TempVarsStack that is
     * used only in method calls in that thread.
     */
    private static final ThreadLocal<SceneVarsStack> varsLocal = new ThreadLocal<SceneVarsStack>() {

        @Override
        public SceneVarsStack initialValue() {
            return new SceneVarsStack();
        }
    };
    /**
     * This instance of TempVars has been retrieved but not released yet.
     */
    private boolean isUsed = false;

    private SceneVars() { }

    /**
     * Acquire an instance of the TempVar class.
     * You have to release the instance after use by calling the 
     * release() method. 
     * If more than STACK_SIZE (currently 5) instances are requested 
     * in a single thread then an ArrayIndexOutOfBoundsException will be thrown.
     * 
     * @return A TempVar instance
     */
    public static SceneVars get() {
        SceneVarsStack stack = varsLocal.get();

        SceneVars instance = stack.tempVars[stack.index];

        if (instance == null) {
            // Create new
            instance = new SceneVars();

            // Put it in there
            stack.tempVars[stack.index] = instance;
        }

        stack.index++;

        instance.isUsed = true;

        return instance;
    }

    /**
     * Releases this instance of TempVars.
     * Once released, the contents of the TempVars are undefined.
     * The TempVars must be released in the opposite order that they are retrieved,
     * e.g. Acquiring vars1, then acquiring vars2, vars2 MUST be released 
     * first otherwise an exception will be thrown.
     */
    public void release() {
        if (!isUsed) {
            throw new IllegalStateException("This instance of TempVars was already released!");
        }

        isUsed = false;

        SceneVarsStack stack = varsLocal.get();

        // Return it to the stack
        stack.index--;

        // Check if it is actually there
        if (stack.tempVars[stack.index] != this) {
            throw new IllegalStateException("An instance of TempVars has not been released in a called method!");
        }
        
        collisionResults.clear();
        ray.setLimit(Float.POSITIVE_INFINITY);
    }

    
    public final Ray ray = new Ray();

    public final Transform trans1 = new Transform();

    public final Vector3f vect1 = new Vector3f();
    public final Vector3f vect2 = new Vector3f();
    public final Vector3f vect3 = new Vector3f();
    public final Quaternion quat1 = new Quaternion();
    public final Quaternion quat2 = new Quaternion();
    public final Quaternion quat3 = new Quaternion();
//    public final Location loc1 = new LocationImpl();
//    public final Vector3f vect3 = new Vector3f();
    public final CollisionResults collisionResults = new CollisionResults();
}

package com.group_finity.mascot;

import com.group_finity.mascot.environment.MascotEnvironment;

import java.awt.Point;
import java.util.Collection;

/**
 * API for mascot in scripts
 * <p>
 * This is public API. There might be other callable methods, but they are not guaranteed
 * to remain the same between versions and their use is discouraged.
 */
public interface ScriptableMascot {

    /**
     * Destroys the mascot.
     *
     * @see com.group_finity.mascot.action.SelfDestruct
     */
    void dispose();

    /**
     * Number of frames since the mascot's creation.
     */
    int getTime();

    /**
     * The mascot's manager. Avoid using in scripts
     * <p>
     * There are a very limited number of reasons to directly access the manager and its API is relatively unstable.
     * To get the amount of mascots, use {@link #getCount()} and {@link #getTotalCount()} instead.
     */
    Manager getManager();

    /**
     * The number of mascots of the same image set being controlled by
     * the same manager
     */
    @SuppressWarnings("unused")
    default int getCount() {
        return getManager().getCount(getImageSet());
    }

    /**
     * The total number of mascots being controlled by the same manager
     */
    @SuppressWarnings("unused")
    default int getTotalCount() {
        return getManager().getCount();
    }

    /**
     * The currently broadcast affordances of this mascot.
     * <p>
     * This collection is cleared each tick.
     *
     * @see com.group_finity.mascot.action.Broadcast
     * @see com.group_finity.mascot.action.BroadcastMove
     * @see com.group_finity.mascot.action.BroadcastStay
     */
    Collection<String> getAffordances();

    /**
     * Represents the screen environment of the mascot. Includes thing like screen size and interactive windows.
     */
    MascotEnvironment getEnvironment();

    /**
     * The path of the image set relative to the img folder.
     * <p>
     * This value can change based on platform and should mostly be used for debugging.
     */
    String getImageSet();

    /**
     * Do not use directly in scripts as it can have unpredictable results when sharing image sets
     * <p>
     * To covert to a different imageSet properly,
     * use the {@link com.group_finity.mascot.action.Transform Transform} action.
     *
     * @see com.group_finity.mascot.action.Transform
     */
    void setImageSet(String set);

    /**
     * The location of the mascot's anchor on the screen.
     * <p>
     * Where the point is within the mascot is the determined by the {@code ImageAnchor} property of the current image.
     * This image anchor is generally placed where the mascot touches the environment.
     * For example the default standing frame has it placed at bottom middle (64,128) where the feet are.
     */
    Point getAnchor();

    /**
     * Sets the mascot's anchor position.
     * <p>
     * Calling this method does not immediately change the anchor's position on screen.
     */
    void setAnchor(Point anchor);

    /**
     * Whether the mascot is looking right or left.
     * <p>
     * Note that the default image is treated as facing left.
     */
    boolean isLookRight();

    /**
     * Sets the direction the mascot is facing.
     * <p>
     * When this is set to true the {@code ImageRight} image will be used if present.
     * If not then the flipped version of the default image is used.
     * <p>
     * Calling this method Does not immediately change image.
     *
     * @see com.group_finity.mascot.action.Look
     */
    void setLookRight(boolean lookRight);

}

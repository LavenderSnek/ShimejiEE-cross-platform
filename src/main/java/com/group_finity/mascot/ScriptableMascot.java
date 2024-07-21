package com.group_finity.mascot;

import com.group_finity.mascot.animation.Hotspot;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.manager.ScriptableManager;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

/**
 * API for mascot in scripts
 * <p>
 * This is public API. There might be other callable methods, but they are not guaranteed
 * to remain the same between versions and their use is discouraged.
 */
public interface ScriptableMascot {

    /**
     * Destroys the mascot.
     * <p>
     * To play an animation before, use the {@link com.group_finity.mascot.action.SelfDestruct SelfDestruct} action.
     *
     * @see com.group_finity.mascot.action.SelfDestruct
     */
    void dispose();

    /**
     * Calculates the new bounds based on the current state of the mascot.
     */
    Rectangle getBounds();

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
    ScriptableManager getManager();

    /**
     * The number of mascots of the same image set being controlled by
     * the same manager
     */
    @SuppressWarnings("unused")
    default int getCount() {
        return getManager() == null ? 0 : getManager().getCount(getImageSet());
    }

    /**
     * The total number of mascots being controlled by the same manager
     */
    @SuppressWarnings("unused")
    default int getTotalCount() {
        return getManager() == null ? 0 : getCount();
    }

    /**
     * The currently broadcasting affordances of this mascot.
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
     * Do not use directly in scripts as the correct input can change based on platform.
     * <p>
     * To covert to a different imageSet properly,
     * use the {@link com.group_finity.mascot.action.Transform Transform} action.
     *
     * @see com.group_finity.mascot.action.Transform
     */
    void setImageSet(String set);

    /**
     * The identifier of the current sound.
     * <p>
     * This value can change based on platform and should mostly be used for debugging.
     */
    String getSound();

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

    /**
     * Currently active hotspots of this mascot.
     */
    List<Hotspot> getHotspots();

    /**
     * Whether the currently running action is a dragging action.
     */
    boolean isDragging();

    /**
     * Indicate whether the current action is a dragging action.
     * <p>
     * Setting dragging to true means that a mouse release event will lead to the Thrown action.
     * This method can be used to implement custom dragging actions.
     *
     * @param dragging Whether the currently running action is a dragging action.
     */
    void setDragging(boolean dragging);

    /**
     * Whether a hotspot is currently clicked.
     */
    default boolean isHotspotClicked() {
        return getCursorPosition() != null;
    }

    /**
     * Relative position of the current hotspot click.
     * @return relative position of hotspot click or null if no hotspots are clicked.
     */
    Point getCursorPosition();

    /**
     * Sets the relative position of the current hotspot clicked.
     * @param position relative position of hotspot click or null if no hotspots are clicked.
     */
    void setCursorPosition(Point position);

}

package com.group_finity.mascot.behavior;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.window.TranslucentWindowEvent;

/**
 * Controls the long-term behavior of a mascot by controlling
 * when different actions are called
 */
public interface Behavior {

    /**
     * Called every time the mascot switches to a new action
     *
     * @param mascot The mascot object that the action will be applying to
     */
    void init(Mascot mascot) throws CantBeAliveException;

    /**
     * Progresses the mascot to the next frame
     */
    void next() throws CantBeAliveException;

    /**
     * Called when the left mouse presses down on the mascot.
     *
     * @param e the click event which triggered this action.
     */
    void mousePressed(TranslucentWindowEvent e) throws CantBeAliveException;

    /**
     * Called when the left mouse is released from the mascot.
     *
     * @param e the click event which triggered this action.
     */
    void mouseReleased(TranslucentWindowEvent e) throws CantBeAliveException;

    boolean isHidden();

}

package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;

public interface Action {

    /**
     * Called once at the beginning of the action.
     *
     * @param mascot The mascot object that this action will be applying to
     */
    void init(Mascot mascot) throws VariableException;

    /**
     * Called after each frame. Action will end if this returns false
     *
     * @return {@code false} if the action needs to end now. {@code true} if it should continue.
     */
    boolean hasNext() throws VariableException;

    /**
     * Progresses the animation, called each frame.
     * */
    void next() throws LostGroundException, VariableException;

}

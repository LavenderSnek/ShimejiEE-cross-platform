package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;
import java.util.logging.Logger;

/**
 * Action to make the mascot to dismiss itself
 * <p>
 * The mascot dismisses itself after all the animations/sub-action have finished playing
 * */
public class SelfDestruct extends Animate {

    private static final Logger log = Logger.getLogger(SelfDestruct.class.getName());

    public SelfDestruct(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params) {
        super(schema, animations, params);
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();

        if (getTime() == getAnimation().getDuration() - 1) {
            getMascot().dispose();
        }
    }

}

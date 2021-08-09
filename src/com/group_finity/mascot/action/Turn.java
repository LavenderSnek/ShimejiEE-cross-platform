package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;
import java.util.logging.Logger;

public class Turn extends BorderedAction {

    private static final Logger log = Logger.getLogger(Turn.class.getName());

    public static final String PARAMETER_LOOKRIGHT = "LookRight";

    private boolean turning = false;

    public Turn(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params) {
        super(schema, animations, params);
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        getMascot().setLookRight(isLookRight());
        super.tick();

        if ((getBorder() != null) && !getBorder().isOn(getMascot().getAnchor())) {
            throw new LostGroundException();
        }

        getAnimation().next(getMascot(), getTime());
    }

    @Override
    public boolean hasNext() throws VariableException {
        turning = turning || isLookRight() != getMascot().isLookRight();
        final boolean intime = getTime() < getAnimation().getDuration();

        return super.hasNext() && intime && turning;
    }

    private Boolean isLookRight() throws VariableException {
        return eval(getSchema().getString(PARAMETER_LOOKRIGHT), Boolean.class, !getMascot().isLookRight());
    }

}
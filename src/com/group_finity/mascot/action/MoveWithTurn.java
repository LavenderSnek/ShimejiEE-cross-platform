package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MoveWithTurn extends BorderedAction {

    private static final Logger log = Logger.getLogger(MoveWithTurn.class.getName());

    private static final String PARAMETER_TARGETX = "TargetX";
    private static final int DEFAULT_TARGETX = Integer.MAX_VALUE;

    private static final String PARAMETER_TARGETY = "TargetY";
    private static final int DEFAULT_TARGETY = Integer.MAX_VALUE;

    private boolean turning = false;

    public MoveWithTurn(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params) {
        super(schema, animations, params);
        if (animations.size() < 2) {
            throw new IllegalArgumentException("animations.size < 2");
        }
    }

    @Override
    public boolean hasNext() throws VariableException {
        final int targetX = getTargetX();
        final int targetY = getTargetY();

        boolean noMoveX = false;
        boolean noMoveY = false;

        if (targetX != Integer.MIN_VALUE) {
            if (getMascot().getAnchor().x == targetX) {
                noMoveX = true;
            }
        }

        if (targetY != Integer.MIN_VALUE) {
            if (getMascot().getAnchor().y == targetY) {
                noMoveY = true;
            }
        }

        return super.hasNext() && !noMoveX && !noMoveY;
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();

        if ((getBorder() != null) && !getBorder().isOn(getMascot().getAnchor())) {
            log.log(Level.INFO, "Lost Ground ({0},{1})", new Object[]{getMascot(), this});
            throw new LostGroundException();
        }

        int targetX = getTargetX();
        int targetY = getTargetY();

        boolean down = false;

        if (targetX != DEFAULT_TARGETX) {
            if (getMascot().getAnchor().x != targetX) {
                // activate turn animation if we change directions
                turning = turning || getMascot().getAnchor().x < targetX != getMascot().isLookRight();
                getMascot().setLookRight(getMascot().getAnchor().x < targetX);
            }
        }
        if (targetY != DEFAULT_TARGETY) {
            down = getMascot().getAnchor().y < targetY;
        }

        // check if turning animation has finished
        if (turning && getTime() >= getAnimation().getDuration()) {
            turning = false;
        }

        getAnimation().next(getMascot(), getTime());

        if (targetX != DEFAULT_TARGETX) {
            if ((getMascot().isLookRight() && (getMascot().getAnchor().x >= targetX))
                    || (!getMascot().isLookRight() && (getMascot().getAnchor().x <= targetX)))
            {
                getMascot().setAnchor(new Point(targetX, getMascot().getAnchor().y));
            }
        }
        if (targetY != DEFAULT_TARGETY) {
            if ((down && (getMascot().getAnchor().y >= targetY)) ||
                    (!down && (getMascot().getAnchor().y <= targetY)))
            {
                getMascot().setAnchor(new Point(getMascot().getAnchor().x, targetY));
            }
        }
    }

    @Override
    protected Animation getAnimation() throws VariableException {
        // force to the last animation if turning
        if (turning) {
            return super.getAnimations().get(super.getAnimations().size() - 1);
        } else {
            // had to expose both animations and variables for this
            // is there a better way?
            List<Animation> animations = super.getAnimations();
            for (int index = 0; index < animations.size() - 1; index++) {
                if (animations.get(index).isEffective(getVariables())) {
                    return animations.get(index);
                }
            }
        }

        return null;
    }

    private int getTargetX() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TARGETX), Number.class, DEFAULT_TARGETX).intValue();
    }

    private int getTargetY() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TARGETY), Number.class, DEFAULT_TARGETY).intValue();
    }
}

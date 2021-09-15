package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Move extends BorderedAction {

    private static final Logger log = Logger.getLogger(Move.class.getName());

    /**
     * @custom.shimeji.param
     * @see Move#getTargetX()
     */
    private static final String PARAMETER_TARGETX = "TargetX";
    public static final int DEFAULT_TARGETX = Integer.MAX_VALUE;

    /**
     * @custom.shimeji.param
     * @see Move#getTargetY()
     */
    private static final String PARAMETER_TARGETY = "TargetY";
    public static final int DEFAULT_TARGETY = Integer.MAX_VALUE;

    public Move(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
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
                getMascot().setLookRight(getMascot().getAnchor().x < targetX);
            }
        }
        if (targetY != DEFAULT_TARGETY) {
            down = getMascot().getAnchor().y < targetY;
        }

        getAnimation().next(getMascot(), getTime());

        if (targetX != DEFAULT_TARGETX) {
            if ((getMascot().isLookRight() && (getMascot().getAnchor().x >= targetX))
                    || (!getMascot().isLookRight() && (getMascot().getAnchor().x <= targetX))) {
                getMascot().setAnchor(new Point(targetX, getMascot().getAnchor().y));
            }
        }
        if (targetY != DEFAULT_TARGETY) {
            if ((down && (getMascot().getAnchor().y >= targetY))
                    || (!down && (getMascot().getAnchor().y <= targetY))) {
                getMascot().setAnchor(new Point(getMascot().getAnchor().x, targetY));
            }
        }

    }

    /**
     * The action stops when the mascot reaches this x-axis
     * */
    private int getTargetX() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TARGETX), Number.class, DEFAULT_TARGETX).intValue();
    }

    /**
     * The action stops when the mascot reaches this y-axis
     * */
    private int getTargetY() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TARGETY), Number.class, DEFAULT_TARGETY).intValue();
    }

}

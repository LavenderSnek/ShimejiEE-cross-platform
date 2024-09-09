package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.util.List;
import java.util.logging.Logger;

public class Move extends BorderedAction {

    private static final Logger log = Logger.getLogger(Move.class.getName());

    record MoveDelegate(BorderedAction base) {

        public static final String PARAMETER_TARGETX = "TargetX";
        private static final int DEFAULT_TARGETX = Integer.MAX_VALUE;

        public static final String PARAMETER_TARGETY = "TargetY";
        private static final int DEFAULT_TARGETY = Integer.MAX_VALUE;

        boolean hasReachedX(int targetX) {
            return targetX != Integer.MIN_VALUE && base.getMascot().getAnchor().x == targetX;
        }

        boolean hasReachedY(int targetY) {
            return targetY != Integer.MIN_VALUE && base.getMascot().getAnchor().y == targetY;
        }

        boolean hasNotReachedEitherTarget() throws VariableException {
            final int targetX = getTargetX();
            final int targetY = getTargetY();

            return !(hasReachedX(targetX) || hasReachedY(targetY));
        }

        boolean shouldMascotTurnAround(int targetX) {
            if (targetX != DEFAULT_TARGETX && base.getMascot().getAnchor().x != targetX) {
                return base.getMascot().isLookRight() != base.getMascot().getAnchor().x < targetX;
            }
            return false;
        }

        void updateLocation(int targetX, int targetY) throws VariableException {
            boolean aboveTY = targetY != DEFAULT_TARGETY && base.getMascot().getAnchor().y < targetY;

            base.getAnimation().next(base.getMascot(), base.getTime());

            if (targetX != DEFAULT_TARGETX) {
                if ((base.getMascot().isLookRight() && (base.getMascot().getAnchor().x >= targetX)) || (!base.getMascot().isLookRight() && (base.getMascot().getAnchor().x <= targetX))) {
                    base.getMascot().setAnchor(new Point(targetX, base.getMascot().getAnchor().y));
                }
            }
            if (targetY != DEFAULT_TARGETY) {
                if ((aboveTY && (base.getMascot().getAnchor().y >= targetY)) || (!aboveTY && (base.getMascot().getAnchor().y <= targetY))) {
                    base.getMascot().setAnchor(new Point(base.getMascot().getAnchor().x, targetY));
                }
            }
        }

        Animation getNextEffectiveTurnAnimation() throws VariableException {
            for (Animation animation : base.getAnimations()) {
                // do isTurn check after condition to keep script side effects consistent
                if (animation.isEffective(base.getVariables()) && animation.isTurn()) {
                    return animation;
                }
            }
            return null;
        }

        /**
         * The action stops when the mascot reaches this x-axis
         */
        int getTargetX() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_TARGETX), Number.class, DEFAULT_TARGETX).intValue();
        }

        /**
         * The action stops when the mascot reaches this y-axis
         */
        int getTargetY() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_TARGETY), Number.class, DEFAULT_TARGETY).intValue();
        }
    }

    private final MoveDelegate del = new MoveDelegate(this);

    private boolean hasTurnAnimation = false;
    private boolean turning = false;

    public Move(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public void init(Mascot mascot) throws VariableException {
        super.init(mascot);
        hasTurnAnimation = getAnimations().stream().anyMatch(Animation::isTurn);
    }

    @Override
    public boolean hasNext() throws VariableException {
        return super.hasNext() && (del.hasNotReachedEitherTarget() || isTurning());
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();

        checkForLostGround();

        int targetX = del.getTargetX();
        int targetY = del.getTargetY();

        if (del.shouldMascotTurnAround(targetX)) {
            getMascot().setLookRight(!getMascot().isLookRight()); // toggle turn
            turning = true;
        }

        // check if turning animation has finished
        if (isTurning() && (getTime() >= getAnimation().getDuration())) {
            turning = false;
        }

        del.updateLocation(targetX, targetY);
    }

    @Override
    protected Animation getAnimation() throws VariableException {
        if (hasTurnAnimation && isTurning()) {
            return del.getNextEffectiveTurnAnimation();
        }
        return super.getAnimation();
    }

    protected final boolean isTurning() {
        return turning;
    }

}

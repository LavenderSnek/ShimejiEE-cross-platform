package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.Tr;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScanMove extends BorderedAction {

    private static final Logger log = Logger.getLogger(ScanMove.class.getName());

    record ScanDelegate(ActionBase base) {

        public static final String PARAMETER_BEHAVIOUR = "Behaviour";
        private static final String DEFAULT_BEHAVIOUR = "";

        public static final String PARAMETER_TARGETBEHAVIOUR = "TargetBehaviour";
        private static final String DEFAULT_TARGETBEHAVIOUR = "";

        public static final String PARAMETER_TARGETLOOK = "TargetLook";
        private static final boolean DEFAULT_TARGETLOOK = false;

        void setMascotFinalStates(Mascot target) throws VariableException {
            try {
                base.getMascot().setBehavior(base.getMascot().getOwnImageSet().getConfiguration().buildBehavior(getBehavior()));
                target.setBehavior(target.getOwnImageSet().getConfiguration().buildBehavior(getTargetBehavior()));

                if (getTargetLook() && target.isLookRight() == base.getMascot().isLookRight()) {
                    target.setLookRight(!base.getMascot().isLookRight());
                }

            } catch (final NullPointerException | BehaviorInstantiationException | CantBeAliveException e) {
                log.log(Level.SEVERE, "Fatal Exception", e);
                throw new VariableException(Tr.tr("FailedSetBehaviourErrorMessage"), e);
            }
        }

        String getBehavior() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_BEHAVIOUR), String.class, DEFAULT_BEHAVIOUR);
        }

        String getTargetBehavior() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_TARGETBEHAVIOUR), String.class, DEFAULT_TARGETBEHAVIOUR);
        }

        boolean getTargetLook() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_TARGETLOOK), Boolean.class, DEFAULT_TARGETLOOK);
        }

    }

    private final Move.MoveDelegate moveDel = new Move.MoveDelegate(this);
    private final ScanDelegate scanDel = new ScanDelegate(this);

    private boolean hasTurnAnimation = false;
    private boolean turning = false;

    private WeakReference<Mascot> target;

    public ScanMove(ResourceBundle schema, List<Animation> animations, VariableMap params) {
        super(schema, animations, params);
    }

    @Override
    public void init(Mascot mascot) throws VariableException {
        super.init(mascot);

        getMascot().getAffordances().clear();

        if (getMascot().getManager() != null) {
            target = getMascot().getManager().getMascotWithAffordance(getAffordance());
        }

        putVariable("target", target != null ? target.get() : null);

        hasTurnAnimation = getAnimations().stream().anyMatch(Animation::isTurn);
    }

    @Override
    public boolean hasNext() throws VariableException {
        if (getMascot().getManager() == null) {
            return super.hasNext();
        }

        if (target == null) {
            target = getMascot().getManager().getMascotWithAffordance(getAffordance());
        }

        var effective = super.hasNext();

        var validTarget = target != null
                          && target.get() != null
                          && target.get().getAffordances().contains(getAffordance());

        return effective && validTarget;
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();

        checkForLostGround();

        getMascot().getAffordances().clear();

        var targetRef = Objects.requireNonNull(target.get());

        int targetX = targetRef.getAnchor().x;
        int targetY = targetRef.getAnchor().y;

        if (moveDel.shouldMascotTurnAround(targetX)) {
            getMascot().setLookRight(!getMascot().isLookRight()); // toggle turn
            turning = true;
        }

        // check if turning animation has finished
        if (isTurning() && (getTime() >= getAnimation().getDuration())) {
            turning = false;
        }

        moveDel.updateLocation(targetX, targetY);

        //----

        if (!isTurning() && moveDel.hasReachedX(targetX) && moveDel.hasReachedY(targetY)) {
            scanDel.setMascotFinalStates(targetRef);
        }
    }

    @Override
    protected Animation getAnimation() throws VariableException {
        if (hasTurnAnimation && isTurning()) {
            return moveDel.getNextEffectiveTurnAnimation();
        }
        return super.getAnimation();
    }

    protected final boolean isTurning() {
        return turning;
    }

}

package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.Tr;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a new mascot
 * <p>
 * The mascot will be created after the animations/sub-actions have finished playing
 * */
public class Breed extends Animate {

    private static final Logger log = Logger.getLogger(Breed.class.getName());

    static class Delegate {
        public static final String PARAMETER_BORNX = "BornX";
        private static final int DEFAULT_BORNX = 0;

        public static final String PARAMETER_BORNY = "BornY";
        private static final int DEFAULT_BORNY = 0;

        public static final String PARAMETER_BORNBEHAVIOUR = "BornBehaviour";
        private static final String DEFAULT_BORNBEHAVIOUR = "";

        public static final String PARAMETER_BORNMASCOT = "BornMascot";
        private static final String DEFAULT_BORNMASCOT = null;

        public static final String PARAMETER_BORNTRANSIENT = "BornTransient";
        private static final boolean DEFAULT_BORNTRANSIENT = false;

        public static final String PARAMETER_BORNINTERVAL = "BornInterval";
        private static final int DEFAULT_BORNINTERVAL = 1;

        public static final String PARAMETER_BORNCOUNT = "BornCount";
        private static final int DEFAULT_BORNCOUNT = 1;

        private final ActionBase base;

        Delegate(ActionBase base) {
            this.base = base;
        }

        boolean isFrameBeforeLast() throws VariableException {
            return base.getTime() == base.getAnimation().getDuration() - 1;
        }

        boolean isIntervalFrame() throws VariableException {
            return base.getTime() % getBornInterval() == 0;
        }

        boolean isAllowed() throws VariableException {
            return getBornTransient()
                    ? base.getMascot().isTransientBreedingAllowed()
                    : base.getMascot().isBreedingAllowed();
        }

        void breed() throws VariableException {
            for (int i = 0; i < getBornCount(); i++) {
                breedOnce();
            }
        }

        void breedOnce() throws VariableException {
            String childType = base.getMascot().getImageSetDependency(getBornMascot()) != null
                    ? getBornMascot()
                    : base.getMascot().getImageSet();

            final Mascot newMascot = Mascot.createBlankFrom(base.getMascot());
            newMascot.setImageSet(childType);

            log.log(Level.INFO, "Breed Mascot ({0},{1},{2})", new Object[]{base.getMascot(), this, newMascot});

            if (base.getMascot().isLookRight()) {
                newMascot.setAnchor(new Point(
                        base.getMascot().getAnchor().x - getBornX(),
                        base.getMascot().getAnchor().y + getBornY()
                ));
            } else {
                newMascot.setAnchor(new Point(
                        base.getMascot().getAnchor().x + getBornX(),
                        base.getMascot().getAnchor().y + getBornY()
                ));
            }

            newMascot.setLookRight(base.getMascot().isLookRight());

            try {
                newMascot.setBehavior(newMascot.getOwnImageSet().getConfiguration().buildBehavior(getBornBehaviour()));
                base.getMascot().getManager().add(newMascot);

            } catch (final BehaviorInstantiationException | CantBeAliveException e) {
                log.log(Level.SEVERE, "Fatal Exception", e);
                newMascot.dispose();
                throw new VariableException(Tr.tr("FailedSetBehaviourErrorMessage"), e);
            }
        }

        /**
         * The X co-ordinates where the shimeji is spawned, this is relative to the shimeji creating it.
         * */
        int getBornX() throws VariableException {
            int res = base.eval(base.getSchema().getString(PARAMETER_BORNX), Number.class, DEFAULT_BORNX).intValue();
            return (int) Math.round(res * base.getMascot().getScaling());
        }

        /**
         * The Y co-ordinates where the shimeji is spawned, this is relative to the shimeji creating it.
         * */
        int getBornY() throws VariableException {
            int res = base.eval(base.getSchema().getString(PARAMETER_BORNY), Number.class, DEFAULT_BORNY).intValue();
            return (int) Math.round(res * base.getMascot().getScaling());
        }

        /**
         * The starting behaviour of the newly created mascot when it spawns
         * */
        String getBornBehaviour() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_BORNBEHAVIOUR), String.class, DEFAULT_BORNBEHAVIOUR);
        }

        /**
         * Name of the imageSet that the new mascot will be of.
         * <p>
         * If this is not provided, the imageSet of the creating mascot will be used.
         */
        String getBornMascot() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_BORNMASCOT), String.class, DEFAULT_BORNMASCOT);
        }

        /**
         * Whether the mascot will be considered 'transient'.
         * <p>
         * Unlike regular mascots, transient mascots will not be prevented from spawning when the user
         * disables regular breeding. Use this for short term mascots such as projectiles and particles.
         */
        boolean getBornTransient() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_BORNTRANSIENT), Boolean.class, DEFAULT_BORNTRANSIENT);
        }

        /**
         * The number of ticks between each spawn.
         * <p>
         * Does not affect plain breeding, only used by BreedMove and BreedJump. Needs to be greater than 0
         */
        int getBornInterval() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_BORNINTERVAL), Number.class, DEFAULT_BORNINTERVAL).intValue();
        }

        void validateBornInterval() throws VariableException {
            if (getBornInterval() < 1) {
                throw new VariableException(PARAMETER_BORNINTERVAL + ": Error, must be > 0");
            }
        }

        /**
         * Multiplier for number of mascots spawned
         * <p>
         * Needs to be greater than 0
         */
        int getBornCount() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_BORNCOUNT), Number.class, DEFAULT_BORNCOUNT).intValue();
        }

        void validateBornCount() throws VariableException {
            if (getBornCount() < 1) {
                throw new VariableException(PARAMETER_BORNCOUNT + ": Error, must be > 0");
            }
        }
    }

    private final Delegate del = new Delegate(this);

    public Breed(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public void init(Mascot mascot) throws VariableException {
        super.init(mascot);

        del.validateBornCount();
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();

        if (del.isFrameBeforeLast() && del.isAllowed()) {
            del.breed();
        }
    }

}

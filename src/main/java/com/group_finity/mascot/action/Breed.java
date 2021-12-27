package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
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

    /**
     * @see Breed#getBornX()
     * */
    public static final String PARAMETER_BORNX = "BornX";
    private static final int DEFAULT_BORNX = 0;

    /**
     * @see Breed#getBornY()
     * */
    public static final String PARAMETER_BORNY = "BornY";
    private static final int DEFAULT_BORNY = 0;

    /**
     * @see Breed#getBornBehaviour()
     * */
    public static final String PARAMETER_BORNBEHAVIOUR = "BornBehaviour";
    private static final String DEFAULT_BORNBEHAVIOUR = "";

    /**
     * @see Breed#getBornMascot()
     * */
    public static final String PARAMETER_BORNMASCOT = "BornMascot";
    private static final String DEFAULT_BORNMASCOT = "";

    private int scaling;

    public Breed(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public void init(final Mascot mascot) throws VariableException {
        super.init(mascot);

        scaling = Integer.parseInt(Main.getInstance().getProperties().getProperty("Scaling", "1"));
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();

        if (getTime() == getAnimation().getDuration() - 1 && Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Breeding", "true"))) {
            breed();
        }
    }

    private void breed() throws VariableException {
        String childType = Main.getInstance().getConfiguration(getBornMascot()) != null ? getBornMascot() : getMascot().getImageSet();

        final Mascot newMascot = new Mascot(childType);

        log.log(Level.INFO, "Breed Mascot ({0},{1},{2})", new Object[]{getMascot(), this, newMascot});

        if (getMascot().isLookRight()) {
            newMascot.setAnchor(new Point(
                    getMascot().getAnchor().x - (getBornX() * scaling),
                    getMascot().getAnchor().y + (getBornY() * scaling)
            ));
        } else {
            newMascot.setAnchor(new Point(
                    getMascot().getAnchor().x + (getBornX() * scaling),
                    getMascot().getAnchor().y + (getBornY() * scaling)
            ));
        }

        newMascot.setLookRight(getMascot().isLookRight());

        try {
            newMascot.setBehavior(Main.getInstance().getConfiguration(childType).buildBehavior(getBornBehaviour()));
            getMascot().getManager().add(newMascot);

        } catch (final BehaviorInstantiationException | CantBeAliveException e) {
            log.log(Level.SEVERE, "Fatal Exception", e);
            Main.showError(
                    Tr.tr("FailedCreateNewShimejiErrorMessage")
                            + "\n" + e.getMessage()
                            + "\n" + Tr.tr("SeeLogForDetails")
            );
            newMascot.dispose();
        }
    }

    /**
     * The X co-ordinates where the shimeji is spawned, this is relative to the shimeji creating it.
     * */
    private int getBornX() throws VariableException {
        return eval(getSchema().getString(PARAMETER_BORNX), Number.class, DEFAULT_BORNX).intValue();
    }

    /**
     * The Y co-ordinates where the shimeji is spawned, this is relative to the shimeji creating it.
     * */
    private int getBornY() throws VariableException {
        return eval(getSchema().getString(PARAMETER_BORNY), Number.class, DEFAULT_BORNY).intValue();
    }

    /**
     * The starting behaviour of the newly created mascot when it spawns
     * */
    private String getBornBehaviour() throws VariableException {
        return eval(getSchema().getString(PARAMETER_BORNBEHAVIOUR), String.class, DEFAULT_BORNBEHAVIOUR);
    }

    /**
     * Name of the imageSet that the new mascot will be of.
     * <p>
     * If this is not provided, the imageSet of the creating mascot will be used.
     * */
    private String getBornMascot() throws VariableException {
        return eval(getSchema().getString(PARAMETER_BORNMASCOT), String.class, DEFAULT_BORNMASCOT);
    }

}

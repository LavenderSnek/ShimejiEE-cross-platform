package com.group_finity.mascot.behavior;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.action.ActionBase;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Sample Behavior.
 */
public class UserBehavior implements Behavior {
    private static final Logger log = Logger.getLogger(UserBehavior.class.getName());

    public static final String BEHAVIOURNAME_FALL = "Fall";

    public static final String BEHAVIOURNAME_DRAGGED = "Dragged";

    public static final String BEHAVIOURNAME_THROWN = "Thrown";

    private final String name;

    private final Configuration configuration;

    private final Action action;

    private Mascot mascot;

    private boolean hidden;

    public UserBehavior(final String name, final Action action, final Configuration configuration, boolean hidden) {
        this.name = name;
        this.configuration = configuration;
        this.action = action;
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return "Behavior(" + getName() + ")";
    }

    @Override
    public synchronized void init(final Mascot mascot) throws CantBeAliveException {

        this.setMascot(mascot);

        log.log(Level.INFO, "Default Behavior({0},{1})", new Object[]{this.getMascot(), this});

        try {
            getAction().init(mascot);
            if (!getAction().hasNext()) {
                try {
                    mascot.setBehavior(this.getConfiguration().buildBehavior(getName(), mascot));
                } catch (final BehaviorInstantiationException e) {
                    throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedInitialiseFollowingBehaviourErrorMessage"), e);
                }
            }
        } catch (final VariableException e) {
            throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("VariableEvaluationErrorMessage"), e);
        }

    }

    private Configuration getConfiguration() {
        return this.configuration;
    }

    private Action getAction() {
        return this.action;
    }

    private String getName() {
        return this.name;
    }

    /**
     * On Mouse Pressed.
     * Start dragging.
     *
     * @ Throws CantBeAliveException
     */
    public synchronized void mousePressed(final MouseEvent event) throws CantBeAliveException {

        if (SwingUtilities.isLeftMouseButton(event)) {
            // check if this action has dragging disabled
            boolean draggable = true;
            if (action != null && action instanceof ActionBase) {
                try {
                    draggable = ((ActionBase) action).isDraggable();
                } catch (VariableException ex) {
                    throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedDragActionInitialiseErrorMessage"), ex);
                }
            }

            if (draggable) {
                // Begin dragging
                try {
                    getMascot().setBehavior(this.getConfiguration().buildBehavior(configuration.getSchema().getString(BEHAVIOURNAME_DRAGGED)));
                } catch (final BehaviorInstantiationException e) {
                    throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedDragActionInitialiseErrorMessage"), e);
                }
            }
        }
    }

    /**
     * On Mouse Release.
     * End dragging.
     *
     * @ Throws CantBeAliveException
     */
    public synchronized void mouseReleased(final MouseEvent event) throws CantBeAliveException {
        if (SwingUtilities.isLeftMouseButton(event)) {
            // check if this action has dragging disabled
            boolean draggable = true;
            if (action != null && action instanceof ActionBase) {
                try {
                    draggable = ((ActionBase) action).isDraggable();
                } catch (VariableException ex) {
                    throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedDropActionInitialiseErrorMessage"), ex);
                }
            }

            if (draggable) {
                // Termination of drag
                try {
                    getMascot().setBehavior(this.getConfiguration().buildBehavior(configuration.getSchema().getString(BEHAVIOURNAME_THROWN)));
                } catch (final BehaviorInstantiationException e) {
                    throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedDropActionInitialiseErrorMessage"), e);
                }
            }
        }
    }

    @Override
    public synchronized void next() throws CantBeAliveException {

        try {
            if (getAction().hasNext()) {
                getAction().next();
            }

            if (getAction().hasNext()) {

                if ((getMascot().getBounds().getX() + getMascot().getBounds().getWidth() <=
                        getEnvironment().getScreen().getLeft())
                        || (getEnvironment().getScreen().getRight() <= getMascot().getBounds().getX())
                        || (getEnvironment().getScreen().getBottom() <= getMascot().getBounds().getY())) {

                    log.log(Level.INFO, "Out of the screen bounds({0},{1})", new Object[]{getMascot(), this});

                    getMascot().setAnchor(
                            new Point((int) (Math.random() * (getEnvironment().getScreen().getRight() - getEnvironment()
                                    .getScreen().getLeft()))
                                    + getEnvironment().getScreen().getLeft(), getEnvironment().getScreen().getTop() - 256));

                    try {
                        getMascot().setBehavior(this.getConfiguration().buildBehavior(configuration.getSchema().getString(BEHAVIOURNAME_FALL)));
                    } catch (final BehaviorInstantiationException e) {
                        throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedFallingActionInitialiseErrorMessage"), e);
                    }
                }

            } else {
                log.log(Level.INFO, "Completed Behavior ({0},{1})", new Object[]{getMascot(), this});

                try {
                    getMascot().setBehavior(this.getConfiguration().buildBehavior(getName(), getMascot()));
                } catch (final BehaviorInstantiationException e) {
                    throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedInitialiseFollowingActionsErrorMessage"), e);
                }
            }
        } catch (final LostGroundException e) {
            log.log(Level.INFO, "Lost Ground ({0},{1})", new Object[]{getMascot(), this});

            try {
                getMascot().setBehavior(this.getConfiguration().buildBehavior(configuration.getSchema().getString(BEHAVIOURNAME_FALL)));
            } catch (final BehaviorInstantiationException ex) {
                throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedFallingActionInitialiseErrorMessage"), e);
            }
        } catch (final VariableException e) {
            throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("VariableEvaluationErrorMessage"), e);
        }

    }

    private void setMascot(final Mascot mascot) {
        this.mascot = mascot;
    }

    private Mascot getMascot() {
        return this.mascot;
    }

    protected MascotEnvironment getEnvironment() {
        return getMascot().getEnvironment();
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }
}

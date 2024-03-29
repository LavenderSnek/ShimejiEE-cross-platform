package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Action to resist a dragging action.
 * <p>
 * I think this meant to be "Resist" but the misspelling is now part of the api ¯\_(ツ)_/¯
 */
public class Regist extends ActionBase {

    private static final Logger log = Logger.getLogger(Regist.class.getName());

    public Regist(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public boolean hasNext() throws VariableException {
        final boolean notMoved = Math.abs(getEnvironment().getCursor().getX() - getMascot().getAnchor().x) < 5;
        return super.hasNext() && notMoved;
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        getMascot().setDragging(true);

        final Animation animation = getAnimation();
        animation.next(getMascot(), getTime());

        if (getTime() + 1 >= getAnimation().getDuration()) {
            getMascot().setLookRight(Math.random() < 0.5);

            log.log(Level.INFO, "Lost Ground ({0},{1})", new Object[]{getMascot(), this});
            throw new LostGroundException();
        }
    }

    @Override
    protected void refreshHotspots() {
        // hotspots unsupported for action
        getMascot().getHotspots().clear();
    }

}

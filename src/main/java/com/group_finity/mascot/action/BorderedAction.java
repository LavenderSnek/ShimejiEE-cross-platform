package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.environment.Border;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;
import java.util.logging.Logger;

/**
 * That base class of actions that depend on sticking to a certain wall
 * */
public abstract class BorderedAction extends ActionBase {

    private static final Logger log = Logger.getLogger(BorderedAction.class.getName());

    /**
     * @custom.shimeji.param
     * @see BorderedAction#getBorderType()
     * */
    public static final String PARAMETER_BORDERTYPE = "BorderType";
    private static final String DEFAULT_BORDERTYPE = null;

    public static final String BORDERTYPE_CEILING = "Ceiling";
    public static final String BORDERTYPE_WALL = "Wall";
    public static final String BORDERTYPE_FLOOR = "Floor";

    private Border border;

    public BorderedAction(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public void init(final Mascot mascot) throws VariableException {
        super.init(mascot);

        final String borderType = getBorderType();

        if (getSchema().getString(BORDERTYPE_CEILING).equals(borderType)) {
            this.setBorder(getEnvironment().getCeiling());
        } else if (getSchema().getString(BORDERTYPE_WALL).equals(borderType)) {
            this.setBorder(getEnvironment().getWall());
        } else if (getSchema().getString(BORDERTYPE_FLOOR).equals(borderType)) {
            this.setBorder(getEnvironment().getFloor());
        }
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        if (getBorder() != null) {
            getMascot().setAnchor(getBorder().move(getMascot().getAnchor()));
        }
    }

    /**
     * The type of border the action will be operating on
     * <p>
     * Options, (see schema properties for true values):
     * <ul>
     *   <li>{@value BORDERTYPE_CEILING}</li>
     *   <li>{@value BORDERTYPE_WALL}</li>
     *   <li>{@value BORDERTYPE_FLOOR}</li>
     * </ul>
     * */
    private String getBorderType() throws VariableException {
        return eval(getSchema().getString(PARAMETER_BORDERTYPE), String.class, DEFAULT_BORDERTYPE);
    }

    protected Border getBorder() {
        return this.border;
    }

    private void setBorder(final Border border) {
        this.border = border;
    }

}

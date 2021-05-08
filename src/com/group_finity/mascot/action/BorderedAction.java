package com.group_finity.mascot.action;

import java.util.List;
import java.util.logging.Logger;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.environment.Border;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;


public abstract class BorderedAction extends ActionBase {

    private static final Logger log = Logger.getLogger(BorderedAction.class.getName());

    private static final String PARAMETER_BORDERTYPE = "BorderType";

    public static final String DEFAULT_BORDERTYPE = null;

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

    private String getBorderType() throws VariableException {
        return eval(getSchema().getString(PARAMETER_BORDERTYPE), String.class, DEFAULT_BORDERTYPE);
    }

    private void setBorder(final Border border) {
        this.border = border;
    }

    protected Border getBorder() {
        return this.border;
    }

}

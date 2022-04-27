package com.group_finity.mascot.action;

import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.util.logging.Logger;

/**
 * An action that shifts the mascot by an amount
 */
public class Offset extends InstantAction {

    private static final Logger log = Logger.getLogger(Offset.class.getName());

    public static final String PARAMETER_OFFSETX = "X";
    public static final int DEFAULT_OFFSETX = 0;

    public static final String PARAMETER_OFFSETY = "Y";
    public static final int DEFAULT_OFFSETY = 0;

    public Offset(java.util.ResourceBundle schema, final VariableMap params) {
        super(schema, params);
    }

    @Override
    protected void apply() throws VariableException {
        getMascot().setAnchor(new Point(
                getMascot().getAnchor().x + getOffsetX(),
                getMascot().getAnchor().y + getOffsetY()
        ));
    }

    private int getOffsetX() throws VariableException {
        int res = eval(getSchema().getString(PARAMETER_OFFSETX), Number.class, DEFAULT_OFFSETX).intValue();
        return (int) Math.round(res * getMascot().getScaling());
    }

    private int getOffsetY() throws VariableException {
        int res = eval(getSchema().getString(PARAMETER_OFFSETY), Number.class, DEFAULT_OFFSETY).intValue();
        return (int) Math.round(res * getMascot().getScaling());
    }

}

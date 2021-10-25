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
        getMascot().getAnchor().translate(
                getOffsetX(),
                getOffsetY()
        );
    }

    private int getOffsetX() throws VariableException {
        return eval(getSchema().getString(PARAMETER_OFFSETX), Number.class, DEFAULT_OFFSETX).intValue();
    }

    private int getOffsetY() throws VariableException {
        return eval(getSchema().getString(PARAMETER_OFFSETY), Number.class, DEFAULT_OFFSETY).intValue();
    }

}

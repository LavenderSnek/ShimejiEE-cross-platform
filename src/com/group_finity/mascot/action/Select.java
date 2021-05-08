package com.group_finity.mascot.action;

import java.util.logging.Logger;

import com.group_finity.mascot.script.VariableMap;


public class Select extends ComplexAction
{
    private static final Logger log = Logger.getLogger( Select.class.getName( ) );

    public Select( java.util.ResourceBundle schema, final VariableMap params, final Action... actions )
    {
        super( schema, params, actions);
    }
}

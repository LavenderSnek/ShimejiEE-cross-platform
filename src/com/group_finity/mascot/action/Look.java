package com.group_finity.mascot.action;

import java.util.logging.Logger;

import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;


public class Look extends InstantAction
{
    private static final Logger log = Logger.getLogger( Look.class.getName( ) );

    public static final String PARAMETER_LOOKRIGHT = "LookRight";

    public Look( java.util.ResourceBundle schema, final VariableMap params )
    {
        super( schema, params );
    }

    @Override
    protected void apply( ) throws VariableException
    {
        getMascot( ).setLookRight( isLookRight( ) );
    }

    private Boolean isLookRight( ) throws VariableException
    {
        return eval( getSchema( ).getString( PARAMETER_LOOKRIGHT ), Boolean.class, !getMascot( ).isLookRight( ) );
    }
}

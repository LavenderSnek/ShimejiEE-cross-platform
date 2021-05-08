package com.group_finity.mascot.action;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;


public class Transform extends Animate
{
    private static final Logger log = Logger.getLogger( Transform.class.getName( ) );
    
    public static final String PARAMETER_TRANSFORMBEHAVIOUR = "TransformBehaviour";

    private static final String DEFAULT_TRANSFORMBEHAVIOUR = "";

    public static final String PARAMETER_TRANSFORMMASCOT = "TransformMascot";

    private static final String DEFAULT_TRANSFORMMASCOT = "";

    public Transform( java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params )
    {
        super( schema, animations, params );
    }

    @Override
    protected void tick( ) throws LostGroundException, VariableException
    {
        super.tick( );

        if( getTime( ) == getAnimation( ).getDuration( ) - 1 && Boolean.parseBoolean( Main.getInstance( ).getProperties( ).getProperty( "Transformation", "true" ) ) )
        {
            transform( );
        }
    }

    private void transform( ) throws VariableException
    {
        String childType = Main.getInstance( ).getConfiguration( getTransformMascot( ) ) != null ? getTransformMascot( ) : getMascot( ).getImageSet( );
        
        try
        {
            getMascot( ).setImageSet( childType );
            getMascot( ).setBehavior( Main.getInstance( ).getConfiguration( childType ).buildBehavior( getTransformBehavior( ) ) );
        }
        catch( final BehaviorInstantiationException | CantBeAliveException e )
        {
            log.log( Level.SEVERE, "Fatal Exception", e );
            Main.showError( Main.getInstance( ).getLanguageBundle( ).getString( "FailedCreateNewShimejiErrorMessage" ) + "\n"
                    + e.getMessage( ) + "\n"
                    + Main.getInstance( ).getLanguageBundle( ).getString( "SeeLogForDetails" ) );
        }
    }

    private String getTransformBehavior( ) throws VariableException
    {
        return eval( getSchema( ).getString( PARAMETER_TRANSFORMBEHAVIOUR ), String.class, DEFAULT_TRANSFORMBEHAVIOUR );
    }

    private String getTransformMascot( ) throws VariableException
    {
        return eval( getSchema( ).getString( PARAMETER_TRANSFORMMASCOT ), String.class, DEFAULT_TRANSFORMMASCOT );
    }
}

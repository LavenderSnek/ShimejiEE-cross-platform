package com.group_finity.mascot.action;

import java.util.logging.Logger;

import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import com.group_finity.mascot.sound.Sounds;
import java.util.ArrayList;
import javax.sound.sampled.Clip;

/**
 *
 * @author Kilkakon
 */
public class Mute extends InstantAction
{
    private static final Logger log = Logger.getLogger( Offset.class.getName( ) );

    public static final String PARAMETER_SOUND = "Sound";

    private static final String DEFAULT_SOUND = null;

    public Mute( java.util.ResourceBundle schema, final VariableMap params )
    {
        super( schema, params );
    }

    @Override
    protected void apply( ) throws VariableException
    {
        String soundName = getSound( );
        if( soundName != null )
        {
            ArrayList<Clip> clips = Sounds.getSoundsIgnoringVolume( "./sound" + soundName );
            if( clips.size( ) > 0 )
            {
                for( Clip clip : clips )
                { 
                    if( clip != null && clip.isRunning( ) )
                        clip.stop( );
                }
            }
            else
            {
                clips = Sounds.getSoundsIgnoringVolume( "./sound/" + getMascot( ).getImageSet( ) + soundName );
                if( clips.size( ) > 0 )
                {
                    for( Clip clip : clips )
                    { 
                        if( clip != null && clip.isRunning( ) )
                            clip.stop( );
                    }
                }
                else
                {
                    clips = Sounds.getSoundsIgnoringVolume( "./img/" + getMascot( ).getImageSet( ) + "/sound" + soundName );
                    for( Clip clip : clips )
                    { 
                        if( clip != null && clip.isRunning( ) )
                            clip.stop( );
                    }
                }
            }
        }
        else
        {
            if( !Sounds.isMuted( ) )
            {
                Sounds.setMuted( true );
                Sounds.setMuted( false );
            }
        }
    }

    private String getSound( ) throws VariableException
    {
        return eval( getSchema( ).getString( PARAMETER_SOUND ), String.class, DEFAULT_SOUND );
    }
}
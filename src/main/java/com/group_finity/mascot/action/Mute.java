package com.group_finity.mascot.action;

import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import javax.sound.sampled.Clip;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author Kilkakon
 */
public class Mute extends InstantAction {

    private static final Logger log = Logger.getLogger(Offset.class.getName());

    public static final String PARAMETER_SOUND = "Sound";
    public static final String DEFAULT_SOUND = null;

    public Mute(ResourceBundle schema, final VariableMap params) {
        super(schema, params);
    }

    @Override
    protected void apply() throws VariableException {
        String soundName = getSound();
        if (soundName != null) {
            getMascot().getOwnImageSet()
                    .getSounds().getIgnoringVolume(soundName)
                    .forEach(Clip::stop);
        }
    }

    private String getSound() throws VariableException {
        return eval(getSchema().getString(PARAMETER_SOUND), String.class, DEFAULT_SOUND);
    }

}
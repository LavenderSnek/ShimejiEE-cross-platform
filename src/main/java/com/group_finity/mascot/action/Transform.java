package com.group_finity.mascot.action;

import com.group_finity.mascot.Tr;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Transform extends Animate {

    private static final Logger log = Logger.getLogger(Transform.class.getName());

    public static final String PARAMETER_TRANSFORMBEHAVIOUR = "TransformBehaviour";
    private static final String DEFAULT_TRANSFORMBEHAVIOUR = "";

    public static final String PARAMETER_TRANSFORMMASCOT = "TransformMascot";
    private static final String DEFAULT_TRANSFORMMASCOT = null;

    public Transform(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params) {
        super(schema, animations, params);
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();

        if (getTime() == getAnimation().getDuration() - 1 && getMascot().isTransformationAllowed()) {
            transform();
        }
    }

    private void transform() throws VariableException {
        var childType =  getTransformMascot() != null
                ? getTransformMascot()
                : getMascot().getImageSet();

        if (getMascot().getImageSetDependency(childType) == null) {
            log.log(Level.WARNING, "Could not load mascot: {0}", childType);
            return;
        }

        try {
            getMascot().setImageSet(childType);
            getMascot().setBehavior(getMascot().getOwnImageSet().getConfiguration().buildBehavior(getTransformBehavior()));

        } catch (final BehaviorInstantiationException | CantBeAliveException e) {
            log.log(Level.SEVERE, "Fatal Exception", e);
            // not the right exception but that's for the logging/error handling rewrite
            throw new VariableException(Tr.tr("FailedCreateNewShimejiErrorMessage"), e);
        }
    }

    private String getTransformBehavior() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TRANSFORMBEHAVIOUR), String.class, DEFAULT_TRANSFORMBEHAVIOUR);
    }

    private String getTransformMascot() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TRANSFORMMASCOT), String.class, DEFAULT_TRANSFORMMASCOT);
    }

}

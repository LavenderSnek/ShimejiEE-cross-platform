package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MoveWithTurn extends Move {

    private static final Logger log = Logger.getLogger(MoveWithTurn.class.getName());

    public MoveWithTurn(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params) {
        super(schema, animations, params);
        if (animations.size() < 2) {
            throw new IllegalArgumentException("animations.size < 2");
        }

    }

    @Override
    protected Animation getAnimation() throws VariableException {
        // force to the last animation if turning (regardless of turn anim)
        if (isTurning()) {
            return super.getAnimations().get(super.getAnimations().size() - 1);
        }
        return super.getAnimation();
    }

}

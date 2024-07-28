package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;
import java.util.ResourceBundle;

@Deprecated
public class BroadcastJump extends Jump {
    public BroadcastJump(ResourceBundle schema, List<Animation> animations, VariableMap context) {
        super(schema, animations, context);
    }
}

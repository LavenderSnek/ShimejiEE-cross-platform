package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;

@Deprecated
public class BroadcastStay extends Stay {
    public BroadcastStay(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params) {
        super(schema, animations, params);
    }
}

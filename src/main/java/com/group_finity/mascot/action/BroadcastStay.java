package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;

public class BroadcastStay extends Stay {

    private final Broadcast.Delegate broadcastDel = new Broadcast.Delegate(this);

    public BroadcastStay(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params) {
        super(schema, animations, params);
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();
        broadcastDel.updateAffordance();
    }

}

package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;

public class Broadcast extends Animate {

    static class Delegate {
        private final ActionBase base;

        public static final String PARAMETER_AFFORDANCE = "Affordance";
        private static final String DEFAULT_AFFORDANCE = "";

        Delegate(ActionBase base) {
            this.base = base;
        }

        void updateAffordance() throws VariableException {
            base.getMascot().getAffordances().add(getAffordance());
        }

        private String getAffordance() throws VariableException {
            return base.eval(base.getSchema().getString(PARAMETER_AFFORDANCE), String.class, DEFAULT_AFFORDANCE);
        }
    }


    private final Delegate del = new Delegate(this);

    public Broadcast(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();
        del.updateAffordance();
    }

}

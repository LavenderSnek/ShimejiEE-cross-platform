package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;

@SuppressWarnings("unused")
public class BreedMove extends Move {

    private final Breed.Delegate breedDel = new Breed.Delegate(this);

    public BreedMove(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public void init(final Mascot mascot) throws VariableException {
        super.init(mascot);

        if (breedDel.getBornInterval() < 1) {
            throw new VariableException("BornInterval must be greater than 0");
        }
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        super.tick();

        if (breedDel.isIntervalFrame() && breedDel.isAllowed()) {
            breedDel.breed();
        }
    }

}

package com.group_finity.mascot.generic;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;

import java.awt.Point;

class GenericEnvironment extends Environment {

    private static Area activeIE = new Area();

    @Override
    public void tick() {
        super.tick();
        activeIE.setVisible(false);
    }

    @Override
    public void moveActiveIE(final Point point) {}

    @Override
    public void restoreIE() {}

    @Override
    public Area getWorkArea() {
        return getScreen();
    }

    @Override
    public Area getActiveIE() {
        return activeIE;
    }

    @Override
    public void refreshCache() {}

}

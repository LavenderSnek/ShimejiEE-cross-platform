package com.group_finity.mascotnative.virtualdesktop;

import com.group_finity.mascot.environment.*;
import com.group_finity.mascotnative.virtualdesktop.display.VirtualEnvironmentDisplay;

import java.awt.*;
import java.util.List;

public class VirtualEnvironment extends BaseNativeEnvironment {

    private final VirtualEnvironmentDisplay display;

    public VirtualEnvironment(VirtualEnvironmentDisplay display) {
        this.display = display;
    }

    @Override
    protected void updateIe(Area ieToUpdate) {
        ieToUpdate.setVisible(false);
    }

    @Override
    protected List<Rectangle> getNewDisplayBoundsList() {
        return display.getDisplayBoundsList();
    }

    @Override
    public void tick() {
        super.tick();
        updateDisplayBounds();
    }

    @Override
    public String getActiveIETitle() {
        return null;
    }

    @Override
    public void moveActiveIE(Point point) {
    }

    @Override
    public void restoreIE() {
    }

    @Override
    protected Point getNewCursorLocation() {
        return display.getCursorLocation(super.getNewCursorLocation());
    }
}

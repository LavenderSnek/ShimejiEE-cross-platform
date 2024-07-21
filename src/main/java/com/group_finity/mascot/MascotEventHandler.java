package com.group_finity.mascot;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.window.contextmenu.MenuItemRep;
import com.group_finity.mascot.window.contextmenu.MenuRep;
import com.group_finity.mascot.window.contextmenu.TopLevelMenuRep;
import com.group_finity.mascot.window.TranslucentWindowEvent;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;

import java.util.ArrayList;
import java.util.List;

class MascotEventHandler implements TranslucentWindowEventHandler {

    private final Mascot mascot;

    public MascotEventHandler(Mascot mascot) {
        this.mascot = mascot;
    }

    @Override
    public void onDragBegin(TranslucentWindowEvent event) {
        if (mascot.getBehavior() != null) {
            try {
                mascot.getBehavior().mousePressed(event);
            } catch (final CantBeAliveException e) {
                mascot.dispose();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onDragEnd(TranslucentWindowEvent event) {
        if (mascot.getBehavior() != null) {
            try {
                mascot.getBehavior().mouseReleased(event);
            } catch (final CantBeAliveException e) {
                mascot.dispose();
                throw new RuntimeException(e);
            }
        }
    }

    // todo: this isnt really an event handling thing. maybe replace w something like onContextMenuRequest
    @Override
    public TopLevelMenuRep getContextMenuRep() {
        return null;
    }

}

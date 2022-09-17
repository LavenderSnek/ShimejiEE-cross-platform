package com.group_finity.mascot.window;

import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;

public interface TranslucentWindowEventHandler {

    TranslucentWindowEventHandler DEFAULT = new TranslucentWindowEventHandler() {
        @Override public void onDragBegin(TranslucentWindowEvent event) {}
        @Override public void onDragEnd(TranslucentWindowEvent event) {}
        @Override
        public TopLevelMenuRep getContextMenuRep() {
            return null;
        }
    };

    void onDragBegin(TranslucentWindowEvent event);

    void onDragEnd(TranslucentWindowEvent event);

    TopLevelMenuRep getContextMenuRep();

}

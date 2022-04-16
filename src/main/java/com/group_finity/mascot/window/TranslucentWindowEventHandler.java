package com.group_finity.mascot.window;

import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;

public interface TranslucentWindowEventHandler {

    TranslucentWindowEventHandler DEFAULT = new TranslucentWindowEventHandler() {
        @Override public void onDragBegin() {}
        @Override public void onDragEnd() {}
        @Override
        public TopLevelMenuRep getContextMenuRep() {
            return null;
        }
    };

    void onDragBegin();

    void onDragEnd();

    TopLevelMenuRep getContextMenuRep();

}

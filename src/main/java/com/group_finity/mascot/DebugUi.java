package com.group_finity.mascot;

public interface DebugUi {

    void update(Mascot mascot);

    default void setAfterDisposeAction(Runnable action) {}
    default void setVisible(boolean visible) {}
    default void dispose() {}

}

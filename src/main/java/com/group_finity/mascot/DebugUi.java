package com.group_finity.mascot;

public interface DebugUi {

    void update(Mascot mascot);

    void setAfterDisposeAction(Runnable action);

    void setVisible(boolean visible);

    void dispose();

}

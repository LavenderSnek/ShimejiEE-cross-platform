package com.group_finity.mascot.ui.debug;

import com.group_finity.mascot.environment.MascotEnvironment;

import java.awt.Point;

public interface DebugUi {

    void setBehaviorName(String behaviorName);

    void setMascotAnchor(Point anchor);

    void setMascotEnvironment(MascotEnvironment environment);

    void setVisible(boolean visible);

    void dispose();

}

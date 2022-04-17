package com.group_finity.mascot.environment;

import java.awt.Point;

public interface Border {

    /**
     * Whether the specified point is on the border.
     */
    boolean isOn(Point location);

    /**
     * Finds the new location for the input point based on the border's movement.
     * @param location the original location before movement.
     * @return The new location.
     */
    Point move(Point location);

}

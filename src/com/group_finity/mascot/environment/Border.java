package com.group_finity.mascot.environment;

import java.awt.Point;

public interface Border {

    boolean isOn(Point location);

    Point move(Point location);

}

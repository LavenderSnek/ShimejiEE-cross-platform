package com.group_finity.mascot.window;

import java.awt.Point;

public class TranslucentWindowEvent {

    private final Point relativeLocation;

    public TranslucentWindowEvent(Point relativeLocation) {
        this.relativeLocation = relativeLocation;
    }

    public Point getRelativeLocation() {
        return relativeLocation;
    }

}

package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;

import java.awt.Point;
import java.awt.Shape;

// part of public api; cant be a record

/**
 * Clickable area on a shimeji that triggers a specified behaviour.
 */
public class Hotspot {
    private final String behaviour;

    private final Shape shape;

    public Hotspot(String behaviour, Shape shape) {
        this.behaviour = behaviour;
        this.shape = shape;
    }

    public boolean contains(Mascot mascot, Point point) {
        // flip if facing right
        if (mascot.isLookRight()) {
            point = new Point(mascot.getBounds().width - point.x, point.y);
        }
        return shape.contains(point);
    }

    public String getBehaviour() {
        return behaviour;
    }

    public Shape getShape() {
        return shape;
    }

}

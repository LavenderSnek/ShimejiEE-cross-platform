package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;

import java.awt.Point;
import java.awt.Shape;

/**
 * Clickable area on a shimeji that triggers a specified behaviour.
 */

// part of public api; cant be a record
@SuppressWarnings("ClassCanBeARecord")
public class Hotspot {
    private final String behaviour;

    private final Shape shape;

    public Hotspot(String behaviour, Shape shape) {
        this.behaviour = behaviour;
        this.shape = shape;
    }

    /**
     * Whether the point is within this hotspot on the specified mascot.
     *
     * @param mascot The mascot object to check
     * @param point A point relative to the top right of the mascot bounds.
     */
    public boolean contains(Mascot mascot, Point point) {
        // flip if facing right
        if (mascot.isLookRight()) {
            point = new Point(mascot.getBounds().width - point.x, point.y);
        }
        return getShape().contains(point);
    }

    /**
     * The name of the behaviour played when this hotspot is activated.
     */
    public String getBehaviour() {
        return behaviour;
    }

    /**
     * The raw shape represented by this hotspot.
     * <p>
     * Do not use this directly, it doesn't account for the mascot being flipped.
     * For most cases, use {@link #contains(Mascot, Point)} instead.
     */
    public Shape getShape() {
        return shape;
    }

}

package com.group_finity.mascot.environment;

import java.awt.Point;

/**
 * Represents a moving point.
 */
public class Location {

    private int x;
    private int y;

    private int dx;
    private int dy;

    /**
     * Updates the point's location.
     * <p>
     * Call this twice with the same input to zero all the delta values.
     * Do not call this function from scripts.
     *
     * @hidden
     */
    public void set(final Point value) {
        setDx((getDx() + value.x - getX()) / 2);
        setDy((getDy() + value.y - getY()) / 2);

        setX(value.x);
        setY(value.y);
    }

    /**
     * The current X coordinate.
     */
    public int getX() {
        return this.x;
    }

    /**
     * The current Y coordinate
     */
    public int getY() {
        return this.y;
    }

    /**
     * The amount moved horizontally since the last tick.
     */
    public int getDx() {
        return this.dx;
    }

    /**
     * The amount moved vertically since the last tick.
     */
    public int getDy() {
        return this.dy;
    }

    //---internal setters
    /** @hidden */public void setX(final int x) {this.x = x;}
    /** @hidden */public void setY(final int y) {this.y = y;}
    /** @hidden */public void setDx(final int dx) {this.dx = dx;}
    /** @hidden */public void setDy(final int dy) {this.dy = dy;}

}

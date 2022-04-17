package com.group_finity.mascot.environment;

import java.awt.Rectangle;

/**
 * Represents a rectangle that moves and/or changes size.
 */
public class Area {

    private boolean visible = true;

    private int left;
    private int right;
    private int top;
    private int bottom;

    private int dleft;
    private int dright;
    private int dtop;
    private int dbottom;

    private final Wall leftBorder = new Wall(this, false);
    private final Wall rightBorder = new Wall(this, true);
    private final FloorCeiling topBorder = new FloorCeiling(this, false);
    private final FloorCeiling bottomBorder = new FloorCeiling(this, true);


    /**
     * Updates the area's location.
     * <p>
     * Call this twice with the same input to zero all the delta values.
     * Do not call this function from scripts.
     *
     * @hidden
     */
    public void set(final Rectangle value) {
        setDleft(value.x - getLeft());
        setDtop(value.y - getTop());
        setDright(value.x + value.width - getRight());
        setDbottom(value.y + value.height - getBottom());

        setLeft(value.x);
        setTop(value.y);
        setRight(value.x + value.width);
        setBottom(value.y + value.height);
    }

    /**
     * Creates a new rectangle representing the current state of the area
     */
    public Rectangle toRectangle() {
        return new Rectangle(left, top, right - left, bottom - top);
    }

    /**
     * Whether the specified point is in the current area. (includes borders)
     */
    public boolean contains(final int x, final int y) {
        return (getLeft() <= x) && (x <= getRight()) && (getTop() <= y) && (y <= getBottom());
    }

    /**
     * The current width of the rectangle
     */
    public int getWidth() {
        return getRight() - getLeft();
    }

    /**
     * The current height of the rectangle
     */
    public int getHeight() {
        return getBottom() - getTop();
    }

    /**
     * Whether the area is currently valid.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * The current X coordinate of the rectangle's left side
     */
    public int getLeft() {
        return this.left;
    }

    /**
     * The current X coordinate of the rectangle's right side
     */
    public int getRight() {
        return this.right;
    }

    /**
     * The current Y coordinate of the rectangle's top side.
     */
    public int getTop() {
        return this.top;
    }

    /**
     * The current Y coordinate of the rectangle's bottom side.
     */
    public int getBottom() {
        return this.bottom;
    }


    //---deltas

    /**
     * The amount the left side has moved since the previous tick
     */
    public int getDleft() {
        return this.dleft;
    }

    /**
     * The amount the right side has moved since the previous tick
     */
    public int getDright() {
        return this.dright;
    }

    /**
     * The amount the top side has moved since the previous tick
     */
    public int getDtop() {
        return this.dtop;
    }

    /**
     * The amount the bottom side has moved since the previous tick
     */
    public int getDbottom() {
        return this.dbottom;
    }

    //---borders

    public Wall getLeftBorder() {
        return this.leftBorder;
    }

    public Wall getRightBorder() {
        return this.rightBorder;
    }

    public FloorCeiling getTopBorder() {
        return this.topBorder;
    }

    public FloorCeiling getBottomBorder() {
        return this.bottomBorder;
    }

    //---internal setters
    /** @hidden */public void setVisible(final boolean visible) {this.visible = visible;}
    /** @hidden */public void setLeft(final int left) {this.left = left;}
    /** @hidden */public void setRight(final int right) {this.right = right;}
    /** @hidden */public void setTop(final int top) {this.top = top;}
    /** @hidden */public void setBottom(final int bottom) {this.bottom = bottom;}
    /** @hidden */public void setDleft(final int dleft) {this.dleft = dleft;}
    /** @hidden */public void setDright(final int dright) {this.dright = dright;}
    /** @hidden */public void setDtop(final int dtop) {this.dtop = dtop;}
    /** @hidden */public void setDbottom(final int dbottom) {this.dbottom = dbottom;}


    @Override
    public String toString() {
        return "Area [left=" + left + ", top=" + top + ", right=" + right + ", bottom=" + bottom + "]";
    }

}

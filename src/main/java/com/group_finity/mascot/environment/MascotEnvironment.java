package com.group_finity.mascot.environment;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.NativeFactory;

import java.awt.*;

public class MascotEnvironment {

    private final NativeEnvironment impl = NativeFactory.getInstance().getEnvironment();

    private final Mascot mascot;

    public MascotEnvironment(Mascot mascot) {
        this.mascot = mascot;
    }

    //---desktop

    /**
     * The ares of the work area at the mascot's anchor
     * <p>
     * If two areas are found, the first one found is returned.
     * The delta values of these areas are not reliable and should always be treated as 0.
     * <p>
     * An invisible area means that the mascot is currently not in any of the areas.
     */
    public Area getWorkArea() {
        return getWorkArea(false);
    }

    /**
     * @hidden
     */
    public Area getWorkArea(boolean ignoreSettings) {
        return impl.getWorkAreaAt(mascot.getAnchor());
    }

    /**
     * The combined bounds of all displays
     */
    public Area getScreen() {
        return impl.getScreen();
    }

    /**
     * @hidden
     */
    @SuppressWarnings("unused")
    public ComplexArea getComplexScreen() {
        return impl.getComplexScreen();
    }

    //---borders

    /**
     * The ceiling that the mascot is currently on.
     * <p>
     * A ceiling can either be the top of a work area or the bottom of an IE.
     * This method takes multiple displays into account and reports no border if the
     * found border overlaps a different display's border.
     * <p>
     * {@link NotOnBorder#INSTANCE} is returned if the mascot is not on any ceiling surfaces
     */
    public Border getCeiling() {
        return getCeiling(false);
    }

    /**
     * @hidden
     */
    public Border getCeiling(boolean ignoreSeparator) {
        if (getActiveIE().getBottomBorder().isOn(mascot.getAnchor())) {
            return getActiveIE().getBottomBorder();
        }

        Area workArea = getWorkArea();
        if (workArea.getTopBorder().isOn(mascot.getAnchor())) {
            if (isScreenTopBottom()) {
                return workArea.getTopBorder();
            }
        }
        return NotOnBorder.INSTANCE;
    }

    /**
     * The floor that the mascot is currently on.
     * <p>
     * A floor can either be the bottom of a work area or the top of an IE.
     * This method takes multiple displays into account and reports no border if the
     * found border overlaps a different display's border.
     * <p>
     * {@link NotOnBorder#INSTANCE} is returned if the mascot is not on any ceiling surfaces
     */
    public Border getFloor() {
        return getFloor(false);
    }

    /**
     * @hidden
     */
    public Border getFloor(boolean ignoreSeparator) {
        if (getActiveIE().getTopBorder().isOn(mascot.getAnchor())) {
            return getActiveIE().getTopBorder();
        }

        Area workArea = getWorkArea();
        if (workArea.getBottomBorder().isOn(mascot.getAnchor())) {
            if (isScreenTopBottom()) {
                return workArea.getBottomBorder();
            }
        }
        return NotOnBorder.INSTANCE;
    }

    /**
     * The side wall that the mascot is currently on.
     * <p>
     * A wall can be either side of a work area or IE.
     * This method takes multiple displays into account and reports no border if the
     * found border overlaps a different display's border.
     * <p>
     * {@link NotOnBorder#INSTANCE} is returned if the mascot is not on any ceiling surfaces
     */
    public Border getWall() {
        return getWall(false);
    }

    /**
     * @hidden
     */
    public Border getWall(boolean ignoreSeparator) {
        Border ieWall = mascot.isLookRight() ? getActiveIE().getLeftBorder() : getActiveIE().getRightBorder();
        if (ieWall.isOn(mascot.getAnchor())) {
            return ieWall;
        }

        Area workArea = getWorkArea();
        Border screenWall = mascot.isLookRight() ? workArea.getRightBorder() : workArea.getLeftBorder();

        if (screenWall.isOn(mascot.getAnchor())) {
            if (isScreenLeftRight()) {
                return screenWall;
            }
        }

        return NotOnBorder.INSTANCE;
    }

    //---IE

    /**
     * The bounds of the current IE (interactive environment)
     * <p>
     * The IE is generally the "frontmost window" but this can be platform dependent.
     * If there is no IE the {@link Area#isVisible() isVisible()} method of the returned Area will return false.
     */
    public Area getActiveIE() {
        return impl.getActiveIE();
    }

    /**
     * Name of the current IE
     * <p>
     * This value may be null even if there is a valid IE.
     */
    public String getActiveIETitle() {
        return impl.getActiveIETitle();
    }

    /**
     * Tries to set the top left corner of the current IE to specified location.
     * <p>
     * If the point is an invalid location, the IE is simply not moved.
     * @param point The destination in screen coordinates.
     */
    public void moveActiveIE(Point point) {
        impl.moveActiveIE(point);
    }

    //---cursor

    /**
     * A location object representing the cursor's location
     */
    public Location getCursor() {
        return impl.getCursor();
    }

    //---unused
    // TODO: add proper logging

    /**
     * Doesn't do anything here. Left for script compatibility.
     *
     * @hidden
     */
    @Deprecated
    public void restoreIE() {}

    /**
     * Doesn't do anything. Left for script compatibility.
     *
     * @hidden
     */
    @Deprecated
    public void refreshWorkArea() {}

    //---private

    private boolean isScreenTopBottom() {
        return impl.isScreenTopBottom(mascot.getAnchor());
    }

    private boolean isScreenLeftRight() {
        return impl.isScreenLeftRight(mascot.getAnchor());
    }

}

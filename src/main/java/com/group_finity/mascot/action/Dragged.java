package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.environment.Location;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.util.List;
import java.util.logging.Logger;

public class Dragged extends ActionBase {

    private static final Logger log = Logger.getLogger(Dragged.class.getName());

    public static final String VARIABLE_FOOTX = "FootX";
    private double footX;

    private static final String VARIABLE_FOOTDX = "FootDX";
    private double footDx;

    public static final String PARAMETER_OFFSETX = "OffsetX";
    private static final int DEFAULT_OFFSETX = 0;

    public static final String PARAMETER_OFFSETY = "OffsetY";
    private static final int DEFAULT_OFFSETY = 120;

    private int timeToRegist;

    public Dragged(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public void init(final Mascot mascot) throws VariableException {
        super.init(mascot);

        setFootX(getEnvironment().getCursor().getX() + getOffsetX());
        setTimeToRegist(250);

    }

    @Override
    public boolean hasNext() throws VariableException {

        final boolean intime = this.getTime() < this.getTimeToRegist();
        final boolean lukewarm = Math.random() >= 0.1;

        return super.hasNext() && (intime || lukewarm);
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        getMascot().setLookRight(false);
        getMascot().setDragging(true);

        final Location cursor = getEnvironment().getCursor();

        if (Math.abs(cursor.getX() - getMascot().getAnchor().x + getOffsetX()) >= 5) {
            this.setTime(0);
        }

        final int newX = cursor.getX();

        setFootDx((getFootDx() + ((newX - getFootX()) * 0.1)) * 0.8);
        setFootX(getFootX() + getFootDx());

        putVariable(getSchema().getString(VARIABLE_FOOTDX), getFootDx());
        putVariable(getSchema().getString(VARIABLE_FOOTX), getFootX());

        getAnimation().next(getMascot(), getTime());

        getMascot().setAnchor(new Point(
                cursor.getX() + getOffsetX(),
                cursor.getY() + getOffsetY()
        ));
    }

    @Override
    protected void refreshHotspots() {
        // hotspots unsupported for action
        getMascot().getHotspots().clear();
    }

    private void setFootX(final double footX) {
        this.footX = footX;
    }

    private double getFootX() {
        return this.footX;
    }

    private void setFootDx(final double footDx) {
        this.footDx = footDx;
    }

    private double getFootDx() {
        return this.footDx;
    }

    public void setTimeToRegist(final int timeToRegist) {
        this.timeToRegist = timeToRegist;
    }

    private int getTimeToRegist() {
        return this.timeToRegist;
    }

    private int getOffsetX() throws VariableException {
        int res = eval(getSchema().getString(PARAMETER_OFFSETX), Number.class, DEFAULT_OFFSETX).intValue();
        return (int) Math.round(res * getMascot().getScaling());
    }

    private int getOffsetY() throws VariableException {
        int res = eval(getSchema().getString(PARAMETER_OFFSETY), Number.class, DEFAULT_OFFSETY).intValue();
        return (int) Math.round(res * getMascot().getScaling());
    }

}

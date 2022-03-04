package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
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
    private double footDx;

    public static final String PARAMETER_OFFSETX = "OffsetX";
    private static final int DEFAULT_OFFSETX = 0;

    public static final String PARAMETER_OFFSETY = "OffsetY";
    private static final int DEFAULT_OFFSETY = 120;

    private int timeToRegist;

    private int scaling;

    public Dragged(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public void init(final Mascot mascot) throws VariableException {
        super.init(mascot);

        scaling = Main.getInstance().getScaling();

        setFootX(getEnvironment().getCursor().getX());
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

        final Location cursor = getEnvironment().getCursor();

        if (Math.abs(cursor.getX() - getMascot().getAnchor().x) >= 5) {
            this.setTime(0);
        }

        final int newX = cursor.getX();

        setFootDx((getFootDx() + ((newX - getFootX()) * 0.1)) * 0.8);
        setFootX(getFootX() + getFootDx());

        putVariable(getSchema().getString(VARIABLE_FOOTX), getFootX());

        getAnimation().next(getMascot(), getTime());

        getMascot().setAnchor(new Point(
                cursor.getX() + getOffsetX() * scaling,
                cursor.getY() + getOffsetY() * scaling
        ));
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
        return eval(getSchema().getString(PARAMETER_OFFSETX), Number.class, DEFAULT_OFFSETX).intValue();
    }

    private int getOffsetY() throws VariableException {
        return eval(getSchema().getString(PARAMETER_OFFSETY), Number.class, DEFAULT_OFFSETY).intValue();
    }

}

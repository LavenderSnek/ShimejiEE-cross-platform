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

	private int timeToRegist;

    private int scaling;

    public Dragged(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public void init(final Mascot mascot) throws VariableException {
        super.init(mascot);

        scaling = Integer.parseInt(Main.getInstance().getProperties().getProperty("Scaling", "1"));

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
        getEnvironment().refreshWorkArea();

        final Location cursor = getEnvironment().getCursor();

        if (Math.abs(cursor.getX() - getMascot().getAnchor().x) >= 5) {
            this.setTime(0);
        }

        final int newX = cursor.getX();

        this.setFootDx((this.getFootDx() + ((newX - this.getFootX()) * 0.1)) * 0.8);
        this.setFootX(this.getFootX() + this.getFootDx());

        putVariable(getSchema().getString(VARIABLE_FOOTX), this.getFootX());

        getAnimation().next(getMascot(), getTime());

        getMascot().getAnchor().setLocation(cursor.getX(), cursor.getY() + 120 * scaling);
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

}

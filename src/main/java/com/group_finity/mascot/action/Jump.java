package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.util.List;
import java.util.logging.Logger;

public class Jump extends ActionBase {

    private static final Logger log = Logger.getLogger(Jump.class.getName());

    public static final String PARAMETER_TARGETX = "TargetX";
    private static final int DEFAULT_PARAMETERX = 0;

    public static final String PARAMETER_TARGETY = "TargetY";
    private static final int DEFAULT_PARAMETERY = 0;

    //An Action Attribute is already named Velocity
    public static final String PARAMETER_VELOCITY = "VelocityParam";
    private static final double DEFAULT_VELOCITY = 20.0;

    public Jump(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }

    @Override
    public boolean hasNext() throws VariableException {
        final int targetX = getTargetX();
        final int targetY = getTargetY();

        final double distanceX = targetX - getMascot().getAnchor().x;
        final double distanceY = targetY - getMascot().getAnchor().y - Math.abs(distanceX) / 2;

        final double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        return super.hasNext() && (distance != 0);
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        final int targetX = getTargetX();
        final int targetY = getTargetY();

        getMascot().setLookRight(getMascot().getAnchor().x < targetX);

        final double distanceX = targetX - getMascot().getAnchor().x;
        final double distanceY = targetY - getMascot().getAnchor().y - Math.abs(distanceX) / 2;

        final double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        final double velocity = getVelocity();

        if (distance != 0) {
            final int velocityX = (int) (velocity * distanceX / distance);
            final int velocityY = (int) (velocity * distanceY / distance);

            getMascot().getAnchor().translate(velocityX, velocityY);

            getAnimation().next(getMascot(), getTime());
        }

        if (distance <= velocity) {
            getMascot().setAnchor(new Point(targetX, targetY));
        }
    }

    private int getTargetX() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TARGETX), Number.class, DEFAULT_PARAMETERX).intValue();
    }

    private int getTargetY() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TARGETY), Number.class, DEFAULT_PARAMETERY).intValue();
    }

	private double getVelocity() throws VariableException {
		return eval(getSchema().getString(PARAMETER_VELOCITY), Number.class, DEFAULT_VELOCITY).doubleValue();
	}

}

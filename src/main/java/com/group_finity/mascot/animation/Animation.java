package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

public class Animation {

    private final Variable condition;

    private final Pose[] poses;

    private final Hotspot[] hotspots;

    public Animation(Variable condition, Pose[] poses, Hotspot[] hotspots) {
        if (poses.length == 0) {
            throw new IllegalArgumentException("poses.length == 0");
        }

        this.condition = condition;
        this.poses = poses;
        this.hotspots = hotspots;
    }

    public boolean isEffective(final VariableMap variables) throws VariableException {
        return (Boolean) getCondition().get(variables);
    }

    public void init() {
        getCondition().init();
    }

    public void initFrame() {
        getCondition().initFrame();
    }

    public void next(final Mascot mascot, final int time) {
        getPoseAt(time).next(mascot);
    }

    public Pose getPoseAt(int time) {
        time %= getDuration();

        for (final Pose pose : getPoses()) {
            time -= pose.duration();
            if (time < 0) {
                return pose;
            }
        }

        return null;
    }

    public int getDuration() {
        int duration = 0;
        for (final Pose pose : getPoses()) {
            duration += pose.duration();
        }

        return duration;
    }

    private Variable getCondition() {
        return this.condition;
    }

    private Pose[] getPoses() {
        return this.poses;
    }

    public Hotspot[] getHotspots() {
        return hotspots;
    }

}

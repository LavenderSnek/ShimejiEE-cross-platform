package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

import java.util.Collection;

public class Animation {

    private final Variable condition;

    private final Collection<Pose> poses;

    private final Collection<Hotspot> hotspots;

    private final boolean isTurn;

    public Animation(Variable condition, Collection<Pose> poses, Collection<Hotspot> hotspots, boolean isTurn) {
        if (poses.isEmpty()) {
            throw new IllegalArgumentException("Pose list is empty");
        }

        this.condition = condition;
        this.poses = poses;
        this.hotspots = hotspots;
        this.isTurn = isTurn;
    }

    public boolean isEffective(final VariableMap variables) throws VariableException {
        return (Boolean) getConditionVar().get(variables);
    }

    public void init() {
        getConditionVar().init();
    }

    public void initFrame() {
        getConditionVar().initFrame();
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

    private Variable getConditionVar() {
        return this.condition;
    }

    private Collection<Pose> getPoses() {
        return this.poses;
    }

    public Collection<Hotspot> getHotspots() {
        return hotspots;
    }

    public boolean isTurn() {
        return isTurn;
    }
}

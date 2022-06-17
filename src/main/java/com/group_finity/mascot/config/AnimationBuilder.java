package com.group_finity.mascot.config;

import com.group_finity.mascot.Tr;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AnimationBuilder {

    private final String condition;

    private final List<Pose> poses = new ArrayList<>();

    public AnimationBuilder(ResourceBundle schema, Entry animationNode, PoseLoader poseLoader) throws IOException {
        this.condition = animationNode.getAttribute(schema.getString("Condition")) == null
                ? "true"
                : animationNode.getAttribute(schema.getString("Condition"));

        for (Entry poseNode : animationNode.selectChildren(schema.getString("Pose"))) {
            Pose pose;
            try {
                pose = poseLoader.loadPose(schema, poseNode);
            } catch (Exception e) {
                throw new IOException(e.getMessage() + "\nUnable to load pose" + poseNode.getAttributes(), e);
            }
            getPoses().add(pose);
        }
    }

    public Animation buildAnimation() throws AnimationInstantiationException {
        try {
            return new Animation(Variable.parse(this.getCondition()), this.getPoses().toArray(new Pose[0]));
        } catch (final VariableException e) {
            throw new AnimationInstantiationException(Tr.tr("FailedConditionEvaluationErrorMessage"), e);
        }
    }

    private String getCondition() {
        return this.condition;
    }

    private List<Pose> getPoses() {
        return this.poses;
    }

}

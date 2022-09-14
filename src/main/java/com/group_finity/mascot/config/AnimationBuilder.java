package com.group_finity.mascot.config;

import com.group_finity.mascot.Tr;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Hotspot;
import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AnimationBuilder {

    private final String condition;

    private final List<Pose> poses = new ArrayList<>();

    private final List<Hotspot> hotspots = new ArrayList<>();

    public AnimationBuilder(ResourceBundle schema, Entry animationNode, PoseLoader poseLoader) throws IOException {
        this.condition = animationNode.getAttribute(schema.getString("Condition")) == null
                ? "true"
                : animationNode.getAttribute(schema.getString("Condition"));

        for (Entry poseNode : animationNode.selectChildren(schema.getString("Pose"))) {
            Pose pose;
            try {
                pose = poseLoader.loadPose(schema, poseNode);
            } catch (Exception e) {
                throw new IOException(e.getMessage() + "\nUnable to load pose: " + poseNode.getAttributes(), e);
            }
            getPoses().add(pose);
        }

        for (Entry hotspotNode : animationNode.selectChildren(schema.getString("Hotspot"))) {
            Hotspot hotspot;
            try {
                hotspot = loadHotspot(schema, poseLoader.getScaling(), hotspotNode);
            } catch (Exception e) {
                throw new IOException(e.getMessage() + "\nUnable to load hotspot: " + hotspotNode.getAttributes(), e);
            }
            getHotspots().add(hotspot);
        }
    }

    public Animation buildAnimation() throws AnimationInstantiationException {
        try {
            return new Animation(Variable.parse(getCondition()), getPoses().toArray(new Pose[0]), getHotspots().toArray(new Hotspot[0]));
        } catch (final VariableException e) {
            throw new AnimationInstantiationException(Tr.tr("FailedConditionEvaluationErrorMessage"), e);
        }
    }

    private String getCondition() {
        return condition;
    }

    private List<Pose> getPoses() {
        return poses;
    }

    private List<Hotspot> getHotspots() {
        return hotspots;
    }

    private static Hotspot loadHotspot(ResourceBundle schema, double scaling, Entry hotspotNode) throws IOException {
        final String shapeText = hotspotNode.getAttribute(schema.getString("Shape"));

        final String originText = hotspotNode.getAttribute(schema.getString("Origin"));
        final String sizeText = hotspotNode.getAttribute(schema.getString("Size"));

        final String behaviourText = hotspotNode.getAttribute(schema.getString("Behaviour"));

        String[] originCoords = originText.split(",");
        double scaledOx = Integer.parseInt(originCoords[0]) * scaling;
        double scaledOy = Integer.parseInt(originCoords[1]) * scaling;

        String[] sizeMeasures = sizeText.split(",");
        double scaledW = Integer.parseInt(sizeMeasures[0]) * scaling;
        double scaledH = Integer.parseInt(sizeMeasures[1]) * scaling;

        Shape shape;
        if (shapeText.equalsIgnoreCase("Rectangle")) {
            shape = new Rectangle((int) Math.round(scaledOx), (int) Math.round(scaledOy), (int) Math.round(scaledW), (int) Math.round(scaledH));
        } else if (shapeText.equalsIgnoreCase("Ellipse")) {
            shape = new Ellipse2D.Float((float) scaledOx, (float) scaledOy, (float) scaledW, (float) scaledH);
        } else {
            throw new IOException(Tr.tr( "HotspotShapeNotSupportedErrorMessage" ) + ": Shape=" + shapeText );
        }

        return new Hotspot(behaviourText, shape);
    }

}

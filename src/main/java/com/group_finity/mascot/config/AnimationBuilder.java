package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;

import com.group_finity.mascot.Tr;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.sound.SoundLoader;

public class AnimationBuilder {

    private static final Logger log = Logger.getLogger(AnimationBuilder.class.getName());

    private final ResourceBundle schema;

    private String imageSet = "";

    private final String condition;

    private final List<Pose> poses = new ArrayList<>();

    public AnimationBuilder(final ResourceBundle schema, final Entry animationNode, final String imageSet) throws IOException {
        if (!imageSet.isEmpty()) {
            this.imageSet = imageSet;
        }

        this.schema = schema;
        this.condition = animationNode.getAttribute(schema.getString("Condition")) == null ? "true" : animationNode.getAttribute(schema.getString("Condition"));

        log.log(Level.INFO, "Start Reading Animations");

        for (final Entry frameNode : animationNode.getChildren()) {
            this.getPoses().add(loadPose(frameNode));
        }

        log.log(Level.INFO, "Animations Finished Loading");
    }

    //todo: rewrite this
    private Pose loadPose(final Entry frameNode) throws IOException {

        final String imageText = frameNode.getAttribute(schema.getString("Image"));
        final String imageRightText = frameNode.getAttribute(schema.getString("ImageRight"));

        //very much not optional
        final String anchorText = frameNode.getAttribute(schema.getString("ImageAnchor"));
        final String moveText = frameNode.getAttribute(schema.getString("Velocity"));
        final String durationText = frameNode.getAttribute(schema.getString("Duration"));

        //optional
        final String soundText = frameNode.getAttribute(schema.getString("Sound"));
        final String volumeText = frameNode.getAttribute(schema.getString("Volume")) != null ? frameNode.getAttribute(schema.getString("Volume")) : "0";
        final double scaling = Main.getInstance().getScaling();

        String imagePairIdentifier = null;
        if (imageText != null) { // if you don't have anchor text defined as well you're going to have a bad time

            final String[] anchorCoordinates = anchorText.split(",");
            final Point anchor = new Point(Integer.parseInt(anchorCoordinates[0]), Integer.parseInt(anchorCoordinates[1]));

            try {
                imagePairIdentifier = ImagePairLoader.load(imageSet, imageText, imageRightText, anchor, scaling);
            } catch (Exception e) {
                String error;
                if (imageRightText == null) {
                    error = " [Image: "+imageText+"]";
                }else {
                    error = " [Image: "+imageText+", ImageRight: "+imageRightText+"]";
                }
                log.log(Level.SEVERE, "Failed to load image: " + error);
                throw new IOException(Tr.tr("FailedLoadImageErrorMessage") + " " + error);
            }
        }

        final String[] moveCoordinates = moveText.split(",");
        int dx = Integer.parseInt(moveCoordinates[0]);
        int dy = Integer.parseInt(moveCoordinates[1]);
        int scaledDx = (int) Math.round(dx * scaling);
        int scaledDy = (int) Math.round(dy * scaling);

        scaledDx = dx != 0 && scaledDx == 0 ? (dx < 0 ? -1 : 1) : scaledDx; // prevents them from getting stuck
        scaledDy = dy != 0 && scaledDy == 0 ? (dy < 0 ? -1 : 1) : scaledDy;

        final int duration = Integer.parseInt(durationText);

        String soundIdentifier = null;
        if (soundText != null) {
            try {
                final float volume = Float.parseFloat(volumeText);
                soundIdentifier = SoundLoader.load(imageSet, soundText, volume);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to load sound: " + soundText);
                throw new IOException(Tr.tr("FailedLoadSoundErrorMessage") + soundText);
            }
        }

        final Pose pose = new Pose(imagePairIdentifier, scaledDx, scaledDy, duration, soundIdentifier);

        log.log(Level.INFO, "ReadPosition({0})", pose);

        return pose;
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

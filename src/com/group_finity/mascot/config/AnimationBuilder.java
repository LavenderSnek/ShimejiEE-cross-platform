package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.sound.SoundLoader;


public class AnimationBuilder {

    private static final Logger log = Logger.getLogger(AnimationBuilder.class.getName());
    private final String condition;
    private String imageSet = "";
    private final List<Pose> poses = new ArrayList<Pose>();
    private final ResourceBundle schema;

    public AnimationBuilder(final ResourceBundle schema, final Entry animationNode, final String imageSet) throws IOException {
        if (!imageSet.equals("")) {
            this.imageSet = "/" + imageSet;
        }

        this.schema = schema;
        this.condition = animationNode.getAttribute(schema.getString("Condition")) == null ? "true" : animationNode.getAttribute(schema.getString("Condition"));

        log.log(Level.INFO, "Start Reading Animations");

        for (final Entry frameNode : animationNode.getChildren()) {
            this.getPoses().add(loadPose(frameNode));
        }

        log.log(Level.INFO, "Animations Finished Loading");
    }

    private Pose loadPose(final Entry frameNode) throws IOException {

        final String imageText = frameNode.getAttribute(schema.getString("Image")) != null ? imageSet + frameNode.getAttribute(schema.getString("Image")) : null;
        final String imageRightText = frameNode.getAttribute(schema.getString("ImageRight")) != null ? imageSet + frameNode.getAttribute(schema.getString("ImageRight")) : null;
        final String anchorText = frameNode.getAttribute(schema.getString("ImageAnchor")) != null ? frameNode.getAttribute(schema.getString("ImageAnchor")) : null;
        final String moveText = frameNode.getAttribute(schema.getString("Velocity"));
        final String durationText = frameNode.getAttribute(schema.getString("Duration"));
        String soundText = frameNode.getAttribute(schema.getString("Sound")) != null ? frameNode.getAttribute(schema.getString("Sound")) : null;
        final String volumeText = frameNode.getAttribute(schema.getString("Volume")) != null ? frameNode.getAttribute(schema.getString("Volume")) : "0";

        final int scaling = Integer.parseInt(Main.getInstance().getProperties().getProperty("Scaling", "1"));

        if (imageText != null) { // if you don't have anchor text defined as well you're going to have a bad time

            final String[] anchorCoordinates = anchorText.split(",");
            final Point anchor = new Point(Integer.parseInt(anchorCoordinates[0]), Integer.parseInt(anchorCoordinates[1]));

            try {
                ImagePairLoader.load(imageText, imageRightText, anchor, scaling);
            } catch (Exception e) {
                String error = imageText;
                if (imageRightText != null)
                    error += ", " + imageRightText;
                log.log(Level.SEVERE, "Failed to load image: " + error);
                throw new IOException(Main.getInstance().getLanguageBundle().getString("FailedLoadImageErrorMessage") + " " + error);
            }
        }

        final String[] moveCoordinates = moveText.split(",");
        final Point move = new Point(Integer.parseInt(moveCoordinates[0]) * scaling, Integer.parseInt(moveCoordinates[1]) * scaling);

        final int duration = Integer.parseInt(durationText);

        if (soundText != null) {
            try {
                if (new File("./sound" + soundText).exists()) {
                    soundText = "./sound" + soundText;
                } else if (new File("./sound" + imageSet + soundText).exists()) {
                    soundText = "./sound" + imageSet + soundText;
                } else {
                    soundText = "./img" + imageSet + "/sound" + soundText;
                }

                SoundLoader.load(soundText, Float.parseFloat(volumeText));
                soundText += Float.parseFloat(volumeText);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to load sound: " + soundText);
                throw new IOException(Main.getInstance().getLanguageBundle().getString("FailedLoadSoundErrorMessage") + soundText);
            }
        }

        final Pose pose = new Pose(imageText, imageRightText, move.x, move.y, duration, soundText != null ? soundText : null);

        log.log(Level.INFO, "ReadPosition({0})", pose);

        return pose;
    }

    public Animation buildAnimation() throws AnimationInstantiationException {
        try {
            return new Animation(Variable.parse(this.getCondition()), this.getPoses().toArray(new Pose[0]));
        } catch (final VariableException e) {
            throw new AnimationInstantiationException(Main.getInstance().getLanguageBundle().getString("FailedConditionEvaluationErrorMessage"), e);
        }
    }

    private List<Pose> getPoses() {
        return this.poses;
    }

    private String getCondition() {
        return this.condition;
    }
}

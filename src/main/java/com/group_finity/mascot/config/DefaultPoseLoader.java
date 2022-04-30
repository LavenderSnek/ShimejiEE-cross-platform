package com.group_finity.mascot.config;

import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.image.ImagePairStore;
import com.group_finity.mascot.sound.SoundStore;

import java.awt.Point;
import java.io.IOException;
import java.util.ResourceBundle;

public class DefaultPoseLoader implements PoseLoader {

    private final ImagePairStore imagePairStore;
    private final SoundStore soundStore;

    public DefaultPoseLoader(ImagePairStore imagePairStore, SoundStore soundStore) {
        this.imagePairStore = imagePairStore;
        this.soundStore = soundStore;
    }

    @Override
    public Pose loadPose(ResourceBundle schema, Entry poseNode) throws IOException {

        final String imageText = poseNode.getAttribute(schema.getString("Image"));
        final String imageRightText = poseNode.getAttribute(schema.getString("ImageRight"));
        final String anchorText = poseNode.getAttribute(schema.getString("ImageAnchor"));

        final String velocityText = poseNode.getAttribute(schema.getString("Velocity"));
        final String durationText = poseNode.getAttribute(schema.getString("Duration"));

        final String soundText = poseNode.getAttribute(schema.getString("Sound"));
        final String volumeText = poseNode.getAttribute(schema.getString("Volume")) == null ? "0" : poseNode.getAttribute(schema.getString("Volume"));


        String imageKey = null;
        if (imageText != null) {
            String[] anchorCoords = anchorText.split(",");
            Point anchor = new Point(Integer.parseInt(anchorCoords[0]), Integer.parseInt(anchorCoords[1]));
            imageKey = getImagePairStore().load(imageText, imageRightText, anchor);
        }

        final double scaling = getImagePairStore().getScaling();

        String[] velocityCoords = velocityText.split(",");
        int dx = Integer.parseInt(velocityCoords[0]);
        int dy = Integer.parseInt(velocityCoords[1]);

        int scaledDx = (int) Math.round(dx * scaling);
        int scaledDy = (int) Math.round(dy * scaling);
        scaledDx = dx != 0 && scaledDx == 0 ? (dx < 0 ? -1 : 1) : scaledDx; // prevents them from getting stuck
        scaledDy = dy != 0 && scaledDy == 0 ? (dy < 0 ? -1 : 1) : scaledDy;

        int duration = Integer.parseInt(durationText);

        String soundKey = null;
        if (soundText != null) {
            float volume = Float.parseFloat(volumeText);
            try {
                soundKey = getSoundStore().load(soundText, volume);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        return new Pose(imageKey, scaledDx, scaledDy, duration, soundKey);
    }

    private ImagePairStore getImagePairStore() {
        return imagePairStore;
    }

    private SoundStore getSoundStore() {
        return soundStore;
    }

}

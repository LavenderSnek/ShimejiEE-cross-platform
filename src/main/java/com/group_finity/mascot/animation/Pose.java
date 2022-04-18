package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.image.ImagePairs;

import java.awt.Point;

public record Pose(
        String imageKey,
        int dx,
        int dy,
        int duration,
        String soundKey
) {

    public void next(final Mascot mascot) {
        mascot.setAnchor(new Point(
                mascot.getAnchor().x + (mascot.isLookRight() ? -dx() : dx()),
                mascot.getAnchor().y + dy()
        ));

        mascot.setImage(ImagePairs.getImage(imageKey(), mascot.isLookRight()));
        mascot.setSound(soundKey());
    }

}
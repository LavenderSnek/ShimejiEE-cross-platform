package com.group_finity.mascot.animation;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.image.MascotImage;

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

        // ignore if key is null (happens when pose has no image)
        MascotImage img = imageKey() == null ? null :
                Main.getInstance()
                .getImageSet(mascot.getImageSet())
                .getImagePairs().get(imageKey())
                .getImage(mascot.isLookRight());

        mascot.setImage(img);
        mascot.setSound(soundKey());
    }

}
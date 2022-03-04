package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.image.ImagePair;
import com.group_finity.mascot.image.ImagePairs;

import java.awt.Point;

public class Pose {

    private final String imageName;

    private final int dx;
    private final int dy;

    private final int duration;
    private final String sound;

    public Pose(final String imageName, final int dx, final int dy, final int duration, final String sound) {
        this.imageName = imageName;
        this.dx = dx;
        this.dy = dy;
        this.duration = duration;
        this.sound = sound;
    }

    @Override
    public String toString() {
        return "Pose (" + (getImage() == null ? "" : getImage()) + ","
                + getDx() + ","
                + getDy() + ","
                + getDuration() + ", "
                + sound + ")";
    }

    public void next(final Mascot mascot) {
        mascot.setAnchor(new Point(
                mascot.getAnchor().x + (mascot.isLookRight() ? -getDx() : getDx()),
                mascot.getAnchor().y + getDy()
        ));

        mascot.setImage(ImagePairs.getImage(getImageName(), mascot.isLookRight()));
        mascot.setSound(getSoundName());
    }

    public ImagePair getImage() {
        return ImagePairs.getImagePair(this.getImageName());
    }

    public String getImageName() {
        return imageName;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDuration() {
        return duration;
    }

    public String getSoundName() {
        return sound;
    }

}
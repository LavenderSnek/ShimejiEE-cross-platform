package com.group_finity.mascot.animation;

import java.awt.Point;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.image.ImagePair;
import com.group_finity.mascot.image.ImagePairs;


public class Pose {
    private final String image;
    private final String rightImage;
    private final int dx;
    private final int dy;
    private final int duration;
    private final String sound;

    public Pose(final String image) {
        this(image, "", 0, 0, 1);
    }

    public Pose(final String image, final int duration) {
        this(image, "", 0, 0, duration);
    }

    public Pose(final String image, final int dx, final int dy, final int duration) {
        this(image, "", dx, dy, duration);
    }

    public Pose(final String image, final String rightImage) {
        this(image, rightImage, 0, 0, 1);
    }

    public Pose(final String image, final String rightImage, final int duration) {
        this(image, rightImage, 0, 0, duration);
    }

    public Pose(final String image, final String rightImage, final int dx, final int dy, final int duration) {
        this(image, rightImage, dx, dy, duration, null);
    }

    public Pose(final String image, final String rightImage, final int dx, final int dy, final int duration, final String sound) {
        this.image = image;
        this.rightImage = rightImage;
        this.dx = dx;
        this.dy = dy;
        this.duration = duration;
        this.sound = sound;
    }

    @Override
    public String toString() {
        return "Pose (" + (getImage() == null ? "" : getImage()) + "," + getDx() + "," + getDy() + "," + getDuration() + ", " + sound + ")";
    }

    public void next(final Mascot mascot) {
        mascot.setAnchor(new Point(mascot.getAnchor().x + (mascot.isLookRight() ? -getDx() : getDx()),
                mascot.getAnchor().y + getDy()));
        mascot.setImage(ImagePairs.getImage(getImageName(), mascot.isLookRight()));
        mascot.setSound(getSoundName());
    }

    public int getDuration() {
        return duration;
    }

    public String getImageName() {
        return (image == null ? "" : image) + (rightImage == null ? "" : rightImage);
    }

    public ImagePair getImage() {
        return ImagePairs.getImagePair(this.getImageName());
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public String getSoundName() {
        return sound;
    }
}
package com.group_finity.mascotapp.prefs;

import com.group_finity.mascot.MascotPrefProvider;

public class MutablePrefs implements MascotPrefProvider, Cloneable {

    public double Scaling = 1;
    public boolean LogicalAnchors = false;
    public boolean AsymmetryNameScheme = false;
    public boolean PixelArtScaling = false;
    public boolean FixRelativeGlobalSound = false;

    public boolean Breeding = true;
    public boolean Transients = true;
    public boolean Transformation = true;
    public boolean Throwing = true;
    public boolean Sounds = true;

    public boolean TranslateBehaviorNames = true;
    public boolean AlwaysShowShimejiChooser = true;
    public boolean IgnoreImagesetProperties = true;

    @Override public boolean isIEMovementAllowed() { return Throwing; }
    @Override public boolean isBreedingAllowed() { return Breeding; }
    @Override public boolean isTransientBreedingAllowed() { return Transients; }
    @Override public boolean isTransformationAllowed() { return Transformation; }
    @Override public boolean isSoundAllowed() { return Sounds; }

    @Override
    public MutablePrefs clone() {
        try {
            return (MutablePrefs) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

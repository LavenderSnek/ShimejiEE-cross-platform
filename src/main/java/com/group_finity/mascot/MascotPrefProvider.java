package com.group_finity.mascot;

public interface MascotPrefProvider {

    MascotPrefProvider DEFAULT = new MascotPrefProvider() {};

    // maybe this should just be an enum and the mascot can get a couple of mapping functions
    // but that loses the flexibility of non bool settings (though that might be a good thing)

    default boolean isIEMovementAllowed() { return true; }
    default boolean isBreedingAllowed() { return true; }
    default boolean isTransientBreedingAllowed() { return true; }
    default boolean isTransformationAllowed() { return true; }
    default boolean isSoundAllowed() { return true; }
    default boolean shouldTranslateBehaviours() { return true; }

    // option to make these all imageset specific in the future
    default boolean isIEMovementAllowed(String imageSet) { return isIEMovementAllowed(); }
    default boolean isBreedingAllowed(String imageSet) { return isBreedingAllowed(); }
    default boolean isTransientBreedingAllowed(String imageSet) { return isTransientBreedingAllowed(); }
    default boolean isTransformationAllowed(String imageSet) { return isTransformationAllowed(); }
    default boolean isSoundAllowed(String imageSet) { return isSoundAllowed(); }
    default boolean shouldTranslateBehaviours(String imageSet) { return shouldTranslateBehaviours(); }
}

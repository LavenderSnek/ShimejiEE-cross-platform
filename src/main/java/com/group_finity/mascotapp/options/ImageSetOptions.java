package com.group_finity.mascotapp.options;


import java.util.Map;

import static picocli.CommandLine.Option;
import static picocli.CommandLine.Help;

// these should all either be image set specific or global
// (at-least theoretically; the toggles still need lots of refactoring, gonna go write some atrocious lambdas later)
public class ImageSetOptions {

    // img set loading
    @Option(names = {"--scale"},  description = "Scaling applied, if any")
    public double scaling = 1.0;

    @Option(names = {"--asym"}, negatable = true, description = "Compatibility for `<...>-r.png` asymmetry name scheme.")
    public boolean asymmetryNameScheme = false;

    @Option(names = {"--lanc"}, negatable = true, description = "Should multiple anchors per image pair be allowed. Changes behaviour where only the first anchor of each image pair is loaded.")
    public boolean logicalAnchors = false;

    @Option(names = {"--pix"}, negatable = true, description = "Should nearest neighbour scaling be used")
    public boolean pixelArtScaling = false;

    @Option(names = {"--soundfix"}, negatable = true, description = "Should fixing relative sound paths be attempted")
    public boolean fixRelativeGlobalSound = false;

    @Option(names = {"--rip"}, negatable = true, description = "Should imageset.properties be read")
    public boolean readImageSetProps = true;

    // behaviour (suffering refactoring any of these to be image set specific)

    @Option(names = {"--throw"}, negatable = true, description = "Is window throwing allowed")
    public boolean windowThrowingAllowed = true;

    @Option(names = {"--breed"}, negatable = true, description = "Is breeding/cloning allowed")
    public boolean breedingAllowed = true;

    @Option(names = {"--transient"}, negatable = true, description = "Is `Transient` breeding/cloning allowed")
    public boolean transientsAllowed = true;

    @Option(names = {"--transform"}, negatable = true, description = "Is switching image sets allowed")
    public boolean transformationAllowed = true;

    // changed this bc it conflicts w the pf sound
    @Option(names = {"--audio"}, negatable = true, description = "Is audio allowed to play")
    public boolean audioAllowed = true;

}
package com.group_finity.mascotapp.options;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static picocli.CommandLine.Option;

public class PersistentAppOptions {

    @Option(names = {"-s", "--select"}, description = "Add active image set")
    public List<String> imageSetSelections = new ArrayList<>();

    @Option(names = {"-l", "--locale"}, description = "Locale for the program")
    public String locale = "en";

    @Option(names = {"--bvt"}, negatable = true, description = "Should behaviour names be translated")
    public boolean translateBehaviourNames = true;

    @Option(names = {"--chooser"}, negatable = true, description = "Is image set chooser shown on start")
    public boolean showChooserOnStart = true;

    @Option(names = {"--iw", "--ie"}, description = "Title matcher for interactive windows")
    public Pattern windowPattern = Pattern.compile(".*");

}

package com.group_finity.mascot.imageset;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.DefaultPoseLoader;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.image.ImagePairLoaderBuilder;
import com.group_finity.mascot.image.ImagePairStore;
import com.group_finity.mascot.sound.SoundLoader;
import com.group_finity.mascot.sound.SoundStore;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ShimejiImageSet implements ImageSet {

    private final Configuration configuration;
    private final ImagePairLoader imagePairs;
    private final SoundLoader sounds;

    ShimejiImageSet(Configuration loadedConfig, ImagePairLoader imagePairs, SoundLoader sounds) {
        this.configuration = loadedConfig;
        this.imagePairs = imagePairs;
        this.sounds = sounds;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public ImagePairStore getImagePairs() {
        return imagePairs;
    }

    @Override
    public SoundStore getSounds() {
        return sounds;
    }

    public static ShimejiImageSet loadFrom(ShimejiProgramFolder pf, String name, Map<String, String> settings) throws ParserConfigurationException, IOException, SAXException, ConfigurationException {
        ImagePairLoaderBuilder ib = new ImagePairLoaderBuilder();
        String scaleVal = settings.getOrDefault("Scaling", ib.getScaling() + "");
        try {
            double scale = Double.parseDouble(scaleVal);
            ib.setScaling(scale > 0 ? scale : ib.getScaling());
        } catch (Exception ignored) {}

        ib.setLogicalAnchors(Boolean.parseBoolean(settings.getOrDefault("LogicalAnchors", ib.isLogicalAnchors() + "")))
                .setAsymmetryNameScheme(Boolean.parseBoolean(settings.getOrDefault("AsymmetryNameScheme", ib.isAsymmetryNameScheme() + "")))
                .setPixelArtScaling(Boolean.parseBoolean(settings.getOrDefault("PixelArtScaling", ib.isPixelArtScaling() + "")));

        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Path actionsPath = pf.getActionConfPath(name);
        Path behaviorPath = pf.getBehaviorConfPath(name);
        Entry actionsEntry = new Entry(docBuilder.parse(actionsPath.toFile()).getDocumentElement());
        Entry behaviorEntry = new Entry(docBuilder.parse(behaviorPath.toFile()).getDocumentElement());

        ImagePairLoader imgLoader = ib.buildForBasePath(pf.imgPath().resolve(name));

        SoundLoader soundLoader = new SoundLoader(pf, name);
        soundLoader.setFixRelativeGlobalSound(Boolean.parseBoolean(settings.getOrDefault("FixRelativeGlobalSound", soundLoader.isFixRelativeGlobalSound() + "")));

        Configuration config = new Configuration();
        config.load(new DefaultPoseLoader(imgLoader, soundLoader), actionsEntry, behaviorEntry);
        config.validate();

        return new ShimejiImageSet(config, imgLoader, soundLoader);
    }

}

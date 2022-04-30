package com.group_finity.mascot.imageset;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.DefaultPoseLoader;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePair;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.image.ImagePairStore;
import com.group_finity.mascot.image.ImagePairs;
import com.group_finity.mascot.sound.SoundLoader;
import com.group_finity.mascot.sound.SoundStore;
import com.group_finity.mascot.sound.Sounds;
import org.xml.sax.SAXException;

import javax.sound.sampled.Clip;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Point;
import java.io.IOException;
import java.util.List;

public class ShimejiImageSet implements ImageSet {

    private Configuration configuration;
    private ImagePairStore imagePairs;
    private SoundStore sounds;

    ShimejiImageSet(Configuration configuration, ImagePairStore imagePairs, SoundStore sounds) {
        this.configuration = configuration;
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

    public static ShimejiImageSet loadFrom(ShimejiProgramFolder pf, String name) throws IOException, ParserConfigurationException, SAXException, ConfigurationException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        var actionsPath = pf.getActionConfPath(name);
        var behaviorPath = pf.getBehaviorConfPath(name);

        var actionEntry = new Entry(docBuilder.parse(actionsPath.toFile()).getDocumentElement());
        var behaviorEntry = new Entry(docBuilder.parse(behaviorPath.toFile()).getDocumentElement());

        // temporary
        var imgLoader = new ImagePairStore() {
            final double scaling = Main.getInstance().getScaling();

            @Override
            public String load(String imageText, String imageRightText, Point anchor) throws IOException {
                return ImagePairLoader.load(name, imageText, imageRightText, anchor, scaling);
            }

            @Override
            public ImagePair get(String key) {
                return ImagePairs.getImagePair(key);
            }

            @Override
            public double getScaling() {
                return scaling;
            }
        };


        var soundLoader = new SoundStore() {
            @Override
            public String load(String soundText, float volume) throws Exception {
                return SoundLoader.load(name, soundText, volume);
            }

            @Override
            public Clip get(String key) {
                return Sounds.getSound(key);
            }

            @Override
            public List<Clip> getIgnoringVolume(String name) {
                return Sounds.getSoundsIgnoringVolume(name, name);
            }
        };


        var pl = new DefaultPoseLoader(imgLoader, soundLoader);

        var cfg = new Configuration();
        cfg.load(actionEntry, pl);
        cfg.load(behaviorEntry, pl);

        cfg.validate();

        return new ShimejiImageSet(cfg, imgLoader, soundLoader);
    }

}

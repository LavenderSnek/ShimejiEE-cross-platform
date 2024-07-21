package com.group_finity.mascot.image;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascotapp.Constants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImagePairLoaderTest {

    private static final String IMG_NAME = "/red-blue.png";
    private static final String IMG_RIGHT_NAME = "/red-blue-r.png"; // half the height of left image

    @TempDir
    static Path BASE;

    @BeforeAll
    static void setUpDir() throws IOException {
        NativeFactory.init("generic", Constants.NATIVE_LIB_DIR);
        Files.copy(Path.of("src/test/resources", IMG_NAME), Path.of(BASE.toString(), IMG_NAME));
        Files.copy(Path.of("src/test/resources", IMG_RIGHT_NAME), Path.of(BASE.toString(), IMG_RIGHT_NAME));
    }

    @Test
    void asymmetrySchemes() throws IOException {
        {
            var ipl = new ImagePairLoaderBuilder().buildForBasePath(BASE);

            String leftOnly = ipl.load(IMG_NAME, null, new Point());
            boolean flippedSameSize = ipl.get(leftOnly).getImage(false).getSize().equals(ipl.get(leftOnly).getImage(true).getSize());

            assertTrue(flippedSameSize);

            String leftRight = ipl.load(IMG_NAME, IMG_RIGHT_NAME, new Point());
            int lh = ipl.get(leftRight).getImage(false).getSize().height;
            int rh = ipl.get(leftRight).getImage(true).getSize().height;

            assertNotEquals(lh, rh);
        }

        var asymmetryIpl = new ImagePairLoaderBuilder().setAsymmetryNameScheme(true).buildForBasePath(BASE);
        String foundRight = asymmetryIpl.load(IMG_NAME, null, new Point());
        int flh = asymmetryIpl.get(foundRight).getImage(false).getSize().height;
        int frh = asymmetryIpl.get(foundRight).getImage(true).getSize().height;

        assertNotEquals(flh, frh);
    }

    @Test
    void logicalAnchorLoading() throws IOException {
        var ipl = new ImagePairLoaderBuilder().setLogicalAnchors(true).buildForBasePath(BASE);

        String k1 = ipl.load(IMG_NAME, null, new Point(8, 16));
        String k2 = ipl.load(IMG_NAME, null, new Point(0, 0));

        assertNotEquals(k1, k2);
        assertNotEquals(ipl.get(k1).getImage(false).getCenter(), ipl.get(k2).getImage(false).getCenter());
    }

    @Test
    void defaultMergedAnchorLoading() throws IOException {
        var ipl = new ImagePairLoaderBuilder().buildForBasePath(BASE);

        String k1 = ipl.load(IMG_NAME, null, new Point(8, 16));
        String k2 = ipl.load(IMG_NAME, null, new Point(0, 0));

        assertEquals(ipl.get(k1).getImage(false).getCenter(), ipl.get(k2).getImage(false).getCenter());
    }

    @Test
    void missingLeadingSlash() {
        var ipl = new ImagePairLoaderBuilder().buildForBasePath(BASE);
        try {
            String slashesRemoved = IMG_NAME.replaceAll("^/+", "");
            ipl.load(slashesRemoved, null, new Point(8, 16));
            fail("Did not throw exception for missing leading slashes: " + slashesRemoved);
        } catch (IOException ignored) {
        }
    }

}
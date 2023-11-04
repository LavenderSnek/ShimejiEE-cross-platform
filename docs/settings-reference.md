
Settings Reference
==================

User Prefs
----------

When setting these as system properties from the command line, prefix them with `com.group_finity.mascot.prefs.`.
For example, to set the `FixRelativeGlobalSound` property to false from the command line, run shimeji like this:

```shell
java -Dcom.group_finity.mascot.prefs.FixRelativeGlobalSound=true -jar ShimejiEE.jar
```

For more info on the available options, look at the `shimejicli.py` file in docs.

### ImageSet specific

All of these are (except `Scaling`) are not written back to any of the settings files. Reloading mascots is necessary 
for these changes to take effect.

To use these options without CLI, you can make a file called `imageset.properties` in the image set's isolated conf folder
(i.e `<image set name>/conf/imageset.properties`).
If these options are specified through the command line, they are only treated as defaults and the contents of
the `imageset.properties` file still take precedence unless `IgnoreImagesetProperties` is enabled.

| Name                     | Description                                                                                | Default |
|--------------------------|--------------------------------------------------------------------------------------------|---------|
| `Scaling`                | Sets the scale factor applies to the images and anchors. Needs to be > 0, Can be a decimal | `1`     |
| `LogicalAnchors`         | Whether different anchors for the same image pairs are loaded.                             | `false` |
| `AsymmetryNameScheme`    | Uses the `-r` suffix to find a separate ImageRight (if no ImageRight is provided).         | `false` |
| `PixelArtScaling`        | Enable this to use nearest neighbour interpolation for scaling.                            | `false` |
| `FixRelativeGlobalSound` | Whether to automatically fix missing sound files when they begin with `/../../sound/`      | `false` |


Settings Reference
==================

User Prefs
----------

When setting these as system properties from the command line, prefix them with `com.group_finity.mascot.prefs.`.
For example, to set the `Sounds` property to false from the command line, run shimeji like this:

```shell
java -Dcom.group_finity.mascot.prefs.Sounds=false -jar ShimejiEE.jar
```

Properties set from the command line take priority over those read from the settings file.

### Toggles

| Name                       | Description                                                            | Default |
|----------------------------|------------------------------------------------------------------------|---------|
| `Breeding`                 | Allow shimeji to spawn other shimeji                                   | `true`  |
| `Transients`               | Allow shimeji to spawn other if the new shimeji is marked as transient | `true`  |
| `Transformation`           | Allow shimeji to transform between image sets                          | `true`  |
| `Throwing`                 | Allow shimeji to move/throw windows                                    | `true`  |
| `Sounds`                   | Allow shimeji to play sound                                            | `true`  |
| `AlwaysShowShimejiChooser` | Show the image set chooser on startup regardless of selections         | `false` |
| `TranslateBehaviorNames`   | Translate behaviour names to more readable forms in menus              | `false` |
| `IgnoreImagesetProperties` | Ignore any settings in `imageset.properties` files.                    | `false` |

### ImageSet specific

All of these are (except `Scaling`) are not written back to any of the settings files. Reloading mascots is necessary 
for these changes to take effect.

To use these options, make a file called `imageset.properties` in the image set's isolated conf folder
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

### Misc.

| Name                 | Description                                                                                                                                                                                                                                                                                                 |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Language`           | The language tag for the UI locale. Default value is english (`en`).                                                                                                                                                                                                                                        |
| `ActiveShimeji`      | A slash separated + url encoded list of selected image sets from the current img folder. For example the value `ImageSet1/abcd/lorem%2Fipsum/`  would select `ImageSet1`, `abcd`, and `lorem/ipsum`. If an image set can't be loaded for any reason,  it is discarded from the list of selected image sets. |



### Program Folder

These properties are command line only. They are not written back to settings on exit. The parent folder of the jar file 
must still contain the `lib` and `conf` folders regardless of what these values are set to.

| Name                  | Description                                                                                                                                                                                             |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Name                  | Description                                                                                                                                                                                             |
| `ProgramFolder`       | Path to the base program folder. This is used to resolve values that haven't been set more specifically. The default value is the jar file's parent directory.                                          |
| `ProgramFolder.conf`  | Path to the global conf folder. This does not affect settings or logging, only the default conf files and user behavior translations.                                                                   |
| `ProgramFolder.img`   | Path to the img folder. Needs to be a valid img folder.                                                                                                                                                 |
| `ProgramFolder.sound` | Path to global sound folder.                                                                                                                                                                            |
| `ProgramFolder.mono`  | (boolean) Whether the img folder directly contains the images. This was common in older versions of shimeji. It is recommended to set this value explicitly as the program's guess isn't 100% accurate. |
| `SettingsPath`        | Input and output path for the settings file. Defaults to `<jar parent dir>/conf/settings.properties` regardless of the program folder settings.                                                         |



Other 
-----

To pick the native implementation set the `com.group_finity.mascotnative` system property. These are the current options:

| Property     | Notes                                                          | Compatibility  |
|--------------|----------------------------------------------------------------|----------------|
| `generic`    | A pure swing implementation with no environment interaction.   | All (Sort of)  |
| `macclassic` | Based on generic. Adds mac environment interactions using JNA. | macOS          |
| `macjni`     | JNI mac implementation, adds proper click through. WIP         | macOS          |
| `win`        | Windows implementation using JNA                               | Windows        |

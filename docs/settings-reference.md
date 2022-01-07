
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

### Booleans

| Name                       | Description                                                     | Default |
|----------------------------|-----------------------------------------------------------------|---------|
| `Breeding`                 | Allow shimeji to spawn other shimeji                            | `true`  |
| `Transformation`           | Allow shimeji to transform between image sets                   | `true`  |
| `Throwing`                 | Allow shimeji to move/throw windows                             | `true`  |
| `Sounds`                   | Allow shimeji to play sound                                     | `true`  |
| `Multiscreen`              | Whether shimeji can move between screens                        | `true`  |
| `AlwaysShowShimejiChooser` | Show the image set chooser on startup regardless of selections  | `false` |
| `TranslateBehaviorNames`   | Translate behaviour names to more readable forms in menus       | `false` |

### Misc.

| Name                 | Description                                                                                                                                                                                                                                                                               |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Language`           | The language tag for the UI locale. Default value is english (`en`).                                                                                                                                                                                                                      |
| `ActiveShimeji`      | A slash separated list of selected image sets from the current img folder. For example the value `ImageSet1/abcd/loremipsum/`  would select `ImageSet1`, `abcd`, and `loremipsum`. If an image set can't be loaded for any reason,  it is discarded from the list of selected image sets. |
| `InteractiveWindows` | This value is system dependent                                                                                                                                                                                                                                                            |

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

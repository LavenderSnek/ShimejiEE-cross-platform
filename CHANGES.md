# Snek Changelog

## 1.1.0
Image set and file finding fixes

- Re-adds original options for config file locations 
- Fixes issue where the image sets stay in memory even after it is no longer active
- Mascots do not need to be reloaded after using chooser or changing language
- Adds setting key `ShowChooserAtStart`
- Adds menu item to refresh all mascots
 
## 1.0.3
Launcher script & UI

- Bundled customized jre and added bash script to launch the app with the correct jre
  - App name is now customizable through this launch script
  - Works even if the shimeji folder is moved
  - won't work if `ShimejiEE.jar` is renamed
- Added back window interaction menu button to tray icon, but only for windows
- Tested on Windows 10 with openjdk 11
- Tray icon language refreshes immediately after language change again


## 1.0.2
Complete macOS dragging fix

- Dragging now uses the regular code without flickering
  - This means the mouse can now change position to the anchor without breaking the drag
  - Changed `"apple.awt.draggableWindowBackground"` to false in `MacTranslucentWindow`
- Fixed a bug I made on the behaviour toggle part of the tray icon
- found ceiling interaction workaround

## 1.0.1
UI adjustments

- Context menu now works
- Sets dock icon to `img/dock-icon.png`
- Menu-bar title shows up as "Shimeji"
- Replaced Dragging with code from [https://github.com/nonowarn/shimeji4mac](https://github.com/nonowarn/shimeji4mac)
  - Still broken, but it is better
- Removed NimrodLaF
- Cleaned up tray icon code to run on mac
  - Split everything into smaller functions 
  - uses AWT because it feels more native + has access to tray icon

## 1.0.0
Initial macOS functionality + cleanup

- Runs _okay_ on macOS
  - made `GenericEnvironment.activeIE` static
- Fewer options for where config files can be
  - They can only be in:
    - `conf/{file}/`
    - `img/{imageSet}/conf/`
  - Done for cleanup
  - Might add the options back later as a separate function in Main, but I like the idea of a consistent setup
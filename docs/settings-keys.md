# Recognized keys of settings.properties

incomplete

## Working

### Language

Locale code that determines the UI language

### ActiveShimeji

A slash separated list of imageSets that are selected by default when the program loads. For example:
```properties
ActiveShimeji=Dog/Abcdef;l/Shimeji
```
This would select the image sets named `Dog`, `Abcdef;l`, `Shimeji` and load them at startup.

### Breeding

`true` Default | Shimeji can multiply

`false` Stops Shimeji from multiplying, any breeding action will simply play without producing another mascot

### Throwing

`true` Default | Shimeji are allowed to throw windows

`false` Shimeji are not allowed to throw windows

### ShowChooserAtStart

`true` The image set chooser is shown on startup.

`false` Default | Options will first be read from settings, shows chooser if no valid options are found.

## Unknown

I haven't tested/used these yet.
### Transformation
...
### Multiscreen
...
### Sounds
...

## Windows Only

These don't work on macOS for now

### Filter

This only gets used if you use a scaling setting above 1

`true` Turns on fancy filtering for scaling images

`false` Default | Turns off fancy filtering for scaling images

### InteractiveWindows

A slash separated list of window names that the shimeji can interact with.

### Scaling

The amount of scaling. Allowed values are: `1`,`2`,`2`,`3`,`6`,`8`.

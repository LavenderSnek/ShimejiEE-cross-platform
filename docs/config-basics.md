# Basic Configuration Guide

Some of these are specific to this version of shimeji.

## How It Works

### Each Shimeji is configured through:

- An image set: **img/.../**
- An actions file: **actions.xml** that can be named (High to low priority):
  - `actions.xml`
  - `action.xml`
  - `動作.xml`
  - `one.xml`
  - `1.xml`
- A behaviors file: **behaviors.xml** that can be named (High to low priority):
  - `behaviors.xml`
  - `behavior.xml`
  - `行動.xml`
  - `two.xml`
  - `2.xml`
  
I'll refer to the config files as actions.xml and behaviors.xml but you can use any of the alternative names.

#### Image Set (img/\[imageSetName]/)
This is a folder located directly in `img/`. The image set must contain all image files specified in the corresponding actions file.

There is also a tool, Image Set Chooser, that will let you select active  image sets at runtime. It remembers previous options via the `settings.properties` file. You'll have to use the `⌘` or `shift` if you want select multiple.

The Image Set Chooser looks for the `img/[imageSetName]/icon.png`, then `img/[imageSetName]/shime1.png` image. If neither is found, no image set preview will be shown. So it's recommended that you have those files.

#### actions.xml
Possible locations (High to low priority):
- `img/[imageSetName]/conf/actions.xml`
- `conf/[imageSetName]/actions.xml`
- `conf/actions.xml`

Specifies the different actions Shimeji can do.  When listing images, only include the file name. This is where you define the animation related things such as frames, speed, size etc.

The following actions must be present for the actions.xml to work nicely:
- ChaseMouse
- Fall
- Dragged
- Thrown
- Anything mentioned in behaviors.xml

#### behaviors.xml
Possible locations (High to low priority):
- `img/[imageSetName]/conf/behaviors.xml`
- `conf/[imageSetName]/behaviors.xml`
- `conf/behaviors.xml`

Specifies the frequency and situation in which each action is performed.

The following behaviors must be present for the behaviors.xml to work nicely:
- ChaseMouse
- Fall
- Dragged
- Thrown

### Other Files & Folders

#### img/unused/
Shimeji-ee will ignore all the image sets that are in the `img/unused` folder, so you can hide image sets in there. It will also ignore all folders in img/ that start with a dot.

#### img/dock-icon.png
Used as the dock icon on systems that support it

#### img/icon.png
The icon used for the system tray

#### conf/settings.properties
Details which Shimeji are active as well as the windows with which they can interact. These settings can generally be changed using the program itself.

#### conf/logging.properties
Defines how logging errors is done.

### Note
Editing an existing configuration is fairly straightforward. But writing a brand-new configuration file is very time-consuming and requires a lot of trial and error.


## Basic Configuration
If you want multiple Shimeji types, you must have multiple image sets. Basically, you put different folders with the correct Shimeji images under the `img` folder.

### Installing a shimeji:
If you can't open the compressed files then I would recommend downloading [keka](https://www.keka.io/en/) if you're on mac, or if you're on Windows then [7zip](https://www.7-zip.org/) .

You might have to modify these steps slightly, since there a very old versions of shimeji that could work a bit differently. This is just a rough guide.

#### Cleanup
Skip this step if you only have the image set folder. Rarely, the jar files might also be modded in which case, you'll have to figure it out on your own.

Basically you'll want to take the image set from the downloaded shimeji and transfer them to your installation. 

Then you'll have to preserve the configuration files. _Skip these if your folder of images already has a conf folder with a couple of xml files OR you're **sure** it uses the default config!!_

1. Make a folder called `conf` _inside_ the image set folder.

2. Move the corresponding actions and behavior files into it. The names of these files can be in different languages. They also might have been mangled by the decompression process, so check for that and rename them to a `behaviors.xml` and an `actions.xml` according to the content if needed.

#### Installation

Now you have a folder of: Shimeji images + (maybe) a conf folder.

Move this folder into the `img` folder of the final program where you want to install it. Done! You can then choose it in the image set chooser.

If it doesn't work, check the config stuff a few times.

### Creating a shimeji

If you just want to make a shimeji that follows default config, you just draw the images to match the default ones or look up a default template. Take these drawn images and name them appropriately, then make a shimeji images folder to put them in.

When sharing your image sets just compress only the image set folder and share that, there's no need to share the whole program.
# ShimejiEE Devnotes

## IDE Setup Stuff
These are instructions for IntelliJ.

1. Just open the folder and let it automatically do the setup. You might want to change the jdk version but that's about it. Just use the [regular jdk](https://adoptopenjdk.net/releases.html) here (versions 11 to 14 recommended).

2. Go to Project Structure > Artifacts > + > JAR > empty. Add compile output to the JAR. Then add the included MANIFEST.MF as the manifest. Set the output directory to the ShimejiEE folder.

3. Build the artifact, this should work.

4. Run the artifact, this will fail, but it will generate a run configuration to edit. Set the working directory to the ShimejiEE folder. **Add building the artifact to the before launch section!**

5. Run this configuration. This should work unless I've missed describing some important steps here. 
   
6. If the menu bar interaction is broken on macOS, that's normal. To fix it, add the custom jre as the jre in the run configuration. It's not included in the repo, get it from a download.

## Custom Java on macOS
Uses a customized version of java because it was the most painless way to get the window levels to work. There's probably a better way to do it using JNI, but I couldn't be bothered to figure it out. Compiling it yourself is optional; You can just download the Shimeji and take the jre from there.

### Editing & Compiling
No Special compilation needed, just use the JDK's instructions. Compile it without any modifications first to just make sure it compiles. If it still doesn't work after a bunch of troubleshooting, pick a different version and try again. Go with versions 11 to 14 unless you'll be setting up the javascript engine yourself. 

Edit [this line](https://github.com/openjdk/jdk/blob/8e312297d806f581c7a069af6c2ee2d8381b46b6/src/java.desktop/macosx/native/libawt_lwawt/awt/AWTWindow.m#L255).
Just change `NSFloatingWindowLevel` to whatever level you want it to stay on. A list of the standard options is [available here](https://developer.apple.com/documentation/appkit/nswindowlevel?language=objc). 

I set it to `NSStatusWindowLevel` since I like the effect of the mascot staying under the menus, but I've also tested `NSScreenSaverWindowLevel` (which stays above _everything_), and it works just fine.

### Converting to JRE
now go to `images/jdk/bin/` in your build directory from the previous step.

Build the JRE using this tool: [justinmahar.github.io/easyjre/](https://justinmahar.github.io/easyjre/), here's what I ended up with: 

```
./jlink --output jre-11.0.11-internal --compress=2 --no-header-files --no-man-pages --module-path ../jmods --add-modules java.base,java.compiler,java.datatransfer,java.desktop,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.se,java.xml,java.xml.crypto,jdk.scripting.nashorn,jdk.scripting.nashorn.shell
```

The only special consideration needed is to make sure that you include the Nashorn libraries since it's not included by default. Might try to minimize it a bit further, but it's small enough for now (about 53mb)

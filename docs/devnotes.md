# ShimejiEE Devnotes

## Building
Install maven + jdk 17, set `JAVA_HOME` to jdk 17, then run `mvn clean package` to build. The complete program folder shows up in `tagret/ShimejiEE/`.

## Custom Java on macOS
_This part isn't necessary to get the program to run_, it just fixes something that annoys me.

The mac releases use a customized version of java because it was the most painless way to get the window levels to work. There's probably a better way to do it using JNI, but I couldn't be bothered to figure it out.

### Editing & Compiling
No Special compilation needed, just use the JDK's instructions. Compile it without any modifications first to just make sure it compiles. If it still doesn't work after a bunch of troubleshooting, pick a different version and try again.

Edit [this line](https://github.com/openjdk/jdk/blob/8e312297d806f581c7a069af6c2ee2d8381b46b6/src/java.desktop/macosx/native/libawt_lwawt/awt/AWTWindow.m#L255).
Just change `NSFloatingWindowLevel` to whatever level you want it to stay on. A list of the standard options is [available here](https://developer.apple.com/documentation/appkit/nswindowlevel?language=objc). 


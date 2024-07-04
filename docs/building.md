
Building
========

This doc covers building through command line. 
IDE setup should be relatively straightforward, though I have had occasional issues with setting `JAVA_HOME` properly for cmake.

Common TLDR Guide
-----------------
Try this before wasting your time reading the rest of this docs.

- Make sure `JAVA_HOME` is JDK 17
- Run `mvn clean package` 
- Fix anything it complains about (i.e. missing tools)
- Try again

The final shimeji folder will be in `target/ShimejiEE`

To skip native build, disable the `cmake` profile in maven

macOS
-----

This guide uses [Homebrew](https://brew.sh/) to install dependencies.

### Install Dependencies

```shell
# java
brew install maven
brew install openjdk@17

# symlink java (see https://formulae.brew.sh/formula/openjdk@17)
sudo ln -sfn $HOMEBREW_PREFIX/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# native (if you'll be building the JNI libs)
brew install cmake
brew install ninja
```

### Setup Java

Make sure that `JAVA_HOME` is set to the correct version. 
If you symlinked java in the previous step here's a nice shortcut ([more info](https://stackoverflow.com/questions/21964709/)).

```shell
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Build

The following command cleans any build files and builds ShimejiEE along with the native libraries for macOS. 
```shell
mvn clean package
```

If you want to skip building native libs, use this command.
```shell
mvn clean package -P '!cmake'
```

### Run
```shell
java -jar target/ShimejiEE/ShimejiEE.jar
```

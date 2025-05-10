
Building
========

TLDR: run `build.py` using python3 and hope for the best.

## Install Deps

### Core:
- Python 3.13+ (might work with a lower version, but this is what I use)
- Maven
- JDK 23

### Native:
- CMake
- [Ninja Build](https://ninja-build.org/)
- [Jextract](https://jdk.java.net/jextract/)
  - You don't _need_ to add this to PATH. `build.py` has a `--jextract` argument you can use to specify the binary instead
- A C/C++ Compiler

## Run Build

- Make sure JAVA_HOME is set to the JDK 23 Install
- Run `build.py`
- use `--no-panama` if you just want to build java code

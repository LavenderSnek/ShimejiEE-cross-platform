# Shimeji-EE | Shimeji English Enhanced

My attempt at getting the Kilkakon version of Shimeji to work on macOS, and hopefully Linux at some point. See docs folder for instructions on stuff.

## Credits

This is all the info I could find about it, and it's definitely possible that I missed something.

### Kilkakon

The current developer of Shimeji-EE.

Homepage: [http://kilkakon.com/shimeji](http://kilkakon.com/shimeji)

Discord: [https://discord.gg/dcJGAn3](https://discord.gg/dcJGAn3)

### TigerHix universal

Added 64 bit support

Repository: [https://github.com/TigerHix/shimeji-universal](https://github.com/TigerHix/shimeji-universal)

### nonowarn macOS Port _(Abandoned)_

Wrote all the native macOS code.

Repository: [https://github.com/nonowarn/shimeji4mac](https://github.com/nonowarn/shimeji4mac)

Homepage: [Abandoned homepage](http://nonowarn.jp/shimeji/index-en.html),
[Snapshot from Dec 2013](https://web.archive.org/web/20131221091851/http://nonowarn.jp/shimeji/index-en.html)

### shimeji-ee Group _(Abandoned)_

Added the translation to english

Repository: [https://code.google.com/archive/p/shimeji-ee/source/default/commits](https://code.google.com/archive/p/shimeji-ee/source/default/commits)

### Original _(Abandoned)_

The furthest back I could find it existing was in ~June 2009, but it was labeled version 2.0, so I'm still not sure when this originated. I also can't find the source code for it.

Homepage: [Defunct; Snapshot from Sep 2016](https://web.archive.org/web/20160901003054/http://www.group-finity.com:80/Shimeji/)

Vector: The only thing I could still find online.
[https://www.vector.co.jp/soft/winnt/amuse/se476479.html](https://www.vector.co.jp/soft/winnt/amuse/se476479.html)

## Requirements

- macOS: tested on 11.1 Intel, but it'll probably work on other models too
    
- Windows 10: Works for now.
  - Needs java 11
  - Download the no jre version

- Linux
  - Technically "ran" on a VM, but it crashed the VM pretty quickly
    - I'm not sure how well it works on a real computer, so try it out I guess
  - Needs a bit of fiddling with jna path
  - You might need to install the jdk from a different place if you're missing `libawt_xawt.so`

## License

Programmers may feel free to use the source. The Shimeji-ee source is under the New BSD license.  
Follow the zlib/libpng licenses.

### Libraries

`jna.jar` and `examples.jar` from the [JNA library](https://github.com/java-native-access/jna)  
Follows [LGPL 2.1/Apache License 2.0](https://github.com/java-native-access/jna/blob/master/LICENSE)

`AbsoluteLayout.jar` from [Netbeans](https://github.com/apache/netbeans)  
Follows the Apache License 2.0
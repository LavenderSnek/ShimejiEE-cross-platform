## Notes For Developers

Getting started is pretty straightforward. The Shimeji project was originally created by Yuki Yamada of Group Finity (http://www.group-finity.com/Shimeji/), most of the major design decisions are followed from the original source. The coding style is also adopted from the original Shimeji code. The Shimeji-ee project branch involves translating the program to English and adding additional functionality.

To participate in development, please send an e-mail to "relic.nt+Shimeji@gmail.com" and say what you intend to work on.

This project is built using ant, so download ant (or use an IDE). Look at the top level build.xml for more details. The release versions on the Downloads page uses the Personal version as "Calm" and Ultimate version as "Mischevious."

If you're going to use an IDE, I'll assume you know how to set everything up yourself. Note that there are multiple top level source folders. The Main class is `com.group_finity.mascot.Main`.

This project uses java logging. The configuration file is located in the conf folder. It is named "logging.properties". Edit the contents as needed for development purposes, it's very straightforward and you can use google for more information. When running this command from the console (or an IDE), make sure to point to the configuration file by including `-Djava.util.logging.config.file=./conf/logging.properties` as an argument to the program.

At this point, this project is very memory intensive. It loads all the Shimeji images for all the different image sets for all the different actions at load time. What this means is that the JVM's maximum heap size needs to be increased or you can't run more than one image set at a time. When running this program from the console (or an IDE), make sure to increase the max heap size by providing an argument (such as `-Xmx256m` for 256 megabytes) to java. Use google for more information. Hopefully we can eventually improve the memory use situation. Right now, running Shimeji-ee from the executable allocates up to 60% of the system's memory to Shimeji.

Finally, the Shimeji-ee.exe is created using launch4j. The jar is not included in the executable, it merely launches it. The program icon is in the img folder. The only other special setting is that 60% of free memory is specified as the Max Heap Size under the JRE tab. Shimeji requires 32 bit Java 6 to run correctly.

Run Shimeji-ee from command line from the Shimeji-ee directory with a command like: 

```shell
"C:\Program Files (x86)\Java\jre6\bin\java" -classpath Shimeji-ee.jar -Xmx256m com.group_finity.mascot.Main -Djava.util.logging.config.file=./conf/logging.properties
```
See the readme for more details: [http://code.google.com/p/shimeji-ee/wiki/Readme](http://code.google.com/p/shimeji-ee/wiki/Readme)
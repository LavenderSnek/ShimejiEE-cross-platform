package com.group_finity.mascotapp;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.swing.*;

import static picocli.CommandLine.*;

@Command(name = "shimeji", version = "2.1.0", mixinStandardHelpOptions = true,
        description = "%nShimejiEE desktop pets%nrun jar with no args for default configuration%n"
)
public class ShimejiEE {

    public static void main(String[] args) {
        var ec = new CommandLine(new ShimejiEE())
                .addSubcommand("run", new ShimejiRun())
                .execute("run");
    }
}

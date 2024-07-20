package com.group_finity.mascotapp;

import com.group_finity.mascotapp.runners.MinimalRunner;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "shimeji", version = "2.1.0", mixinStandardHelpOptions = true,
        description = "%nShimejiEE desktop pets%nrun jar with no args for default configuration%n"
)
public class ShimejiEE {

    public static void main(String[] args) {
        var ec = new CommandLine(new ShimejiEE())
                .addSubcommand("run", new MinimalRunner())
                .execute("run");
    }
}

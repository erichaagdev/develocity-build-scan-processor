package dev.erichaag.develocity.cli;

import picocli.CommandLine;

import static picocli.CommandLine.Command;

@Command(
        name = "cli",
        mixinStandardHelpOptions = true,
        subcommands = {TopRequestedTasksReport.class}
)
class Main {

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}

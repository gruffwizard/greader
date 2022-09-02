package dev.gruffwizard.tools.github.cmd;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {MarketplaceCommand.class})

public class Root {
    @CommandLine.Option(names = { "-l", "--log" }, description = "enable logging")
    boolean log=false;
}

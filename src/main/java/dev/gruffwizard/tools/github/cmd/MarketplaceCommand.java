package dev.gruffwizard.tools.github.cmd;

import dev.gruffwizard.tools.github.readers.MarketplaceReader;
import org.jboss.logging.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

@CommandLine.Command(name = "marketplace", description = "Extract marketplace elements")
public class MarketplaceCommand implements Runnable {

    private static final Logger LOG = Logger.getLogger(MarketplaceCommand.class);

    @CommandLine.ParentCommand
    private Root parent;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = "-apps") boolean apps;
    @CommandLine.Option(names = "-actions") boolean actions;

    @Override
    public void run() {


        if(parent.log) {

        }
        if(!apps && !actions) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "Missing option: at least one of the " +
                            "'-apps' or '-actions' options must be specified.");
        }

        try {
            MarketplaceReader.runner()
                    .includeApps(apps)
                    .includeActions(actions)
                    .output(new File("marketplace.csv"))
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
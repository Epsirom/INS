package entry;

import org.apache.commons.cli.*;

public class Main {

    public static void main(String[] args) {
        Base.logger(Main.class).info("Started.");

        // Settings
        boolean skip_index;

        Options options = new Options();
        options.addOption("h", "help", false, "Show this message.");
        options.addOption(OptionBuilder.withLongOpt("indexing")
                .withDescription("Set the directory where the index is.")
                .hasArg()
                .withArgName("DIR")
                .create("i"));
        options.addOption("s", "skip-index", false, "Skip the indexing step.");
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                throw new Exception();
            }
            Config.index_path = cmd.getOptionValue("indexing", "indexing");
            skip_index = Boolean.parseBoolean(cmd.getOptionValue("skip-index", "false"));
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("INS", options);
        }
    }
}

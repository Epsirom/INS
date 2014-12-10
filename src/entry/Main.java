package entry;

import indexing.LireIndexer;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import retrieval.LireSearcher;

public class Main {

    private static Logger logger = Base.logger(Main.class);

    public static void main(String[] args) {
        // Settings
        boolean skip_index;

        Options options = new Options();
        options.addOption("h", "help", false, "Show this message.");
        options.addOption(OptionBuilder.withLongOpt("index-path")
                .withDescription("Set the directory where the index is.")
                .hasArg()
                .withArgName("INDEX-DIR")
                .create("i")
        );
        options.addOption(OptionBuilder.withLongOpt("data-path")
                        .withDescription("Set the directory where the dataset is.")
                        .hasArg()
                        .withArgName("DATA-DIR")
                        .create("d")
        );
        options.addOption("si", "skip-index", false, "Skip the indexing step.");
        options.addOption(OptionBuilder.withLongOpt("query-file")
                .withDescription("Set the query file.")
                .hasArg()
                .withArgName("QUERY-FILE")
                .create("q")
        );
        options.addOption(OptionBuilder.withLongOpt("query-image")
                .withDescription("Set the query image.")
                .hasArg()
                .withArgName("QUERY-IMAGE")
                .create("qi")
        );
        options.addOption(OptionBuilder.withLongOpt("result-file")
                .withDescription("Set the result file.")
                .hasArg()
                .withArgName("RESULT-FILE")
                .create("r")
        );
        options.addOption(OptionBuilder.withLongOpt("num-of-results")
                        .withDescription("How many results to retrive")
                        .hasArg()
                        .withArgName("NUM-OF-RESULTS")
                        .create("n")
        );
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                throw new Exception();
            }
            Config.index_path = cmd.getOptionValue("indexing", "index");
            Config.data_path = cmd.getOptionValue("data-path", "dataset");
            Config.query_file = cmd.getOptionValue("query-file");
            Config.query_image = cmd.getOptionValue("query-image");
            Config.result_file = cmd.getOptionValue("result-file");
            Config.num_of_results = Integer.parseInt(cmd.getOptionValue("num-of-results", "50"));
            skip_index = cmd.hasOption("skip-index");

            logger.info("Process started.");
            try {
                if (!skip_index && Config.data_path != null) {
                    LireIndexer.index();
                }
                LireSearcher.searchByConfig();
            } catch (Exception e) {
                logger.error("Process failed with error: {}", e.toString());
                e.printStackTrace();
            }
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("INS", options);
        }
    }
}

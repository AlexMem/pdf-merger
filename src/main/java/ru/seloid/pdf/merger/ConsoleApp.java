package ru.seloid.pdf.merger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.seloid.pdf.merger.sevice.Merger;

public class ConsoleApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleApp.class);

    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            LOGGER.error("Not enough args");
            return;
        }
        LOGGER.info("Input dir {}", args[0]);
        LOGGER.info("Output file {}", args[1]);
        LOGGER.info("Merging...");
        Merger.merge(args[0], args[1]);
        LOGGER.info("Done");
    }
}

package org.example.utils.logger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtility {
    private static FileHandler fileHandler;

    static {
        try {
            fileHandler = new FileHandler("server.log", true); // true = append to existing log
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

        } catch (IOException e) {
            System.err.println("Failed to set up file logger: " + e.getMessage());
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());

        // Add the shared FileHandler only once
        if (fileHandler != null) {
            logger.addHandler(fileHandler);
        }

        // Add a ConsoleHandler to also log to the console
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        // Prevent duplicate logs
        logger.setUseParentHandlers(false);

        return logger;
    }
}

package org.example.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LoggerUtility {

    public static Logger getLogger(Class<?> clazz, int countryCode) {
        Logger logger = Logger.getLogger(clazz.getName());

        try {
            File logDir = new File("client_logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            FileHandler countryFileHandler = new FileHandler("client_logs/country" + countryCode + ".log", true);
            countryFileHandler.setLevel(Level.ALL);
            countryFileHandler.setFormatter(new SimpleFormatter());

            logger.addHandler(countryFileHandler);

        } catch (IOException e) {
            System.err.println("Failed to set up country-specific file logger for country code " + countryCode + ": " + e.getMessage());
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        logger.setUseParentHandlers(false);

        return logger;
    }
}

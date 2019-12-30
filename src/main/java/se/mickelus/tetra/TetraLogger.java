package se.mickelus.tetra;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TetraLogger {

    private static Logger logger;

    static {
        logger = Logger.getLogger(TetraMod.MOD_ID);
    }

    public static void log(Object message) {
        log(Level.INFO, message);
    }

    public static void log(Level level, Object message) {
        if(message != null) {
            logger.log(Level.INFO, message.toString());
        } else {
            logger.log(Level.INFO, "null");
        }
    }

    public static void logf(String format, Object... args) {
        logf(Level.INFO, format, args);
    }

    public static void logf(Level level, String format, Object... args) {
        logger.log(level, String.format(format, args));
    }
}
package se.mickelus.tetra.module.schema;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InvalidSchemaException extends Exception {
    private static Logger logger = LogManager.getLogger();

    private String key;
    private String[] faultyModules;

    public InvalidSchemaException(String key, String[] faultyModules) {
        this.key = key;
        this.faultyModules = faultyModules;
    }

    public void printMessage() {
        logger.warn("Skipping schema '{}' due to faulty module keys:", key);
        for (String faultyKey : faultyModules) {
            logger.warn("\t" + faultyKey);
        }
    }
}

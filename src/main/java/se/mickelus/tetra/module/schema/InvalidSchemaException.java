package se.mickelus.tetra.module.schema;

public class InvalidSchemaException extends Exception {

    private String key;
    private String[] faultyModules;

    public InvalidSchemaException(String key, String[] faultyModules) {
        this.key = key;
        this.faultyModules = faultyModules;
    }

    public void printMessage() {
        System.err.println(String.format("Skipping schema '%s' due to faulty module keys:", key));
        for (String faultyKey : faultyModules) {
            System.err.println("\t" + faultyKey);
        }
    }
}

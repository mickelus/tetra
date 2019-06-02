package se.mickelus.tetra.items.journal;

public enum JournalPage {
    craft("CRFT"),
    blueprint("BRPT"),
    system("SYST");

    public String label;

    JournalPage(String label) {
        this.label = label;
    }
}

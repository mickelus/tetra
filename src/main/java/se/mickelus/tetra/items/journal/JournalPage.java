package se.mickelus.tetra.items.journal;

public enum JournalPage {
    craft("CRFT"),
    structures("STRC"),
    system("SYST");

    public String label;

    JournalPage(String label) {
        this.label = label;
    }
}

package se.mickelus.tetra.items.modular.impl.holo;

public enum HoloPage {
    craft("CRFT"),
    structures("STRC"),
    system("SYST");

    public String label;

    HoloPage(String label) {
        this.label = label;
    }
}

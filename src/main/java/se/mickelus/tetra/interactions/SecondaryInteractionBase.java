package se.mickelus.tetra.interactions;

import net.minecraft.client.resources.language.I18n;

public abstract class SecondaryInteractionBase implements SecondaryInteraction {
    protected String key;
    protected PerformSide performSide;

    public SecondaryInteractionBase(String key, PerformSide performSide) {
        this.key = key;
        this.performSide = performSide;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getLabel() {
        return I18n.get("tetra.interactions." + key + ".label");
    }

    @Override
    public PerformSide getPerformSide() {
        return performSide;
    }
}

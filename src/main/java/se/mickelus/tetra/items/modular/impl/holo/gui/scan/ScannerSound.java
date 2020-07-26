package se.mickelus.tetra.items.modular.impl.holo.gui.scan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraSounds;

@OnlyIn(Dist.CLIENT)
public class ScannerSound extends TickableSound {
    private int activeCounter;
    private Minecraft mc;
    private boolean hasStarted;

    public ScannerSound(Minecraft mc) {
        super(TetraSounds.scannerLoop, SoundCategory.PLAYERS);

        this.mc = mc;

        priority = true;
        repeat = true;
        attenuationType = AttenuationType.NONE;

        volume = 0;
        pitch = 0.5f;
    }

    public void activate() {
        if (!hasStarted) {
            mc.getSoundHandler().play(this);
            hasStarted = true;
        }

        activeCounter = 2;
    }

    public void reset() {
        mc.getSoundHandler().stop(this);
        hasStarted = false;
    }

    @Override
    public boolean canBeSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (activeCounter > 0) {
            volume = Math.min(volume + 0.03f, 0.6f);

            activeCounter--;
        } else {
            volume = Math.max(volume - 0.027f, 0);
        }
    }
}
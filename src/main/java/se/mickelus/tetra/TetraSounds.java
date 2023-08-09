package se.mickelus.tetra;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TetraSounds {
    public static final SoundEvent scannerLoop = SoundEvent.createVariableRangeEvent(new ResourceLocation(TetraMod.MOD_ID, "scanner"));
    public static final SoundEvent scanMiss = SoundEvent.createVariableRangeEvent(new ResourceLocation(TetraMod.MOD_ID, "scan_miss"));
    public static final SoundEvent scanHit = SoundEvent.createVariableRangeEvent(new ResourceLocation(TetraMod.MOD_ID, "scan_hit"));
    public static final SoundEvent honeGain = SoundEvent.createVariableRangeEvent(new ResourceLocation(TetraMod.MOD_ID, "hone_gain"));
    public static final SoundEvent settle = SoundEvent.createVariableRangeEvent(new ResourceLocation(TetraMod.MOD_ID, "settle"));
}

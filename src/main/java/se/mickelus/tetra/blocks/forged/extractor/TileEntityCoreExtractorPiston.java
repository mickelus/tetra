package se.mickelus.tetra.blocks.forged.extractor;

import com.google.common.collect.ImmutableMap;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.TimeValues;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;

public class TileEntityCoreExtractorPiston extends TileEntity implements ITickable {

    private static final long activationDuration = 95;
    private static final int fillAmount = 32;

    private final TimeValues.VariableValue activationTime = new TimeValues.VariableValue(Float.NEGATIVE_INFINITY);
    private long endTime = Long.MAX_VALUE;

    @Nullable
    private final IAnimationStateMachine asm;

    public TileEntityCoreExtractorPiston() {
        if (FMLCommonHandler.instance().getEffectiveSide().equals(Side.CLIENT)) {
            asm = ModelLoaderRegistry.loadASM(new ResourceLocation(TetraMod.MOD_ID, "asms/block/extractor_piston.json"),
                    ImmutableMap.of("activation_time", activationTime));
        } else {
            asm = null;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return CapabilityAnimation.ANIMATION_CAPABILITY.equals(capability) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityAnimation.ANIMATION_CAPABILITY) {
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        }
        return super.getCapability(capability, side);
    }

    public void activate() {
        if (asm != null) {
            if (!asm.currentState().equals("active")) {
                asm.transition("active");
                activationTime.setValue(Animation.getWorldTime(getWorld(), Animation.getPartialTickTime()));
            }
        }

        if (!isActive()) {
            endTime = world.getTotalWorldTime() + activationDuration;
        }
    }

    public boolean isActive() {
        return endTime != Long.MAX_VALUE;
    }

    @Override
    public boolean hasFastRenderer()
    {
        return true;
    }

    @Override
    public void update() {
        if (endTime < world.getTotalWorldTime() ) {
            TileEntityOptional.from(world, pos.offset(Direction.DOWN), TileEntityCoreExtractorBase.class)
                    .ifPresent(base -> base.fill(fillAmount));

            runEndEffects();
            endTime = Long.MAX_VALUE;
        }
    }

    private void runEndEffects() {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                    pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                    5,  0, 0, 0, 0.02f);
        }

        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                0.1f, 1);

        world.playSound(null, pos, SoundEvents.BLOCK_METAL_FALL, SoundCategory.BLOCKS,
                0.2f, 0.5f);
    }
}

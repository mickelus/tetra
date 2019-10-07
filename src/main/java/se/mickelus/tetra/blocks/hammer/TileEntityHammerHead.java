package se.mickelus.tetra.blocks.hammer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.TimeValues;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nullable;

public class TileEntityHammerHead extends TileEntity {

    private final TimeValues.VariableValue activationTime = new TimeValues.VariableValue(Float.NEGATIVE_INFINITY);

    @Nullable
    private final IAnimationStateMachine asm;

    public TileEntityHammerHead() {
        if (FMLCommonHandler.instance().getEffectiveSide().equals(Side.CLIENT)) {
            asm = ModelLoaderRegistry.loadASM(new ResourceLocation(TetraMod.MOD_ID, "asms/block/hammer_head.json"),
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
    }

    @Override
    public boolean hasFastRenderer()
    {
        return true;
    }
}

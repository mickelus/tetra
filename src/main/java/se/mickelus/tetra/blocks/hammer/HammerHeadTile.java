package se.mickelus.tetra.blocks.hammer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.animation.TimeValues;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class HammerHeadTile extends TileEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerHeadBlock.unlocalizedName)
    public static TileEntityType<HammerBaseTile> type;

    private final TimeValues.VariableValue activationTime = new TimeValues.VariableValue(Float.NEGATIVE_INFINITY);

    private final LazyOptional<IAnimationStateMachine> asmOptional;

    public HammerHeadTile() {
        super(type);
        if (FMLEnvironment.dist.isClient()) {
            asmOptional = LazyOptional.of(() ->
                    ModelLoaderRegistry.loadASM(new ResourceLocation(TetraMod.MOD_ID, "asms/block/hammer_head.json"),
                            ImmutableMap.of("activation_time", activationTime)));
        } else {
            asmOptional = LazyOptional.empty();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        return CapabilityAnimation.ANIMATION_CAPABILITY.orEmpty(cap, asmOptional);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withProperty(Properties.AnimationProperty).build();
    }

    public void activate() {
        asmOptional
                .filter(asm -> !asm.currentState().equals("active"))
                .ifPresent(asm -> {
                    asm.transition("active");
                    activationTime.setValue(Animation.getWorldTime(getWorld(), Animation.getPartialTickTime()));
                });
    }

    @Override
    public boolean hasFastRenderer()
    {
        return true;
    }
}

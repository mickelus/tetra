package se.mickelus.tetra.blocks.forged;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;

import javax.annotation.Nullable;
import java.util.List;

public class BlockForgedPlatform extends TetraBlock {
    static final String unlocalizedName = "forged_platform";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedPlatform instance;

    public BlockForgedPlatform() {
        super(ForgedBlockCommon.propertiesSolid);

        hasItem = true;

    setRegistryName(unlocalizedName);
}

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }
}
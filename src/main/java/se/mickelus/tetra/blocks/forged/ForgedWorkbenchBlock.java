package se.mickelus.tetra.blocks.forged;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;

import javax.annotation.Nullable;
import java.util.List;

public class ForgedWorkbenchBlock extends AbstractWorkbenchBlock {
    public static final String unlocalizedName = "forged_workbench";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static AbstractWorkbenchBlock instance;

    public ForgedWorkbenchBlock() {
        super(ForgedBlockCommon.properties);

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.hintTooltip);
    }
}

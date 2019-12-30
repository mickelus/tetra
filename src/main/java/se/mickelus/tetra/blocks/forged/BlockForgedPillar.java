package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TextHelper;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.Materials;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class BlockForgedPillar extends RotatedPillarBlock implements ITetraBlock {
    private static final String unlocalizedName = "forged_pillar";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedPillar instance;

    public BlockForgedPillar() {
        super(Properties.create(Materials.forgedBlock)
                .sound(SoundType.METAL)
                .harvestTool(ToolTypes.hammer)
                .harvestLevel(4)
                .hardnessAndResistance(10F, 25));

        setRegistryName(unlocalizedName);
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(TextHelper.forgedBlockTooltip);
    }

    @Override
    public boolean hasItem() {
        return true;
    }
}

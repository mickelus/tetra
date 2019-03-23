package se.mickelus.tetra.generation.processor;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;
import se.mickelus.tetra.blocks.hammer.BlockHammerBase;
import se.mickelus.tetra.blocks.hammer.EnumHammerConfig;
import se.mickelus.tetra.blocks.hammer.EnumHammerPlate;
import se.mickelus.tetra.blocks.hammer.TileEntityHammerBase;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Random;

public class HammerProcessor implements ITemplateProcessor {

    Random random;

    public HammerProcessor(Random random) {
        this.random = random;
    }

    @Nullable
    @Override
    public Template.BlockInfo processBlock(World worldIn, BlockPos pos, Template.BlockInfo blockInfo) {
        if (blockInfo.blockState.getBlock() instanceof BlockHammerBase) {

            // randomize cells
            ItemCellMagmatic item = ItemCellMagmatic.instance;
            int discharge1 = random.nextInt(item.maxCharge);
            int discharge2 = item.maxCharge - random.nextInt(Math.max(discharge1, 1));
            TileEntityHammerBase.writeCells(blockInfo.tileentityData,
                    new ItemStack(item, 1, discharge1), new ItemStack(item, 1, discharge2));


            // randomize configurations
            EnumHammerConfig[] configs =  EnumHammerConfig.values();
            TileEntityHammerBase.writeConfig(blockInfo.tileentityData,
                    configs[random.nextInt(configs.length)], configs[random.nextInt(configs.length)]);

            // randomize plates
            if (random.nextFloat() < 0.1) {
                TileEntityHammerBase.writePlate(blockInfo.tileentityData,
                        random.nextBoolean() ? EnumHammerPlate.WEST : EnumHammerPlate.EAST, false);
            }
        }
        return blockInfo;
    }
}

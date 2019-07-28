package se.mickelus.tetra.generation.processor;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;
import se.mickelus.tetra.blocks.forged.transfer.BlockTransferUnit;
import se.mickelus.tetra.blocks.forged.transfer.EnumTransferConfig;
import se.mickelus.tetra.blocks.forged.transfer.TileEntityTransferUnit;
import se.mickelus.tetra.blocks.hammer.BlockHammerBase;
import se.mickelus.tetra.blocks.hammer.EnumHammerConfig;
import se.mickelus.tetra.blocks.hammer.EnumHammerPlate;
import se.mickelus.tetra.blocks.hammer.TileEntityHammerBase;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Random;

public class TransferUnitProcessor implements ITemplateProcessor {

    Random random;

    public TransferUnitProcessor(Random random) {
        this.random = random;
    }

    @Nullable
    @Override
    public Template.BlockInfo processBlock(World worldIn, BlockPos pos, Template.BlockInfo blockInfo) {
        if (blockInfo.blockState.getBlock() instanceof BlockTransferUnit) {

            // randomize cell
            if (random.nextFloat() < 0.1) {
                int charge = random.nextInt(ItemCellMagmatic.maxCharge);
                TileEntityTransferUnit.writeCell(blockInfo.tileentityData, new ItemStack(ItemCellMagmatic.instance, 1, charge));
            } else if (random.nextFloat() < 0.2) {
                TileEntityTransferUnit.writeCell(blockInfo.tileentityData, new ItemStack(ItemCellMagmatic.instance, 1, 0));
            }

            // randomize configurations
            EnumTransferConfig[] configs =  EnumTransferConfig.values();
            TileEntityTransferUnit.writeConfig(blockInfo.tileentityData, configs[random.nextInt(configs.length)]);

            // randomize plates
            if (random.nextFloat() < 0.7) {
                TileEntityTransferUnit.writePlate(blockInfo.tileentityData, random.nextBoolean());
            }
        }
        return blockInfo;
    }
}

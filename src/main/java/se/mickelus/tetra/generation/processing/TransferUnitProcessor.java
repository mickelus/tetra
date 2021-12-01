package se.mickelus.tetra.generation.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import se.mickelus.tetra.blocks.forged.transfer.EnumTransferConfig;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlock;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitTile;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
@ParametersAreNonnullByDefault
public class TransferUnitProcessor extends StructureProcessor {
    public TransferUnitProcessor() {}

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo $, StructureTemplate.StructureBlockInfo blockInfo,
            StructurePlaceSettings placementSettings, @Nullable StructureTemplate template) {
        if (blockInfo.state.getBlock() instanceof TransferUnitBlock) {
            Random random = placementSettings.getRandom(blockInfo.pos);

            CompoundTag newCompound = blockInfo.nbt.copy();

            int cellState = 0;

            // randomize cell
            if (random.nextFloat() < 0.05) {
                int charge = random.nextInt(ItemCellMagmatic.maxCharge);
                ItemStack itemStack = new ItemStack(ItemCellMagmatic.instance);
                ItemCellMagmatic.instance.recharge(itemStack, charge);

                cellState = charge > 0 ? 2 : 1;

                TransferUnitTile.writeCell(newCompound, itemStack);
            } else if (random.nextFloat() < 0.1) {
                TransferUnitTile.writeCell(newCompound, new ItemStack(ItemCellMagmatic.instance));
            }

            // randomize configuration & plate
            EnumTransferConfig[] configs = EnumTransferConfig.values();
            BlockState newState = blockInfo.state
                    .setValue(TransferUnitBlock.cellProp, cellState)
                    .setValue(TransferUnitBlock.configProp, configs[random.nextInt(configs.length)])
                    .setValue(TransferUnitBlock.plateProp, random.nextBoolean());

            return new StructureTemplate.StructureBlockInfo(blockInfo.pos, newState, newCompound);
        }
        return blockInfo;
    }

    @Override
    protected StructureProcessorType getType() {
        return ProcessorTypes.transferUnit;
    }
}

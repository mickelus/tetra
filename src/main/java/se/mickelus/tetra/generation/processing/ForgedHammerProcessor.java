package se.mickelus.tetra.generation.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseBlock;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseBlockEntity;
import se.mickelus.tetra.blocks.forged.hammer.HammerEffect;
import se.mickelus.tetra.items.cell.ThermalCellItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ForgedHammerProcessor extends StructureProcessor {
    public ForgedHammerProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo $, StructureTemplate.StructureBlockInfo blockInfo,
            StructurePlaceSettings placementSettings, @Nullable StructureTemplate template) {
        if (blockInfo.state.getBlock() instanceof HammerBaseBlock) {
            RandomSource random = placementSettings.getRandom(blockInfo.pos);
            CompoundTag newCompound = blockInfo.nbt.copy();

            // randomize cells
            ItemStack cell1 = random.nextBoolean() ? new ItemStack(ThermalCellItem.instance.get()) : null;
            ItemStack cell2 = random.nextBoolean() ? new ItemStack(ThermalCellItem.instance.get()) : null;

            int charge1 = random.nextInt(ThermalCellItem.maxCharge);
            if (cell1 != null) {
                ThermalCellItem.recharge(cell1, charge1);
            }

            int charge2 = ThermalCellItem.maxCharge - random.nextInt(Math.max(charge1, 1));
            if (cell2 != null) {
                ThermalCellItem.recharge(cell2, charge2);
            }

            HammerBaseBlockEntity.writeCells(newCompound, cell1, cell2);

            HammerEffect module = HammerEffect.efficient;
            if (random.nextFloat() < 0.1) {
                module = HammerEffect.reliable;
            } else if (random.nextFloat() < 0.1) {
                module = random.nextBoolean() ? HammerEffect.precise : HammerEffect.power;
            }

            if (random.nextBoolean()) {
                HammerBaseBlockEntity.writeModules(newCompound, module, null);
            } else {
                HammerBaseBlockEntity.writeModules(newCompound, null, module);
            }


            return new StructureTemplate.StructureBlockInfo(blockInfo.pos, blockInfo.state, newCompound);
        }
        return blockInfo;
    }

    @Override
    protected StructureProcessorType getType() {
        return ProcessorTypes.forgedHammer;
    }
}

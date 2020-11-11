package se.mickelus.tetra.generation.processing;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import se.mickelus.tetra.blocks.forged.hammer.*;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Random;

public class ForgedHammerProcessor extends StructureProcessor {
    public ForgedHammerProcessor() { }

    @Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, BlockPos pos2, Template.BlockInfo $, Template.BlockInfo blockInfo,
            PlacementSettings placementSettings, @Nullable Template template) {
        if (blockInfo.state.getBlock() instanceof HammerBaseBlock) {
            Random random = placementSettings.getRandom(blockInfo.pos);
            CompoundNBT newCompound = blockInfo.nbt.copy();

            // randomize cells
            ItemStack cell1 = random.nextBoolean() ? new ItemStack(ItemCellMagmatic.instance) : null;
            ItemStack cell2 = random.nextBoolean() ? new ItemStack(ItemCellMagmatic.instance) : null;

            int charge1 = random.nextInt(ItemCellMagmatic.maxCharge);
            if (cell1 != null) {
                ItemCellMagmatic.instance.recharge(cell1, charge1);
            }

            int charge2 = ItemCellMagmatic.maxCharge - random.nextInt(Math.max(charge1, 1));
            if (cell2 != null) {
                ItemCellMagmatic.instance.recharge(cell2, charge2);
            }

            HammerBaseTile.writeCells(newCompound, cell1, cell2);

            HammerEffect module = HammerEffect.efficient;
            if (random.nextFloat() < 0.1) {
                module = HammerEffect.reliable;
            } else if (random.nextFloat() < 0.1) {
                module = random.nextBoolean() ? HammerEffect.precise : HammerEffect.power;
            }

            if (random.nextBoolean()) {
                HammerBaseTile.writeModules(newCompound, module, null);
            } else {
                HammerBaseTile.writeModules(newCompound, null, module);
            }


            return new Template.BlockInfo(blockInfo.pos, blockInfo.state, newCompound);
        }
        return blockInfo;
    }

    @Override
    protected IStructureProcessorType getType() {
        return ProcessorTypes.forgedHammer;
    }
}

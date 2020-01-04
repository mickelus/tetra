package se.mickelus.tetra.blocks.forged.transfer;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Random;

public class TransferUnitProcessor extends StructureProcessor {


    public TransferUnitProcessor() {}

    @Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, Template.BlockInfo $, Template.BlockInfo blockInfo,
            PlacementSettings placementSettings, @Nullable Template template) {
        if (blockInfo.state.getBlock() instanceof TransferUnitBlock) {
            Random random = placementSettings.getRandom(blockInfo.pos);

            CompoundNBT newCompound = blockInfo.nbt.copy();

            // randomize cell
            if (random.nextFloat() < 0.05) {
                int charge = random.nextInt(ItemCellMagmatic.maxCharge);
                ItemStack itemStack = new ItemStack(ItemCellMagmatic.instance);
                itemStack.setDamage(charge);
                TransferUnitTile.writeCell(newCompound, itemStack);
            } else if (random.nextFloat() < 0.1) {
                TransferUnitTile.writeCell(newCompound, new ItemStack(ItemCellMagmatic.instance));
            }

            // randomize configuration & plate
            EnumTransferConfig[] configs =  EnumTransferConfig.values();
            BlockState newState = blockInfo.state
                    .with(TransferUnitBlock.configProp, configs[random.nextInt(configs.length)])
                    .with(TransferUnitBlock.plateProp, random.nextBoolean());

            return new Template.BlockInfo(blockInfo.pos, newState, newCompound);
        }
        return blockInfo;
    }

    protected IStructureProcessorType getType() {
        return IStructureProcessorType.NOP;
    }

    protected <T> Dynamic<T> serialize0(DynamicOps<T> ops) {
        return new Dynamic<>(ops);
    }
}

package se.mickelus.tetra.levelgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import se.mickelus.tetra.compat.forge.registries.RegistryObject;
import se.mickelus.tetra.blocks.forged.transfer.EnumTransferConfig;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlock;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlockEntity;
import se.mickelus.tetra.items.cell.ThermalCellItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TransferUnitProcessor extends StructureProcessor {
    public static final TransferUnitProcessor INSTANCE = new TransferUnitProcessor();
    public static final MapCodec<TransferUnitProcessor> codec = MapCodec.unit(TransferUnitProcessor.INSTANCE);
    public static RegistryObject<StructureProcessorType<?>> type;

    public TransferUnitProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo $,
            StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings placementSettings, @Nullable StructureTemplate template) {
        if (blockInfo.state().getBlock() instanceof TransferUnitBlock) {
            RandomSource random = placementSettings.getRandom(blockInfo.pos());

            CompoundTag newCompound = blockInfo.nbt().copy();

            int cellState = 0;

            // randomize cell
            if (random.nextFloat() < 0.1) {
                int charge = random.nextInt(ThermalCellItem.maxCharge);
                ItemStack itemStack = new ItemStack(ThermalCellItem.instance.get());
                ThermalCellItem.recharge(itemStack, charge);

                cellState = charge > 0 ? 2 : 1;

                TransferUnitBlockEntity.writeCell(newCompound, world.registryAccess(), itemStack);
            } else if (random.nextFloat() < 0.2) {
                TransferUnitBlockEntity.writeCell(newCompound, world.registryAccess(), new ItemStack(ThermalCellItem.instance.get()));
                cellState = 1;
            }

            // randomize configuration & plate
            EnumTransferConfig[] configs = EnumTransferConfig.values();
            BlockState newState = blockInfo.state()
                    .setValue(TransferUnitBlock.cellProp, cellState)
                    .setValue(TransferUnitBlock.configProp, configs[random.nextInt(configs.length)])
                    .setValue(TransferUnitBlock.plateProp, random.nextBoolean());

            return new StructureTemplate.StructureBlockInfo(blockInfo.pos(), newState, newCompound);
        }
        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return type.get();
    }
}

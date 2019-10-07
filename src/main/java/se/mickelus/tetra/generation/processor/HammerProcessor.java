package se.mickelus.tetra.generation.processor;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import se.mickelus.tetra.blocks.hammer.BlockHammerBase;
import se.mickelus.tetra.blocks.hammer.EnumHammerConfig;
import se.mickelus.tetra.blocks.hammer.EnumHammerPlate;
import se.mickelus.tetra.blocks.hammer.TileEntityHammerBase;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Random;

public class HammerProcessor extends StructureProcessor {

    Random random;

    public HammerProcessor(Random random) {
        this.random = random;
    }

    @Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, Template.BlockInfo baseInfo,
            Template.BlockInfo updatedInfo, PlacementSettings placementSettings) {
        if (updatedInfo.state.getBlock() instanceof BlockHammerBase) {

            // randomize cells
            ItemCellMagmatic item = ItemCellMagmatic.instance;

            ItemStack itemStack1 = new ItemStack(item, 1);
            item.setCharge(itemStack1, random.nextInt(ItemCellMagmatic.maxCharge));

            ItemStack itemStack2 = new ItemStack(item, 1);
            item.setCharge(itemStack2, ItemCellMagmatic.maxCharge - random.nextInt(Math.max(item.getCharge(itemStack1), 1)));

            TileEntityHammerBase.writeCells(baseInfo.nbt, itemStack1, itemStack2);


            // randomize configurations
            EnumHammerConfig[] configs =  EnumHammerConfig.values();
            TileEntityHammerBase.writeConfig(baseInfo.nbt,
                    configs[random.nextInt(configs.length)], configs[random.nextInt(configs.length)]);


            // randomize plates
            if (random.nextFloat() < 0.1) {
                TileEntityHammerBase.writePlate(baseInfo.nbt,
                        random.nextBoolean() ? EnumHammerPlate.WEST : EnumHammerPlate.EAST, false);
            }
        }
        return baseInfo;
    }

    @Override
    protected IStructureProcessorType getType() {
        return null;
    }

    @Override
    protected <T> Dynamic<T> serialize0(DynamicOps<T> ops) {
        return null;
    }
}

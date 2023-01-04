package se.mickelus.tetra.data.provider;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicBlock;
import se.mickelus.tetra.items.forged.MetalScrapItem;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MultiblockSchematicLootProvider extends BlockLoot {
    public static List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getLootTables() {
        return ImmutableList.of(
                Pair.of(() -> getMultiBlockSchematics("stonecutter", 3, 2, true), LootContextParamSets.BLOCK),
                Pair.of(() -> getMultiBlockSchematics("earthpiercer", 2, 2, true), LootContextParamSets.BLOCK),
                Pair.of(() -> getMultiBlockSchematics("extractor", 3, 3, true), LootContextParamSets.BLOCK)
        );
    }

    private static Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> getMultiBlockSchematics(String identifier, int width, int height, boolean ruinable) {
        return consumer -> {
            for (int h = 0; h < width; h++) {
                for (int v = 0; v < height; v++) {
                    String id = String.format(MultiblockSchematicBlock.Builder.format, identifier, h, v);
                    consumer.accept(new ResourceLocation("tetra", MultiblockSchematicBlock.Builder.pryTablePrefix + id),
                            getMultiBlockSchematicPryTable(id));

                    consumer.accept(new ResourceLocation("tetra", "blocks/" + id),
                            getMultiBlockSchematicDropTable(id));

                    if (ruinable) {
                        id = String.format(MultiblockSchematicBlock.Builder.ruinedFormat, identifier, h, v);
                        consumer.accept(new ResourceLocation("tetra", MultiblockSchematicBlock.Builder.pryTablePrefix + id),
                                getMultiBlockSchematicPryTable(id));

                        consumer.accept(new ResourceLocation("tetra", "blocks/" + id), getMultiBlockSchematicDropTable(id));
                    }
                }
            }
        };
    }

    private static LootTable.Builder getMultiBlockSchematicPryTable(String identifier) {
        ResourceLocation rl = new ResourceLocation("tetra", identifier);
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ForgeRegistries.BLOCKS.getValue(rl))));
    }

    private static LootTable.Builder getMultiBlockSchematicDropTable(String identifier) {
        ResourceLocation rl = new ResourceLocation("tetra", identifier);

        return createSilkTouchDispatchTable(ForgeRegistries.BLOCKS.getValue(rl),
                LootItem.lootTableItem(MetalScrapItem.instance.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))
                        .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)));
    }
}

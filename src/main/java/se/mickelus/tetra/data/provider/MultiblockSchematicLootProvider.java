package se.mickelus.tetra.data.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicBlock;
import se.mickelus.tetra.items.forged.MetalScrapItem;
import se.mickelus.tetra.util.RegistryHelper;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MultiblockSchematicLootProvider extends BlockLootSubProvider {
    protected MultiblockSchematicLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    public static List<LootTableProvider.SubProviderEntry> getLootTables() {
        return List.of(
                new LootTableProvider.SubProviderEntry(registries -> getMultiBlockSchematics("stonecutter", 3, 2, true, registries), LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(registries -> getMultiBlockSchematics("earthpiercer", 2, 2, true, registries), LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(registries -> getMultiBlockSchematics("extractor", 3, 3, true, registries), LootContextParamSets.BLOCK)
        );
    }

    private static LootTableSubProvider getMultiBlockSchematics(String identifier, int width, int height, boolean ruinable,
            HolderLookup.Provider registries) {
        MultiblockSchematicLootProvider provider = new MultiblockSchematicLootProvider(registries);
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);

        return consumer -> {
            for (int h = 0; h < width; h++) {
                for (int v = 0; v < height; v++) {
                    String id = String.format(MultiblockSchematicBlock.Builder.format, identifier, h, v);
                    consumer.accept(ResourceKey.create(Registries.LOOT_TABLE,
                                    ResourceLocation.fromNamespaceAndPath("tetra", MultiblockSchematicBlock.Builder.pryTablePrefix + id)),
                            provider.getMultiBlockSchematicPryTable(id));
                    consumer.accept(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("tetra", "blocks/" + id)),
                            provider.getMultiBlockSchematicDropTable(id, enchantments));

                    if (ruinable) {
                        id = String.format(MultiblockSchematicBlock.Builder.ruinedFormat, identifier, h, v);
                        consumer.accept(ResourceKey.create(Registries.LOOT_TABLE,
                                        ResourceLocation.fromNamespaceAndPath("tetra", MultiblockSchematicBlock.Builder.pryTablePrefix + id)),
                                provider.getMultiBlockSchematicPryTable(id));
                        consumer.accept(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("tetra", "blocks/" + id)),
                                provider.getMultiBlockSchematicDropTable(id, enchantments));
                    }
                }
            }
        };
    }

    private LootTable.Builder getMultiBlockSchematicPryTable(String identifier) {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("tetra", identifier);
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Objects.requireNonNull(RegistryHelper.get(BuiltInRegistries.BLOCK, rl), "Unknown block: " + rl))));
    }

    private LootTable.Builder getMultiBlockSchematicDropTable(String identifier, HolderLookup.RegistryLookup<Enchantment> enchantments) {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("tetra", identifier);

        return createSilkTouchDispatchTable(Objects.requireNonNull(RegistryHelper.get(BuiltInRegistries.BLOCK, rl), "Unknown block: " + rl),
                LootItem.lootTableItem(MetalScrapItem.instance.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))
                        .apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE))));
    }

    @Override
    protected void generate() {
    }
}

package se.mickelus.tetra.data.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TetraLootTableProvider extends LootTableProvider {
    public TetraLootTableProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, Set.of(), MultiblockSchematicLootProvider.getLootTables(), lookupProvider);
    }
}

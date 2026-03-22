package se.mickelus.tetra.craftingeffect.outcome;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.lang.reflect.Type;
import java.util.Map;

public interface CraftingEffectOutcome {

    boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials,
            Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
            ItemStack[] postMaterials, float severity);

    class Deserializer implements JsonDeserializer<CraftingEffectOutcome> {
        @Override
        public CraftingEffectOutcome deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            Class<? extends CraftingEffectOutcome> clazz = CraftingEffectRegistry.getEffectClass(type);
            if (clazz != null) {
                return DataManager.gson.fromJson(json, clazz);
            }

            throw new JsonParseException("Crafting effect outcome type \"" + type + "\" is not valid");
        }
    }
}

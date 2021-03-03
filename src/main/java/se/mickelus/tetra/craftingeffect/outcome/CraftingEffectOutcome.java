package se.mickelus.tetra.craftingeffect.outcome;

import com.google.gson.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.data.DataManager;

import java.lang.reflect.Type;
import java.util.Map;

public interface CraftingEffectOutcome {

    boolean apply(ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player, ItemStack[] preMaterials,
            Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState, boolean consumeResources, ItemStack[] postMaterials);

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

package se.mickelus.tetra.craftingeffect.condition;

import com.google.gson.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.data.DataManager;

import java.lang.reflect.Type;
import java.util.Map;

public interface CraftingEffectCondition {
    boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player, ItemStack[] materials,
            Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState);

    class Deserializer implements JsonDeserializer<CraftingEffectCondition> {
        @Override
        public CraftingEffectCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            Class<? extends CraftingEffectCondition> clazz = CraftingEffectRegistry.getConditionClass(type);
            if (clazz != null) {
                return DataManager.gson.fromJson(json, clazz);
            }

            throw new JsonParseException("Crafting effect condition type \"" + type + "\" is not valid");
        }
    }
}

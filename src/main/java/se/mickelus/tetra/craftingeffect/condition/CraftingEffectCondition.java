package se.mickelus.tetra.craftingeffect.condition;

import com.google.gson.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.data.DataManager;

import java.lang.reflect.Type;
import java.util.Map;

public interface CraftingEffectCondition {
    boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials,
            Map<ToolType, Integer> tools, Level world, BlockPos pos, BlockState blockState);

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

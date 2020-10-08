package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.properties.PropertyHelper;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

public class ConfigActionImpl extends ConfigAction {

    private static final LootParameterSet lootParameters = new LootParameterSet.Builder()
            .required(LootParameters.TOOL)
            .required(LootParameters.THIS_ENTITY)
            .required(LootParameters.field_237457_g_)
            .build();

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean canPerformOn(PlayerEntity player, ItemStack itemStack) {
        return requirement.test(itemStack);
    }

    @Override
    public Collection<ToolType> getRequiredToolTypes(ItemStack itemStack) {
        return requiredTools.getValues();
    }

    @Override
    public int getRequiredToolLevel(ItemStack itemStack, ToolType toolType) {
        return requiredTools.getLevel(toolType);
    }

    @Override
    public Map<ToolType, Integer> getRequiredTools(ItemStack itemStack) {
        return requiredTools.getLevelMap();
    }

    @Override
    public void perform(PlayerEntity player, ItemStack targetStack, WorkbenchTile workbench) {
        if (!player.world.isRemote) {
            ServerWorld world = (ServerWorld) player.world;
            LootTable table = world.getServer().getLootTableManager().getLootTableFromLocation(lootTable);
            ItemStack toolStack = requiredTools.getLevelMap().entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(entry -> {
                        ItemStack providingStack = PropertyHelper.getPlayerProvidingItemStack(entry.getKey(), entry.getValue(), player);

                        if (providingStack.isEmpty()) {
                            providingStack = PropertyHelper.getToolbeltProvidingItemStack(entry.getKey(), entry.getValue(), player);
                        }

                        return providingStack;
                    })
                    .orElse(ItemStack.EMPTY);

            LootContext context = new LootContext.Builder(world)
                    .withLuck(player.getLuck())
                    .withParameter(LootParameters.TOOL, toolStack)
                    .withParameter(LootParameters.THIS_ENTITY, player)
                    .withParameter(LootParameters.field_237457_g_, player.getPositionVec())
                    .build(lootParameters);

            table.generate(context).forEach(itemStack -> {
                if (!player.inventory.addItemStackToInventory(itemStack)) {
                    player.dropItem(itemStack, false);
                }
            });

            BlockPos pos = workbench.getPos();
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, player.getSoundCategory(),
                    1.0F, 1.5f + (float) Math.random() * 0.5f);

            world.spawnParticle(new ItemParticleData(ParticleTypes.ITEM, targetStack),
                    pos.getX() + 0.5d, pos.getY() + 1.1d, pos.getZ() + 0.5d,
                    4, 0, 0, 0,
                    0.1f);

            // todo: add proper criteria
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity) player, targetStack);

            targetStack.setCount(targetStack.getCount() - 1);
            workbench.markDirty();
        }
    }
}

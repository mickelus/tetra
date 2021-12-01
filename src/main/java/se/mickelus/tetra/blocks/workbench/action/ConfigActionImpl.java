package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.properties.PropertyHelper;

import java.util.Collection;
import java.util.Map;

public class ConfigActionImpl extends ConfigAction {

    private static final LootContextParamSet lootParameters = new LootContextParamSet.Builder()
            .required(LootContextParams.ORIGIN)
            .optional(LootContextParams.TOOL)
            .optional(LootContextParams.THIS_ENTITY)
            .build();

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean canPerformOn(Player player, WorkbenchTile tile, ItemStack itemStack) {
        return requirement.matches(itemStack);
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
    public void perform(Player player, ItemStack targetStack, WorkbenchTile workbench) {
        if (player != null && !player.level.isClientSide) {
            ServerLevel world = (ServerLevel) player.level;
            LootTable table = world.getServer().getLootTables().get(lootTable);
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
                    .withParameter(LootContextParams.TOOL, toolStack)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .create(lootParameters);

            table.getRandomItems(context).forEach(itemStack -> {
                if (!player.inventory.add(itemStack)) {
                    player.drop(itemStack, false);
                }
            });

            BlockPos pos = workbench.getBlockPos();
            world.playSound(null, pos, SoundEvents.STONE_BREAK, player.getSoundSource(),
                    1.0F, 1.5f + (float) Math.random() * 0.5f);

            world.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, targetStack),
                    pos.getX() + 0.5d, pos.getY() + 1.1d, pos.getZ() + 0.5d,
                    4, 0, 0, 0,
                    0.1f);

            // todo: add proper criteria
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, targetStack);

            targetStack.setCount(targetStack.getCount() - 1);
            workbench.setChanged();
        } else if (!workbench.getLevel().isClientSide) {
            ServerLevel world = (ServerLevel) workbench.getLevel();
            LootTable table = world.getServer().getLootTables().get(lootTable);

            LootContext context = new LootContext.Builder(world)
                    .withParameter(LootContextParams.ORIGIN, Vec3.upFromBottomCenterOf(workbench.getBlockPos(), 1.1f))
                    .create(lootParameters);

            table.getRandomItems(context).forEach(itemStack -> Block.popResource(world, workbench.getBlockPos().above(), itemStack));

            BlockPos pos = workbench.getBlockPos();
            world.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS,
                    1.0F, 1.5f + (float) Math.random() * 0.5f);

            world.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, targetStack),
                    pos.getX() + 0.5d, pos.getY() + 1.1d, pos.getZ() + 0.5d,
                    4, 0, 0, 0,
                    0.1f);

            targetStack.setCount(targetStack.getCount() - 1);
            workbench.setChanged();
        }
    }

    @Override
    public boolean allowInWorldInteraction() {
        return inWorld;
    }
}

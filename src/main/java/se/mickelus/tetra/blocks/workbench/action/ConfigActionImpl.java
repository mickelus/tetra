package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.properties.PropertyHelper;

import java.util.Collection;
import java.util.Map;

public class ConfigActionImpl extends ConfigAction {

    private static final LootParameterSet lootParameters = new LootParameterSet.Builder()
            .required(LootParameters.ORIGIN)
            .optional(LootParameters.TOOL)
            .optional(LootParameters.THIS_ENTITY)
            .build();

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean canPerformOn(PlayerEntity player, WorkbenchTile tile, ItemStack itemStack) {
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
    public void perform(PlayerEntity player, ItemStack targetStack, WorkbenchTile workbench) {
        if (player != null && !player.level.isClientSide) {
            ServerWorld world = (ServerWorld) player.level;
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
                    .withParameter(LootParameters.TOOL, toolStack)
                    .withParameter(LootParameters.THIS_ENTITY, player)
                    .withParameter(LootParameters.ORIGIN, player.position())
                    .create(lootParameters);

            table.getRandomItems(context).forEach(itemStack -> {
                if (!player.inventory.add(itemStack)) {
                    player.drop(itemStack, false);
                }
            });

            BlockPos pos = workbench.getBlockPos();
            world.playSound(null, pos, SoundEvents.STONE_BREAK, player.getSoundSource(),
                    1.0F, 1.5f + (float) Math.random() * 0.5f);

            world.sendParticles(new ItemParticleData(ParticleTypes.ITEM, targetStack),
                    pos.getX() + 0.5d, pos.getY() + 1.1d, pos.getZ() + 0.5d,
                    4, 0, 0, 0,
                    0.1f);

            // todo: add proper criteria
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity) player, targetStack);

            targetStack.setCount(targetStack.getCount() - 1);
            workbench.setChanged();
        } else if (!workbench.getLevel().isClientSide) {
            ServerWorld world = (ServerWorld) workbench.getLevel();
            LootTable table = world.getServer().getLootTables().get(lootTable);

            LootContext context = new LootContext.Builder(world)
                    .withParameter(LootParameters.ORIGIN, Vector3d.upFromBottomCenterOf(workbench.getBlockPos(), 1.1f))
                    .create(lootParameters);

            table.getRandomItems(context).forEach(itemStack -> Block.popResource(world, workbench.getBlockPos().above(), itemStack));

            BlockPos pos = workbench.getBlockPos();
            world.playSound(null, pos, SoundEvents.STONE_BREAK, SoundCategory.BLOCKS,
                    1.0F, 1.5f + (float) Math.random() * 0.5f);

            world.sendParticles(new ItemParticleData(ParticleTypes.ITEM, targetStack),
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

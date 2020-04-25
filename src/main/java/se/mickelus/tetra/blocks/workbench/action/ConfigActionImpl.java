package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;

import java.util.Comparator;
import java.util.Map;

public class ConfigActionImpl extends ConfigAction {

    private static final LootParameterSet lootParameters = new LootParameterSet.Builder()
            .required(LootParameters.TOOL)
            .required(LootParameters.THIS_ENTITY)
            .required(LootParameters.POSITION)
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
    public Capability[] getRequiredCapabilitiesFor(ItemStack itemStack) {
        return requiredCapabilities.getValues().toArray(new Capability[0]);
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        return requiredCapabilities.getLevel(capability);
    }

    @Override
    public void perform(PlayerEntity player, ItemStack targetStack, WorkbenchTile workbench) {
        if (!player.world.isRemote) {
            ServerWorld world = (ServerWorld) player.world;
            LootTable table = world.getServer().getLootTableManager().getLootTableFromLocation(lootTable);
            ItemStack toolStack = requiredCapabilities.valueMap.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getValue))
                    .findFirst()
                    .map(entry -> {
                        ItemStack providingStack = CapabilityHelper.getPlayerProvidingItemStack(entry.getKey(), entry.getValue(), player);

                        if (providingStack.isEmpty()) {
                            providingStack = CapabilityHelper.getToolbeltProvidingItemStack(entry.getKey(), entry.getValue(), player);
                        }

                        return providingStack;
                    })
                    .orElse(ItemStack.EMPTY);

            LootContext context = new LootContext.Builder(world)
                    .withLuck(player.getLuck())
                    .withParameter(LootParameters.TOOL, toolStack)
                    .withParameter(LootParameters.THIS_ENTITY, player)
                    .withParameter(LootParameters.POSITION, player.getPosition())
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

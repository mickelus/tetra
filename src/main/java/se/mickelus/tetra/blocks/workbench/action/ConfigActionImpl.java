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
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.capabilities.Capability;

public class ConfigActionImpl extends ConfigAction {

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
    public void perform(PlayerEntity player, ItemStack targetStack, TileEntityWorkbench workbench) {
        if (!player.world.isRemote) {
            ServerWorld world = (ServerWorld) player.world;
            LootTable table = world.getServer().getLootTableManager().getLootTableFromLocation(lootTable);
            LootContext context = new LootContext.Builder(world)
                    .withLuck(player.getLuck())
                    .withParameter(LootParameters.THIS_ENTITY, player)
                    .build(LootParameterSets.GENERIC);

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
                    1, 0, world.rand.nextGaussian() * 0.2, 0, 0);

            // todo: add proper criteria
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity) player, targetStack);

            targetStack.setCount(targetStack.getCount() - 1);
            workbench.markDirty();
        }
    }
}
